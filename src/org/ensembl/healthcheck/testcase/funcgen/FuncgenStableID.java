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


package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class FuncgenStableID extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of StableID.
	 */
	public FuncgenStableID() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		//setHintLongRunning(true);// ?Only take about 10 mins for mouse
		setTeamResponsible(Team.FUNCGEN);

		setDescription("Checks regulatory_feature stable_id fields are valid.");
		setPriority(Priority.RED);
		setEffect("RegulatoryFeatures will not have valid stable IDs.");
		setFix("Re-run stable ID mapping or fix manually.");
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);
		//add COMPARA here?
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		//boolean result = true;

		Connection con = dbre.getConnection();

		//result &= checkStableIDs(con, "regulatory_feature");


		//No need for any of this prefix stuff as we only store ints
		//Keep just in case of future 'imports'

		// there are several species where ID mapping is not done
		//Species s = dbre.getSpecies();
		//if (s != Species.CAENORHABDITIS_ELEGANS && s != Species.DROSOPHILA_MELANOGASTER &&
		//	s != Species.SACCHAROMYCES_CEREVISIAE && s != Species.ANOPHELES_GAMBIAE) {
		//	result &= checkPrefixes(dbre);
		//	result &= checkStableIDEventTypes(con);
		//}

		//result = checkStableIDTimestamps(con);
		
		//return result;


		//Just merge this with checkStableIDs?

		return checkStableIDs(con, "regulatory_feature");
	}

	/**
	 * Checks that the typeName_stable_id table is valid. The table is valid if it
	 * has >0 rows, and there are no orphan references between typeName table and
	 * typeName_stable_id. Also prints some example data from the
	 * typeName_stable_id table via ReportManager.info().
	 * 
	 * @param con
	 *          connection to run queries on.
	 * @param typeName
	 *          name of the type to check, e.g. "exon"
	 * @return true if the table and references are valid, otherwise false.
	 */
	public boolean checkStableIDs(Connection con, String typeName) {

		boolean result = true;

		String stableIDtable = typeName; // + "_stable_id";
		//WARNING THis is not truly generic as we always check for regulatory type in feature_set
		//Need to change this if we ever want to check other stable IDs
		
		//Need to do this for each DISPLAYABLE regulatory feature_set
		//String[] fsetIDs = getColumnValues(conn, "SELECT feature_set_id from feature_set where type='regulatory'");
		
		//This would be better with a ResultSet as we want the nametoo

		try {
			Statement stmt = con.createStatement();
			String     sql = "SELECT feature_set_id, name from feature_set where type='regulatory'";
			
			//System.out.println("Executing " + sql);
			ResultSet fsetIdNames = stmt.executeQuery(sql);

		

			
			while (fsetIdNames != null && fsetIdNames.next()) {
				sql = "SELECT count(stable_id) from regulatory_feature where " + 
					"stable_id is not NULL and feature_set_id=" + fsetIdNames.getString(1);

				//System.out.println("Executing " + sql);
				int nStableIDs = DBUtils.getRowCount(con, sql);

						
				//We could really do with a HC to check we have RegFeats on all main Chrs

				if (nStableIDs < 1) {
					ReportManager.problem(this, con, stableIDtable + 
										  " contains no valid stable_ids for FeatureSet:\t" + fsetIdNames.getString(2));
					result = false;
				}
				else{
				
					nStableIDs = DBUtils.getRowCount(con, "SELECT count(stable_id) from regulatory_feature where stable_id " + 
											 "is NULL and feature_set_id=" + fsetIdNames.getString(1));


					if (nStableIDs > 0) {
						ReportManager.problem(this, con, stableIDtable + " contains " + nStableIDs + 
											  " NULL stable_ids for FeatureSet:\t" + fsetIdNames.getString(2));
						result = false;
					}
					else{
						ReportManager.correct(this, con, "No NULL stable_ids for FeatureSet " + fsetIdNames.getString(2));
					}
				}



				//Test for duplicates within set
				//Could remove this if there is a unique feature_set_id, stble_id key?
				int duplicates = DBUtils.getRowCount(con, "SELECT COUNT(stable_id)-COUNT(DISTINCT stable_id) FROM " + 
											 stableIDtable + " WHERE feature_set_id=" + fsetIdNames.getString(1));
				
				if (duplicates > 0) {
					ReportManager.problem(this, con, stableIDtable + " has " + duplicates + 
										  " duplicate stable IDs for FeatureSet:\t" + fsetIdNames.getString(2));
					result = false;
				} else {
					ReportManager.correct(this, con, "No duplicate stable IDs for FeatureSet:\t" + fsetIdNames.getString(2));
				}
		
				


				// check for invalid or missing stable ID versions
				//int nInvalidVersions = DBUtils.getRowCount(con, "SELECT COUNT(*) AS " + typeName + "_with_invalid_version" + " FROM " + stableIDtable
				//								   + " WHERE version < 1 OR version IS NULL;");
				//
				//if (nInvalidVersions > 0) {
				//	ReportManager.problem(this, con, "Invalid " + typeName + " versions in " + stableIDtable);
				//	DBUtils.printRows(this, con, "SELECT DISTINCT(version) FROM " + stableIDtable);
				//	result = false;
				//}

				// make sure stable ID versions in the typeName_stable_id table matches those in stable_id_event
				// for the latest mapping_session
                //String mappingSessionId = DBUtils.getRowColumnValue(con, "SELECT mapping_session_id FROM mapping_session " +
				//"ORDER BY created DESC LIMIT 1");

                //if (mappingSessionId.equals("")) {
                // ReportManager.info(this, con, "No mapping_session found");
                //  return result;
                //}
		
				//int nVersionMismatch = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM stable_id_event sie, " + stableIDtable +
				//								   " si WHERE sie.mapping_session_id = " + Integer.parseInt(mappingSessionId) +
				//								   " AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");
				
				//if (nVersionMismatch > 0) {
				//	ReportManager.problem(this, con, "Version mismatch between " + nVersionMismatch + " " + typeName + " versions in " +
				//						  stableIDtable + " and stable_id_event");
				//	DBUtils.printRows(this, con, "SELECT si.stable_id FROM stable_id_event sie, " + stableIDtable +
				//					  " si WHERE sie.mapping_session_id = " + Integer.parseInt(mappingSessionId) +
				//					  " AND sie.new_stable_id = si.stable_id AND sie.new_version <> si.version");
				//	result = false;
			}
			
		}catch (SQLException e){
			e.printStackTrace();
		}


		return result;
	}

	// -----------------------------------------------------------
	/**
	 * Check that all stable IDs in the table have the correct prefix. The prefix
	 * is defined in Species.java
	 */
	private boolean checkPrefixes(DatabaseRegistryEntry dbre) {
		
		boolean result = true;

		Connection con = dbre.getConnection();

		Map<String,String> tableToLetter = new HashMap<>();
		tableToLetter.put("gene", "G");
		tableToLetter.put("transcript", "T");
		tableToLetter.put("translation", "P");
		tableToLetter.put("exon", "E");

		Iterator<String> it = tableToLetter.keySet().iterator();
		while (it.hasNext()) {

			String type = (String) it.next();
			String table = type + "_stable_id";

			String prefix = DatabaseRegistryEntry.getStableIDPrefixForSpecies(dbre.getSpecies());
			if (prefix == null || prefix == "") {
				ReportManager.problem(this, con, "Can't get stable ID prefix for " + dbre.getSpecies().toString());
				result = false;
			} else {
				if (prefix.equalsIgnoreCase("IGNORE")) {
					return true;
				}
				String prefixLetter = prefix + (String) tableToLetter.get(type);
				int wrong = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE stable_id NOT LIKE '" + prefixLetter + "%'");
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
//	private boolean checkStableIDEventTypes(Connection con) {
//
//		boolean result = true;
//
//		String[] types = { "gene", "transcript", "translation" };
//
//		for (int i = 0; i < types.length; i++) {
//
//			String type = types[i];
//
//			String prefix = getPrefixForType(con, type);
//
//			String sql = "SELECT COUNT(*) FROM stable_id_event WHERE (old_stable_id LIKE '" + prefix + "%' OR new_stable_id LIKE '"
//					+ prefix + "%') AND type != '" + type + "'";
//
//			int rows = DBUtils.getRowCount(con, sql);
//
//			if (rows > 0) {
//
//				ReportManager.problem(this, con, rows + " rows of type " + type + " (prefix " + prefix
//						+ ") in stable_id_event have identifiers that do not correspond to " + type + "s");
//				result = false;
//
//			} else {
//
//				ReportManager.correct(this, con, "All types in stable_id_event correspond to identifiers");
//
//			}
//		}
//		return result;
//
//	}
//
//	// -----------------------------------------------------------
//
//	private String getPrefixForType(Connection con, String type) {
//
//		String prefix = "";
//
//		// hope the first row of the _type_stable_id table is correct
//		String stableID = DBUtils.getRowColumnValue(con, "SELECT stable_id FROM " + type + "_stable_id LIMIT 1");
//
//		prefix = stableID.replaceAll("[0-9]", "");
//
//		if (prefix.equals("")) {
//			System.err.println("Error, can't get prefix for " + type + " from stable ID " + stableID);
//		}
//
//		return prefix;
//
//	} 
////-----------------------------------------------------------
//	/**
//
//	 * 
//	 */
//	private boolean checkStableIDTimestamps(Connection con) {
//
//		boolean result = true;
//
//		String[] types = { "gene", "transcript", "translation" };
//
//		for (int i = 0; i < types.length; i++) {
//
//			String table = types[i] + "_stable_id";
//
//			String sql = "SELECT COUNT(*) FROM " + table + " WHERE created_date=0 OR modified_date=0";
//
//			int rows = DBUtils.getRowCount(con, sql);
//
//			if (rows > 0) {
//
//				ReportManager.problem(this, con, rows + " rows in " + table + " have created or modified dates of 0000-00-00 00:00:00");
//				result = false;
//
//			} else {
//
//				ReportManager.correct(this, con, "All entries in " + table + " have valid created/modified timestamps");
//
//			}
//		}
//		return result;
//
//	}

}
