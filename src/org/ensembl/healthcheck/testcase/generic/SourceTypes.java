/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all chromosomes have at least some genes with certain analyses.
 */
public class SourceTypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalysisTypes
	 */
	public SourceTypes() {

		setDescription("Check that all chromsosomes have at least some genes with certain sources.");
		setPriority(Priority.AMBER);
		setEffect("Some genes may have only Ensembl or Havana annotation.");
		setFix("Possibly indicates a problem with the Havana/Ensembl merge pipeline");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		// only applies to human, mouse and zebrafish at the moment
		String species = dbre.getSpecies();
		boolean is_merged = isMerged(species);

		if (!is_merged) {

			return true;

		}

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= geneSources(con);
		result &= transcriptSources(con);
		result &= geneTranscriptSources(con);

		return result;

	} // run

	public boolean geneSources(Connection con) {

		boolean result = true;

		String[] sources = { "ensembl", "havana", "ensembl_havana" };

		// get all chromosomes, ignore LRG and MT
		String[] seqRegionNames = DBUtils.getColumnValues(con,
				"SELECT s.name FROM seq_region s, seq_region_attrib sa, attrib_type at WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND code = 'karyotype_rank' AND s.name NOT LIKE 'MT'");

		// loop over each seq region, check that each logic name is represented
		try {

			PreparedStatement stmt = con.prepareStatement(
					"SELECT COUNT(*) FROM gene g, seq_region sr WHERE g.seq_region_id=sr.seq_region_id AND sr.name=? AND g.source=?");

			for (String seqRegion : seqRegionNames) {

				for (String source : sources) {

					stmt.setString(1, seqRegion);
					stmt.setString(2, source);

					ResultSet rs = stmt.executeQuery();

					rs.first();
					int rows = rs.getInt(1);

					if (rows == 0) {

						result = false;
						ReportManager.problem(this, con,
								String.format("Chromosome %s has no genes with source %s", seqRegion, source));

					}

					rs.close();

				}

			}

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing SQL");
			e.printStackTrace();

		}

		return result;

	} // geneSources

	public boolean transcriptSources(Connection con) {

		boolean result = true;

		String sql = "SELECT COUNT(*) FROM transcript WHERE isnull(source)";

		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, "Some transcripts have no source");

		}

		sql = "SELECT COUNT(*) FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('ensembl') AND (logic_name LIKE '%ensembl%' OR logic_name LIKE '%nrcna%') AND logic_name NOT LIKE '%havana%'";
		String useful_sql = "SELECT t.stable_id, t.source, a.logic_name FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('ensembl') AND (logic_name LIKE '%ensembl%' OR logic_name LIKE '%nrcna%') AND logic_name NOT LIKE '%havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con,
					"Some transcripts of %ensembl% and/or %ncrna% analyses do not have ensembl source. Try "
							+ useful_sql + " to find those transcripts");

		}

		sql = "SELECT COUNT(*) FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('havana') AND logic_name LIKE '%havana%' AND logic_name NOT LIKE '%ensembl_havana%'";
		useful_sql = "SELECT t.stable_id, t.source, a.logic_name FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('havana') AND logic_name LIKE '%havana%' AND logic_name NOT LIKE '%ensembl_havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, "Some transcripts of %havana% analysis do not have havana source. Try "
					+ useful_sql + " to find those transcripts");

		}

		sql = "SELECT COUNT(*) FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('ensembl_havana') AND logic_name LIKE '%ensembl_havana%'";
		useful_sql = "SELECT t.stable_id, t.source, a.logic_name FROM transcript t, analysis a WHERE t.analysis_id = a.analysis_id AND source not in ('ensembl_havana') AND logic_name LIKE '%ensembl_havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con,
					"Some transcripts of analysis %ensembl_havana% do not have ensembl_havana source. Try " + useful_sql
							+ " to find those transcripts");

		}

		return result;

	}

	public boolean geneTranscriptSources(Connection con) {

		boolean result = true;

		String sql = "SELECT COUNT(*) FROM gene g, transcript t where g.gene_id = t.gene_id and g.source = 'ensembl' and t.source not in ('ensembl')";
		String useful_sql = "SELECT g.stable_id, g.source, t.stable_id, t.source FROM gene g, transcript t where g.gene_id = t.gene_id and g.source = 'ensembl' and t.source not in ('ensembl')";

		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, "Some ensembl genes have transcripts which are not ensembl. Try "
					+ useful_sql + " to find those genes");

		}

		sql = "SELECT COUNT(*) FROM gene g, transcript t where g.gene_id = t.gene_id and g.source = 'havana' and t.source not in ('havana')";
		useful_sql = "SELECT g.stable_id, g.source, t.stable_id, t.source FROM gene g, transcript t where g.gene_id = t.gene_id and g.source = 'havana' and t.source not in ('havana')";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, "Some havana genes have transcripts which are not havana. Try "
					+ useful_sql + " to find those genes");

		}

		sql = "SELECT COUNT(*) FROM gene g, transcript t where g.gene_id = t.gene_id and g.source not in ('ensembl_havana') and t.source = 'ensembl_havana'";
		useful_sql = "SELECT g.stable_id, g.source, t.stable_id, t.source FROM gene g, transcript t where g.gene_id = t.gene_id and g.source not in ('ensembl_havana') and t.source = 'ensembl_havana'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con,
					"Some transcripts with ensembl_havana source belong to genes whose source is not ensembl_havana. Try "
							+ useful_sql + " to find those transcripts");

		}

		// Check source and analysis are consistent in the gene table

		sql = "SELECT COUNT(*) FROM gene g, analysis a where g.analysis_id = a.analysis_id and source not in ('ensembl') AND (logic_name LIKE '%ensembl%' OR logic_name LIKE '%nrcna%') AND logic_name NOT LIKE '%havana%'";
		useful_sql = "SELECT g.stable_id, g.source, a.logic_name FROM gene g, analysis a where g.analysis_id = a.analysis_id and source not in ('ensembl') AND (logic_name LIKE '%ensembl%' OR logic_name LIKE '%nrcna%') AND logic_name NOT LIKE '%havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con,
					"Some genes of %ensembl% and/or %ncrna% analyses do not have ensembl source. Try " + useful_sql
							+ " to find those genes");

		}

		sql = "SELECT COUNT(*) FROM gene g, analysis a WHERE g.analysis_id = a.analysis_id AND source not in ('havana') AND logic_name LIKE '%havana%' AND logic_name NOT LIKE '%ensembl_havana%'";
		useful_sql = "SELECT g.stable_id, g.source, a.logic_name FROM gene g, analysis a WHERE g.analysis_id = a.analysis_id AND source not in ('havana') AND logic_name LIKE '%havana%' AND logic_name NOT LIKE '%ensembl_havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, "Some genes of %havana% analysis do not have havana source. Try "
					+ useful_sql + " to find those genes");

		}

		sql = "SELECT COUNT(*) FROM gene g, analysis a WHERE g.analysis_id = a.analysis_id AND source not in ('ensembl_havana') AND logic_name LIKE '%ensembl_havana%'";
		useful_sql = "SELECT g.stable_id, g.source, a.logic_name FROM gene g, analysis a WHERE g.analysis_id = a.analysis_id AND source not in ('ensembl_havana') AND logic_name LIKE '%ensembl_havana%'";

		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con,
					"Some genes of analysis %ensembl_havana% do not have ensembl_havana source. Try " + useful_sql
							+ " to find those genes");

		}

		return result;

	}

	// --------------------------------------------------------------------------

} // AnalysisTypes
