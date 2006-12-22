/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

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

		addToGroup("id_mapping");
		addToGroup("release");
		setDescription("Checks the mapping session and stable ID tables.");

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test - check the ID mapping-related tables.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		// there are several species where ID mapping is not done
		Species s = dbre.getSpecies();
		if (s != Species.CAENORHABDITIS_ELEGANS && s != Species.DROSOPHILA_MELANOGASTER && s != Species.SACCHAROMYCES_CEREVISIAE) {

			Connection con = dbre.getConnection();

			logger.info("Checking tables exist and are populated");
			result &= checkTablesExistAndPopulated(dbre);
			logger.info("Checking DB name format in mapping_session");
			result &= checkDBNameFormat(con);
			// logger.info("Checking mapping_session chaining");
			// result &= checkMappingSessionChaining(con);
			logger.info("Checking mapping_session old_release and new_release values");
			result &= checkOldAndNewReleases(con);
			logger.info("Checking for duplicates in stable_id_event");
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

			String[] names = getColumnValues(con, sql[i]);
			for (int j = 0; j < names.length; j++) {
				if (!(names[j].matches(dbNameRegexp)) && !ignoreName(names[j])) {
					ReportManager.problem(this, con, "Database name " + names[j]
							+ " in mapping_session does not appear to be in the correct format");
					result = false;
				}
			}

		}

		if (result) {
			ReportManager.correct(this, con, "All database names in mapping_session appear to be in the correct format");
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
		String dbName = DBUtils.getShortDatabaseName(con);
		if (!dbName.matches(".*_1[a-zA-Z]?$")) {

			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				boolean exists = checkTableExists(con, table);
				if (exists) {
					// gene_archive and peptide_archive can be empty
					if (table.equals("gene_archive") || table.equals("peptide_archive")) {
						continue;
					}
					if (countRowsInTable(con, table) == 0) {
						ReportManager.problem(this, con, "Empty table:" + table);
						result = false;
					}
				} else {
					ReportManager.problem(this, con, "Missing table:" + table);
					result = false;
				}
			}

		} else {

			logger.info(dbName + " seems to be a new genebuild, skipping table checks");

		}

		return result;
	}


	// -----------------------------------------------------------------
	/**
	 * Check that the old_db_name and new_db_name columns "chain" together.
	 */
	private boolean checkMappingSessionChaining(final Connection con) {

		boolean result = true;

		String[] oldNames = getColumnValues(con, "SELECT old_db_name FROM mapping_session WHERE old_db_name <> 'ALL' ORDER BY created");
		String[] newNames = getColumnValues(con,
				"SELECT new_db_name FROM mapping_session WHERE new_db_name <> 'LATEST' ORDER BY created");

		for (int i = 1; i < oldNames.length; i++) {
			if (!(oldNames[i].equalsIgnoreCase(newNames[i - 1]))) {
				ReportManager.problem(this, con, "Old/new names " + oldNames[i] + " " + newNames[i - 1] + " do not chain properly");
				result = false;
			}
		}

		if (result) {
			ReportManager.correct(this, con, "Old/new db name chaining in mapping_session seems OK");
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

			// nasty forced cast by adding 0 required since the columns are VARCHARS and need to be compared lexicographically
			ResultSet rs = stmt.executeQuery("SELECT mapping_session_id, old_db_name, new_db_name, old_release, new_release FROM mapping_session WHERE old_release+0 > new_release+0");

			while (rs.next()) {

				// ignore homo_sapiens_core_18_34 -> homo_sapiens_core_18_34a since this was when we didn't change numbers between releases
				if (rs.getString("old_db_name").equals("homo_sapiens_core_18_34")) {
					continue;
				}
				ReportManager.problem(this, con, "Mapping session with ID " + rs.getLong("mapping_session_id") + " (" + rs.getString("old_db_name") + " -> " + rs.getString("new_db_name") + ") has a new_release (" + rs.getInt("new_release") + ") that is not greater than the old release (" + rs.getInt("old_release") + ")");
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

        String sql = "SELECT mapping_session_id, COUNT(*) FROM stable_id_event " +
        	"GROUP BY old_stable_id, old_version, new_stable_id, new_version, mapping_session_id, type, score " +
        	"HAVING COUNT(*) > 1";

        String[] rows = getColumnValues(con, sql);
        if (rows.length > 0) {
            ReportManager.problem(this, con, rows + " duplicates in stable_id_event");
            result = false;
        }

        return result;
    }

	// -----------------------------------------------------------------
	/**
	 * Certain historical names don't match the new format and should be ignored
	 * to prevent constant failures.
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
