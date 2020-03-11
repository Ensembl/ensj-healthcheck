package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;

public class MemberProductionCounts extends AbstractComparaTestCase {

	public MemberProductionCounts() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether gene member counts have been populated");
	}

	public boolean run(DatabaseRegistryEntry dbre) {


		boolean result = true;

		if (getComparaDivisionName(dbre).equals("grch37")) {

			// There are no gene-trees so we need to manually set the collection name here
			List<String> allCollectionNames = Arrays.asList("default");
			result &= compareCollectionNames(dbre, allCollectionNames);

			// The automatic detection doesn't allow homologies in the absence of gene-trees
			boolean[] expectNonEmpty = {false, false, false, false, true, false};
			result &= runComparison(dbre, "1", "(GRCh37)", expectNonEmpty);

		} else {

			// All the possible collection names
			List<String> allCollectionNames = DBUtils.getColumnValuesList(dbre.getConnection(), "SELECT DISTINCT clusterset_id FROM gene_tree_root WHERE tree_type = \"tree\" AND ref_root_id IS NULL");
			result &= compareCollectionNames(dbre, allCollectionNames);

			// Check the counts for each collection name
			for (String collection : allCollectionNames) {
				boolean[] expectNonEmpty = getExpectationForCollection(dbre, collection);
				result &= runComparison(dbre, "collection = \"" + collection + "\"", "for the " + collection + " collection", expectNonEmpty);
			}
		}

		return result;
	}

	public boolean compareCollectionNames(DatabaseRegistryEntry dbre, List<String> allCollectionNames) {
		boolean result = true;
		// Check that there are no other collection names in gene_member_hom_stats
		for (String collection : DBUtils.getColumnValuesList(dbre.getConnection(), "SELECT DISTINCT collection FROM gene_member_hom_stats")) {
			if (!allCollectionNames.contains(collection)) {
				result = false;
				ReportManager.problem(this, dbre.getConnection(), "Found entries in gene_member_hom_stats for the " + collection + " collection but there isn't any data attached to it");
			}
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


	private boolean[] getExpectationForCollection(DatabaseRegistryEntry dbre, String collection) {
		SqlTemplate srv = getSqlTemplate(dbre);

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

		// Which counts should be non-zero
		boolean[] expectNonEmpty = {hasFamilies, hasGeneTrees, hasCAFETrees, hasGeneTrees, hasGeneTrees, hasGeneTrees && hasPolyploids};
		return expectNonEmpty;
	}


	private boolean runComparison(DatabaseRegistryEntry dbre, String filterSQL, String filterDescription, boolean[] expectNonEmpty) {
		SqlTemplate srv = getSqlTemplate(dbre);
		Connection con  = dbre.getConnection();

		// NOTE: keep "headers" in sync with the columns used in SUM
		String[] headers = {"families", "gene_trees", "gene_gain_loss_trees", "orthologues", "paralogues", "homoeologues"};
		String sqlCounts = "SELECT SUM(families), SUM(gene_trees), SUM(gene_gain_loss_trees), SUM(orthologues), SUM(paralogues), SUM(homoeologues) FROM gene_member_hom_stats WHERE " + filterSQL;
		Integer[] counts = getFirstRowAsIntegers(con, sqlCounts);

		/*
		 * Check that the table is not empty
		 */

		boolean shouldBeNonEmpty = false;
		for (boolean e: expectNonEmpty) {
			shouldBeNonEmpty |= e;
		}

		if (shouldBeNonEmpty) {
			if (counts[0] == null) {
				ReportManager.problem(this, con, "Found no entries in gene_member_hom_stats " + filterDescription + ". There should be some");
				return false;
			}
		} else {
			if (counts[0] != null) {
				ReportManager.problem(this, con, "Found entries in gene_member_hom_stats " + filterDescription + ". There shouldn't be any");
				return false;
			}
		}

		/*
		 * Check every column independently
		 */

		boolean result = true;
		for (int i=0; i<headers.length; i++) {
			// Compare the zeroness of the count to the expectation
			if (expectNonEmpty[i] && (counts[i] == 0)) {
				ReportManager.problem(this, con, "Found no entries in gene_member_hom_stats with " + headers[i] + " > 0 " + filterDescription + ". There should be some");
				result = false;
			} else if (!expectNonEmpty[i] && (counts[i] > 0)) {
				ReportManager.problem(this, con, "Found entries in gene_member_hom_stats with " + headers[i] + " > 0 " + filterDescription + ". There shouldn't be any");
				result = false;
			}
		}


		/*
		 * Check the dependencies between columns
		 */

		if (expectNonEmpty[1]) { // gene_trees flag
			// Where there are homologues, there must be a gene-tree
			String sqlBrokenHomologyCounts  = "SELECT COUNT(*) FROM gene_member_hom_stats WHERE gene_trees = 0 AND (orthologues > 0 OR paralogues > 0 OR homoeologues > 0) AND " + filterSQL;
			Integer numBrokenHomologyCounts = srv.queryForDefaultObject(sqlBrokenHomologyCounts, Integer.class);
			if (numBrokenHomologyCounts > 0) {
				ReportManager.problem(this, con, "Found " + numBrokenHomologyCounts + " rows " + filterDescription + " where there are homologues without gene_trees");
				result = false;
			}

			// Where there is a CAFE tree, there must be a gene-tree
			String sqlBrokenCAFEcounts  = "SELECT COUNT(*) FROM gene_member_hom_stats WHERE gene_trees = 0 AND gene_gain_loss_trees > 0 AND " + filterSQL;
			Integer numBrokenCAFEcounts = srv.queryForDefaultObject(sqlBrokenCAFEcounts, Integer.class);
			if (numBrokenCAFEcounts > 0) {
				ReportManager.problem(this, con, "Found " + numBrokenCAFEcounts + " rows " + filterDescription + " where there are gene_gain_loss_trees without gene_trees");
				result = false;
			}
		}


		/*
		 * Check that the columns have been correctly populated
		 */

		// gene_trees
		result &= checkCountIsZero(con,
				"gene_member_hom_stats JOIN gene_member USING (gene_member_id) LEFT JOIN gene_tree_node ON canonical_member_id = seq_member_id",
				"node_id IS NULL AND gene_trees > 0 AND " + filterSQL
				);
		result &= checkCountIsZero(con,
				"gene_member_hom_stats JOIN gene_member USING (gene_member_id) JOIN gene_tree_node ON canonical_member_id = seq_member_id JOIN gene_tree_root USING (root_id)",
				"gene_trees = 0 AND collection = clusterset_id AND " + filterSQL
				);

		return result;
	}
}
