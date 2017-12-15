/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks that mapped stable IDs are correct and mapping tables are correctly
 * populated
 * 
 * <p>
 * Group is <b>check_stable_ids </b>
 * </p>
 * 
 * <p>
 * To be run after the stable ids have been assigned.
 * </p>
 */
public class StableIDMapping extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of StableID.
	 */
	public StableIDMapping() {

		setDescription("Checks stable_id mapping data is valid.");
		setPriority(Priority.RED);
		setEffect("Compara will have invalid stable IDs.");
		setFix("Re-run stable ID mapping or fix manually.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// there are several species where ID mapping is not done
		String s = dbre.getSpecies();
		result &= checkPrefixes(dbre);
		if (s != null && !s.equals(DatabaseRegistryEntry.CAENORHABDITIS_ELEGANS)
				&& !s.equals(DatabaseRegistryEntry.DROSOPHILA_MELANOGASTER)
				&& !s.equals(DatabaseRegistryEntry.SACCHAROMYCES_CEREVISIAE)
				&& !s.equals(DatabaseRegistryEntry.ANOPHELES_GAMBIAE) && !s.equals(DatabaseRegistryEntry.UNKNOWN)) {
			if (dbre.getType() == DatabaseType.CORE) {// for sangervega, do not
														// check the prefixes
				result &= checkStableIDEventTypes(con);
				result = checkStableIDTimestamps(con);
			}
		}

		return result;
	}

	// -----------------------------------------------------------
	/**
	 * Check that all stable IDs in the table have the correct prefix. The prefix is
	 * defined in Species.java
	 */
	private boolean checkPrefixes(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		Map<String, String> tableToLetter = new HashMap<String, String>();
		tableToLetter.put("gene", "G");
		tableToLetter.put("transcript", "T");
		tableToLetter.put("translation", "P");
		tableToLetter.put("exon", "E");

		Iterator<String> it = tableToLetter.keySet().iterator();
		while (it.hasNext()) {

			String type = (String) it.next();
			String table = type;

			String prefix = DatabaseRegistryEntry.getStableIDPrefixForSpecies(dbre.getSpecies());
			if (prefix == null || prefix == "") {
				ReportManager.problem(this, con, "Can't get stable ID prefix for " + dbre.getSpecies().toString()
						+ " - please add to Species.java");
				result = false;
			} else {
				if (prefix.equalsIgnoreCase("IGNORE")) {
					return true;
				}
				String prefixLetter = prefix + (String) tableToLetter.get(type);
				int wrong = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE stable_id NOT LIKE '"
						+ prefixLetter + "%' AND stable_id NOT LIKE 'LRG%'");
				if (wrong > 0) {
					ReportManager.problem(this, con,
							wrong + " rows in " + table + " do not have the correct (" + prefixLetter + ") prefix");
					result = false;
				}
			}
		}

		return result;

	}

	// -----------------------------------------------------------
	/**
	 * Check for any stable ID events where the 'type' column does not match the
	 * identifier type.
	 * 
	 */
	private boolean checkStableIDEventTypes(Connection con) {

		boolean result = true;

		String[] types = { "gene", "transcript", "translation", "exon" };

		for (int i = 0; i < types.length; i++) {

			String type = types[i];

			String prefix = getPrefixForType(con, type);

			String sql = "SELECT COUNT(*) FROM stable_id_event WHERE (old_stable_id LIKE '" + prefix
					+ "%' OR new_stable_id LIKE '" + prefix + "%') AND type != '" + type + "'";

			int rows = DBUtils.getRowCount(con, sql);

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " rows of type " + type + " (prefix " + prefix
						+ ") in stable_id_event have identifiers that do not correspond to " + type + "s");
				result = false;

			}

			// check for invalid or missing stable ID versions
			int nInvalidVersions = DBUtils.getRowCount(con, "SELECT COUNT(*) AS " + type + "_with_invalid_version"
					+ " FROM " + type + " WHERE version < 1 OR version IS NULL;");

			if (nInvalidVersions > 0) {
				ReportManager.problem(this, con, "Invalid versions in " + type);
				DBUtils.printRows(this, con, "SELECT DISTINCT(version) FROM " + type);
				result = false;
			}

			// make sure stable ID versions in the typeName table matches those
			// in stable_id_event
			// for the latest mapping_session
			String mappingSessionId = DBUtils.getRowColumnValue(con,
					"SELECT mapping_session_id FROM mapping_session " + "ORDER BY created DESC LIMIT 1");

			if (mappingSessionId.equals("")) {
				ReportManager.info(this, con, "No mapping_session found");
				return result;
			}

			int nVersionMismatch = DBUtils.getRowCount(con,
					"SELECT COUNT(*) FROM stable_id_event sie, " + type + " si WHERE sie.mapping_session_id = "
							+ Integer.parseInt(mappingSessionId)
							+ " AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");

			if (nVersionMismatch > 0) {
				ReportManager.problem(this, con, "Version mismatch between " + nVersionMismatch + " " + type
						+ " versions in and stable_id_event");
				DBUtils.printRows(this, con,
						"SELECT si.stable_id FROM stable_id_event sie, " + type + " si WHERE sie.mapping_session_id = "
								+ Integer.parseInt(mappingSessionId)
								+ " AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");
				result = false;
			}

		}
		return result;

	}

	// -----------------------------------------------------------

	private String getPrefixForType(Connection con, String type) {

		String prefix = "";

		// hope the first row of the type table is correct
		String stableID = DBUtils.getRowColumnValue(con, "SELECT stable_id FROM " + type + " LIMIT 1");

		prefix = stableID.replaceAll("[0-9]", "");

		if (prefix.equals("")) {
			System.err.println("Error, can't get prefix for " + type + " from stable ID " + stableID);
		}

		return prefix;

	}

	// -----------------------------------------------------------
	/**
	
	 * 
	 */
	private boolean checkStableIDTimestamps(Connection con) {

		boolean result = true;

		String[] types = { "gene", "transcript", "translation", "exon" };

		for (int i = 0; i < types.length; i++) {

			String table = types[i];

			String sql = "SELECT COUNT(*) FROM " + table + " WHERE created_date=0 OR modified_date=0";

			int rows = DBUtils.getRowCount(con, sql);

			if (rows > 0) {

				ReportManager.problem(this, con,
						rows + " rows in " + table + " have created or modified dates of 0000-00-00 00:00:00");
				result = false;

			}
		}
		return result;

	}

}
