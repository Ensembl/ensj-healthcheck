/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check various things about ID mapping-related tables.
 */
public class MappingSession extends SingleDatabaseTestCase {

	// historical names to ignore when doing format checking
	private String[] ignoredNames = { "homo_sapiens_core_120" };

	/**
	 * Create a new MappingSession healthcheck.
	 */
	public MappingSession() {

		setDescription("Checks the mapping session and stable ID tables.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test - check the ID mapping-related tables.
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		// there are several species where ID mapping is not done
		String s = dbre.getSpecies();
		if (!s.equals(DatabaseRegistryEntry.CAENORHABDITIS_ELEGANS)
				&& !s.equals(DatabaseRegistryEntry.DROSOPHILA_MELANOGASTER)
				&& !s.equals(DatabaseRegistryEntry.SACCHAROMYCES_CEREVISIAE)) {

			Connection con = dbre.getConnection();

			logger.fine("Checking tables exist and are populated");
			result &= checkTablesExistAndPopulated(dbre);
			logger.fine("Checking DB name format in mapping_session");
			result &= checkDBNameFormat(con);
			// logger.fine("Checking mapping_session chaining");
			// result &= checkMappingSessionChaining(con);
			logger.fine("Checking mapping_session old_release and new_release values");
			result &= checkOldAndNewReleases(con);
			logger.fine("Checking for duplicates in stable_id_event");
			result &= checkStableIdEventDuplicates(con);
		}

		return result;
	} // run

	// -----------------------------------------------------------------
	/**
	 * Check format of old/new DB names in mapping_session.
	 */
	private boolean checkDBNameFormat(final Connection con) {

		boolean result = true;
		String dbNameRegexp = "[A-Za-z]+_[A-Za-z]+_(core|est|estgene|vega)_\\d+_\\d+[A-Za-z]?.*";

		String[] sql = { "SELECT old_db_name from mapping_session WHERE old_db_name <> 'ALL'",
				"SELECT new_db_name from mapping_session WHERE new_db_name <> 'LATEST'" };

		for (int i = 0; i < sql.length; i++) {

			String[] names = DBUtils.getColumnValues(con, sql[i]);
			for (int j = 0; j < names.length; j++) {
				if (!(names[j].matches(dbNameRegexp)) && !ignoreName(names[j])) {
					ReportManager.problem(this, con, "Database name " + names[j]
							+ " in mapping_session does not appear to be in the correct format");
					result = false;
				}
			}

		}

		if (result) {
			ReportManager.correct(this, con,
					"All database names in mapping_session appear to be in the correct format");
		}

		return result;

	}

	// -----------------------------------------------------------------

	/**
	 * Checks tables exist and have >0 rows. Doesn't check population for
	 * first-build databases.
	 * 
	 * @param con
	 * @return True when all ID mapping-related tables exist and have > 0 rows.
	 * 
	 */
	private boolean checkTablesExistAndPopulated(final DatabaseRegistryEntry dbre) {

		String[] tables = new String[] { "stable_id_event", "mapping_session", "gene_archive", "peptide_archive" };

		boolean result = true;

		Connection con = dbre.getConnection();

		for (int i = 0; i < tables.length; i++) {
			String table = tables[i];
			boolean exists = DBUtils.checkTableExists(con, table);
			if (exists) {
				// gene_archive and peptide_archive can be empty
				if (table.equals("gene_archive") || table.equals("peptide_archive")) {
					continue;
				}
				if (DBUtils.countRowsInTable(con, table) == 0) {
					ReportManager.problem(this, con, "Empty table:" + table);
					result = false;
				}
			} else {
				ReportManager.problem(this, con, "Missing table:" + table);
				result = false;
			}
		}

		return result;
	}

	// -----------------------------------------------------------------
	/**
	 * Check that all mapping_sessions have new releases that are greater than the
	 * old releases.
	 */
	private boolean checkOldAndNewReleases(final Connection con) {

		boolean result = true;

		try {

			Statement stmt = con.createStatement();

			// nasty forced cast by adding 0 required since the columns are
			// VARCHARS and need to be compared lexicographically
			ResultSet rs = stmt.executeQuery(
					"SELECT mapping_session_id, old_db_name, new_db_name, old_release, new_release FROM mapping_session WHERE old_release+0 >= new_release+0");

			while (rs.next()) {

				// ignore homo_sapiens_core_18_34 -> homo_sapiens_core_18_34a
				// since this was when we didn't change numbers between releases
				if (rs.getString("old_db_name").equals("homo_sapiens_core_18_34")) {
					continue;
				}
				ReportManager.problem(this, con, "Mapping session with ID " + rs.getLong("mapping_session_id") + " ("
						+ rs.getString("old_db_name") + " -> " + rs.getString("new_db_name") + ") has a new_release ("
						+ rs.getInt("new_release") + ") that is not greater than the old release ("
						+ rs.getInt("old_release") + "). May cause problems with IDHistoryView.");
				result = false;

			}

		} catch (SQLException se) {
			se.printStackTrace();
		}

		if (result) {

			ReportManager.correct(this, con, "All new_release values are greater than old_release.");

		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Check for duplicates in the stable_id_event table
	 */
	private boolean checkStableIdEventDuplicates(final Connection con) {

		boolean result = true;

		String sql = "SELECT mapping_session_id, COUNT(*) FROM stable_id_event "
				+ "GROUP BY old_stable_id, old_version, new_stable_id, new_version, mapping_session_id, type, score "
				+ "HAVING COUNT(*) > 1";

		String[] rows = DBUtils.getColumnValues(con, sql);
		if (rows.length > 0) {
			ReportManager.problem(this, con, rows.length + " duplicates in stable_id_event");
			result = false;
		} else {
			ReportManager.correct(this, con, "No duplicates in stable_id_event");
		}

		return result;
	}

	// -----------------------------------------------------------------
	/**
	 * Certain historical names don't match the new format and should be ignored to
	 * prevent constant failures.
	 */
	private boolean ignoreName(String name) {

		for (int i = 0; i < ignoredNames.length; i++) {

			if (name.equals(ignoredNames[i])) {
				return true;
			}
		}

		return false;

	}

	// -----------------------------------------------------------------

} // MappingSession
