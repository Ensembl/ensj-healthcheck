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

/**
 * Check that analyses (e.g. havana) and their associated xref types (e.g. OTTT) exist, and vice versa.
 */
public class AnalysisXrefs extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalysisXrefs
	 */
	public AnalysisXrefs() {

		setDescription("Check that analyses (e.g. havana) and their associated xref types (e.g. OTTT) exist, and vice versa");
		setPriority(Priority.AMBER);
		setEffect("Will cause problems/miscoloring on web display.");
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
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// --------------------------------
		// havana/OTTT

    result &= checkAnalysisAndSource( dbre, "Transcript",
                         "havana",         "ensembl_havana_transcript",
                         "havana_ig_gene", "ensembl_havana_lincrna",
                         "proj_havana",    "proj_ensembl_havana_transcript",
                         "proj_ensembl_havana_lincrna", "proj_havana_ig_gene",
                         "ensembl_havana_ig_gene", "proj_ensembl_havana_ig_gene",
                         "OTTT" );

		// --------------------------------
		// other pairs here

		// --------------------------------

		return result;

	} // run

	// --------------------------------------------------------------------------

	private boolean checkAnalysisAndSource(DatabaseRegistryEntry dbre, String objectType, String analysis1, String analysis2, String analysis3, String analysis4, String analysis5, String analysis6, String analysis7, String analysis8, String analysis9, String analysis10, String source) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String table = objectType.toLowerCase();

		// find objects with the analysis logic name that don't have any xrefs of a
		// particular type (e.g. transcripts with an analysis of 'havana' should
		// have at least one xref from 'OTTT')
		String sql = "SELECT COUNT(*) FROM "
				+ table
				+ " t, analysis a WHERE t.analysis_id=a.analysis_id AND a.logic_name like '%havana%' AND t."
				+ table
				+ "_id NOT IN (SELECT DISTINCT(ox.ensembl_id) FROM xref x, object_xref ox WHERE x.xref_id=ox.xref_id AND ox.ensembl_object_type=? AND x.display_label like '%OTT%')";

		try {

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, objectType);

			ResultSet rs = stmt.executeQuery();

			rs.first();
			int rows = rs.getInt(1);

			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, rows + " " + table + "s with source " + analysis1 + " do not have any associated xrefs of display OTT ");
			} else {
				ReportManager.correct(this, con, "All " + table + "s with analysis " + analysis1 + " have associated " + source + " xrefs");
			}

			rs.close();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		// and vice-versa - check for objects that have particular types of xref but
		// are the wrong analysis
		// e.g. all transcripts that have an OTTT xref should be of analysis type
		// 'havana' or 'ensembl_havana_transcript'.

		sql = "SELECT COUNT(DISTINCT(t." + table + "_id)) FROM xref x, object_xref ox, external_db e, " + table
				+ " t, analysis a WHERE x.xref_id=ox.xref_id AND x.external_db_id=e.external_db_id AND ox.ensembl_id=t." + table
				+ "_id AND a.logic_name not in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AND e.db_name=? AND t.analysis_id=a.analysis_id  AND ox.ensembl_object_type=?";

		try {

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, analysis1);
			stmt.setString(2, analysis2);
                        stmt.setString(3, analysis3);
                        stmt.setString(4, analysis4);
                        stmt.setString(5, analysis5);
                        stmt.setString(6, analysis6);
                        stmt.setString(7, analysis7);
                        stmt.setString(8, analysis8);
                        stmt.setString(9, analysis9);
                        stmt.setString(10, analysis10);
			stmt.setString(11, source);
			stmt.setString(12, objectType);

			ResultSet rs = stmt.executeQuery();

			rs.first();
			int rows = rs.getInt(1);

			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, rows + " " + table + "s with " + source + " xrefs do not have an analysis named " + analysis1 + ", " + analysis2 + ", " + analysis3 + ", " + analysis4 + ", " + analysis5 + ", " + analysis6 + ", " + analysis7 + ", " + analysis8 + ", " + analysis9 + " or " + analysis10);
			} else {
				ReportManager.correct(this, con, "All " + table + "s with " + source + " xrefs have analyses of type " + analysis1 + ", " + analysis2 + ", " + analysis3 + ", " + analysis4 + ", " + analysis5 + ", " + analysis6 + ", " + analysis7 + ", " + analysis8 + ", " + analysis9 + " or " + analysis10);
			}

			rs.close();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		return result;

	}

	// --------------------------------------------------------------------------

} // AnalysisXrefs

