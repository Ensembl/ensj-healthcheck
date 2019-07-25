package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;

public class MemberProductionCounts extends AbstractTemplatedTestCase {

	public MemberProductionCounts() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether gene member counts have been populated");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		// All the possible collection names
		List<String> allCollectionNames = DBUtils.getColumnValuesList(dbre.getConnection(), "SELECT DISTINCT clusterset_id FROM gene_tree_root WHERE tree_type = \"tree\" AND ref_root_id IS NULL");

		boolean result = true;

		// Check that there are no other collection names in gene_member_hom_stats
		for (String collection : DBUtils.getColumnValuesList(dbre.getConnection(), "SELECT DISTINCT collection FROM gene_member_hom_stats")) {
			if (!allCollectionNames.contains(collection)) {
				result = false;
				ReportManager.problem(this, dbre.getConnection(), "Found entries in gene_member_hom_stats for the " + collection + " collection but there isn't any data attached to it");
			}
		}

		// Check the counts for each collection name
		for (String collection : allCollectionNames) {
			result &= checkCountsForCollection(dbre, collection);
		}

		return result;
	}


	/**
	 * Returns the first row as an Integer array
	 */
	public Integer[] getFirstRowAsIntegers(Connection con, String sql) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int length = rs.getMetaData().getColumnCount();
				Integer[] values = new Integer[length];
				for (int sqlIndex = 1, arrayIndex = 0; sqlIndex <= length; sqlIndex++, arrayIndex++) {
					values[arrayIndex] = rs.getInt(sqlIndex);
				}
				rs.close();
				stmt.close();
				return values;
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new SqlUncheckedException("Could not execute query", e);
		} finally {
			DBUtils.closeQuietly(rs);
			DBUtils.closeQuietly(stmt);
		}
		return null;
	}


	private boolean checkCountsForCollection(DatabaseRegistryEntry dbre, String collection) {
		SqlTemplate srv = getSqlTemplate(dbre);
		Connection con  = dbre.getConnection();

		/*
		 * Check which data tables have actually been populated for that collection
		 */

		// How much data in each table
		String sqlFamilies    = "SELECT COUNT(*) FROM family";  // no concept of collection in the table itself ...
		String sqlGeneTrees   = "SELECT COUNT(*) FROM gene_tree_root WHERE clusterset_id = \"" + collection + "\"";
		String sqlPolyploids  = "SELECT COUNT(*) FROM genome_db WHERE genome_component IS NOT NULL";  // NOTE: assumes that the polyploid genomes are found in all the collections
		String sqlCAFETrees   = "SELECT COUNT(*) FROM gene_tree_root JOIN CAFE_gene_family ON gene_tree_root.root_id = gene_tree_root_id WHERE clusterset_id = \"" + collection + "\"";
		// Which columns should have non-zero counts
		boolean hasFamilies   = collection.equals("default") && (srv.queryForDefaultObject(sqlFamilies, Integer.class) > 0);  // ... so we record families in the "default" collection
		boolean hasGeneTrees  = srv.queryForDefaultObject(sqlGeneTrees, Integer.class) > 0;
		boolean hasPolyploids = srv.queryForDefaultObject(sqlPolyploids, Integer.class) > 0;
		boolean hasCAFETrees  = srv.queryForDefaultObject(sqlCAFETrees, Integer.class) > 0;

		// NOTE: keep "headers" in sync with the columns used in SUM
		String[] headers = {"families", "gene_trees", "gene_gain_loss_trees", "orthologues", "paralogues", "homoeologues"};
		String sqlCounts = "SELECT SUM(families), SUM(gene_trees), SUM(gene_gain_loss_trees), SUM(orthologues), SUM(paralogues), SUM(homoeologues) FROM gene_member_hom_stats WHERE collection = \"" + collection + "\"";
		Integer[] counts = getFirstRowAsIntegers(dbre.getConnection(), sqlCounts);

		// Which counts should be non-zero
		boolean[] expectNonEmpty = {hasFamilies, hasGeneTrees, hasCAFETrees, hasGeneTrees, hasGeneTrees, hasGeneTrees && hasPolyploids};


		/*
		 * Check that the table is not empty
		 */

		if ((hasFamilies || hasGeneTrees) && (counts[0] == null)) {
			ReportManager.problem(this, dbre.getConnection(), "Found no entries in gene_member_hom_stats for the " + collection + " collection. There should be some");
			return false;
		}


		/*
		 * Check every column independently
		 */

		boolean result = true;
		for (int i=0; i<headers.length; i++) {
			// Compare the zeroness of the count to the expectation
			if (expectNonEmpty[i] && (counts[i] == 0)) {
				ReportManager.problem(this, dbre.getConnection(), "Found no entries in gene_member_hom_stats with " + headers[i] + " > 0 for the " + collection + " collection. There should be some");
				result = false;
			} else if (!expectNonEmpty[i] && (counts[i] > 0)) {
				ReportManager.problem(this, dbre.getConnection(), "Found entries in gene_member_hom_stats with " + headers[i] + " > 0 for the " + collection + " collection. There shouldn't be any");
				result = false;
			}
		}


		/*
		 * Check the dependencies between columns
		 */

		// Where there are homologues, there must be a gene-tree
		String sqlBrokenHomologyCounts  = "SELECT COUNT(*) FROM gene_member_hom_stats WHERE gene_trees = 0 AND (orthologues > 0 OR paralogues > 0 OR homoeologues > 0) AND collection = '" + collection + "'";
		Integer numBrokenHomologyCounts = srv.queryForDefaultObject(sqlBrokenHomologyCounts, Integer.class);
		if (numBrokenHomologyCounts > 0) {
			ReportManager.problem(this, dbre.getConnection(), "Found " + numBrokenHomologyCounts + " rows the collection " + collection + " where there are homologues without gene_trees");
			result = false;
		}

		// Where there is a CAFE tree, there must be a gene-tree
		String sqlBrokenCAFEcounts  = "SELECT COUNT(*) FROM gene_member_hom_stats WHERE gene_trees = 0 AND gene_gain_loss_trees > 0 AND collection = '" + collection + "'";
		Integer numBrokenCAFEcounts = srv.queryForDefaultObject(sqlBrokenCAFEcounts, Integer.class);
		if (numBrokenCAFEcounts > 0) {
			ReportManager.problem(this, dbre.getConnection(), "Found " + numBrokenCAFEcounts + " rows for the collection " + collection + " where there are gene_gain_loss_trees without gene_trees");
			result = false;
		}

		return result;
	}
}
