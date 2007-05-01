/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.testcase.Priority;

/**
 * Checks the *_stable_id tables to ensure they are populated, have no orphan
 * references, and have valid versions. Also prints some examples from the table
 * for checking by eye.
 * 
 * <p>
 * Group is <b>check_stable_ids </b>
 * </p>
 * 
 * <p>
 * To be run after the stable ids have been assigned.
 * </p>
 */
public class StableID extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of StableID.
	 */
	public StableID() {
		addToGroup("id_mapping");
		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Checks *_stable_id tables are valid.");
		setPriority(Priority.RED);
		setEffect("Compara will have invalid stable IDs.");
		setFix("Re-run stable ID mapping or fix manually.");
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= checkStableIDs(con, "exon");
		result &= checkStableIDs(con, "translation");
		result &= checkStableIDs(con, "transcript");
		result &= checkStableIDs(con, "gene");

		result &= checkStableIDEventTypes(con);

		result &= checkPrefixes(dbre);

		return result;
	}

	/**
	 * Checks that the typeName_stable_id table is valid. The table is valid if it
	 * has >0 rows, and there are no orphan references between typeName table and
	 * typeName_stable_id. Also prints some example data from the
	 * typeName_stable_id table via ReportManager.info().
	 * 
	 * @param con
	 *          connection to run quries on.
	 * @param typeName
	 *          name of the type to check, e.g. "exon"
	 * @return true if the table and references are valid, otherwise false.
	 */
	public boolean checkStableIDs(Connection con, String typeName) {

		boolean result = true;

		String stableIDtable = typeName + "_stable_id";
		int nStableIDs = countRowsInTable(con, stableIDtable);
		// ReportManager.info(this, con, "Num " + typeName + "s stable ids = " +
		// nStableIDs);

		if (nStableIDs < 1) {
			ReportManager.problem(this, con, stableIDtable + " table is empty.");
			result = false;
		}

		// look for orphans between type and type_stable_id tables
		int orphans = countOrphans(con, typeName, typeName + "_id", stableIDtable, typeName + "_id", false);
		if (orphans > 0) {
			ReportManager.problem(this, con, "Orphan references between " + typeName + " and " + typeName + "_stable_id tables.");
			result = false;
		}

		// check for duplicate stable IDs (will be redundant when stable ID columns
		// get a UNIQUE constraint)
		// to find which records are duplicated use
		// SELECT exon_id, stable_id, COUNT(*) FROM exon_stable_id GROUP BY
		// stable_id HAVING COUNT(*) > 1;
		// this will give the internal IDs for *one* of each of the duplicates
		// if there are only a few then reassign the stable IDs of one of the
		// duplicates
		int duplicates = getRowCount(con, "SELECT COUNT(stable_id)-COUNT(DISTINCT stable_id) FROM " + stableIDtable);
		if (duplicates > 0) {
			ReportManager.problem(this, con, stableIDtable + " has " + duplicates + " duplicate stable IDs (versions not checked)");
			result = false;
		} else {
			ReportManager.correct(this, con, "No duplicate stable IDs in " + stableIDtable);
		}
		
		// check for invalid or missing stable ID versions
		int nInvalidVersions = getRowCount(con, "SELECT COUNT(*) AS " + typeName + "_with_invalid_version" + " FROM " + stableIDtable
				+ " WHERE version < 1 OR version IS NULL;");

		if (nInvalidVersions > 0) {
			ReportManager.problem(this, con, "Invalid " + typeName + " versions in " + stableIDtable);
			DBUtils.printRows(this, con, "SELECT DISTINCT(version) FROM " + stableIDtable);
			result = false;
		}

		// make sure stable ID versions in the typeName_stable_id table matches those in stable_id_event
		// for the latest mapping_session
		String mappingSessionId = getRowColumnValue(con, "SELECT mapping_session_id FROM mapping_session " +
			"ORDER BY created DESC LIMIT 1");
		
		int nVersionMismatch = getRowCount(con, "SELECT COUNT(*) FROM stable_id_event sie, " + stableIDtable +
			" si WHERE sie.mapping_session_id = " + Integer.parseInt(mappingSessionId) +
			" AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");

		if (nVersionMismatch > 0) {
			ReportManager.problem(this, con, "Version mismatch between " + nVersionMismatch + " " + typeName + " versions in " +
				stableIDtable + " and stable_id_event");
			DBUtils.printRows(this, con, "SELECT si.stable_id FROM stable_id_event sie, " + stableIDtable +
			    " si WHERE sie.mapping_session_id = " + Integer.parseInt(mappingSessionId) +
			    " AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");
			result = false;
		}

		return result;
	}

	// -----------------------------------------------------------
	/**
	 * Check that all stable IDs in the table have the correct prefix. The prefix
	 * is defined by the stableid.prefix value in the meta table.
	 */
	private boolean checkPrefixes(DatabaseRegistryEntry dbre) {
		
		boolean result = true;

		Connection con = dbre.getConnection();

		Map tableToLetter = new HashMap();
		tableToLetter.put("gene", "G");
		tableToLetter.put("transcript", "T");
		tableToLetter.put("translation", "P");
		tableToLetter.put("exon", "E");

		Iterator it = tableToLetter.keySet().iterator();
		while (it.hasNext()) {

			String type = (String) it.next();
			String table = type + "_stable_id";

			String prefix = Species.getStableIDPrefixForSpecies(dbre.getSpecies(), dbre.getType());
			if (prefix == null || prefix == "") {
				ReportManager.problem(this, con, "Can't get stable ID prefix for " + dbre.getSpecies().toString() + " - please add to Species.java");
				result = false;
			} else {
				if (prefix.equalsIgnoreCase("IGNORE")) {
					return true;
				}
				String prefixLetter = prefix + (String) tableToLetter.get(type);
				int wrong = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE stable_id NOT LIKE '" + prefixLetter + "%'");
				if (wrong > 0) {
					ReportManager.problem(this, con, wrong + " rows in " + table + " do not have the correct (" + prefixLetter + ") prefix");
					result = false;
				} else {
					ReportManager.correct(this, con, "All rows in " + table + " have the correct prefix (" + prefixLetter + ")");
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

		String[] types = { "gene", "transcript", "translation" };

		for (int i = 0; i < types.length; i++) {

			String type = types[i];

			String prefix = getPrefixForType(con, type);

			String sql = "SELECT COUNT(*) FROM stable_id_event WHERE (old_stable_id LIKE '" + prefix + "%' OR new_stable_id LIKE '"
					+ prefix + "%') AND type != '" + type + "'";

			int rows = getRowCount(con, sql);

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " rows of type " + type + " (prefix " + prefix
						+ ") in stable_id_event have identifiers that do not correspond to " + type + "s");
				result = false;

			} else {

				ReportManager.correct(this, con, "All types in stable_id_event correspond to identifiers");

			}
		}
		return result;

	}

	// -----------------------------------------------------------

	private String getPrefixForType(Connection con, String type) {

		String prefix = "";

		// hope the first row of the _type_stable_id table is correct
		String stableID = getRowColumnValue(con, "SELECT stable_id FROM " + type + "_stable_id LIMIT 1");

		prefix = stableID.replaceAll("[0-9]", "");

		if (prefix.equals("")) {
			System.err.println("Error, can't get prefix for " + type + " from stable ID " + stableID);
		}

		return prefix;

	} // -----------------------------------------------------------

}
