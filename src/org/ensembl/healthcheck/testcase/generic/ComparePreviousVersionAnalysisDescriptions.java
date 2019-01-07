/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Compare the analysis_descriptions in the current database with those from the equivalent database on the secondary server. Note
 * only certain columns are checked.
 * 
 * Note this is not comparing counts so doesn't extend ComparePreviousVersionBase.
 */

public class ComparePreviousVersionAnalysisDescriptions extends SingleDatabaseTestCase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionAnalysisDescriptions() {

		setDescription("Compare the analysis_descriptions in the current database with those from the equivalent database on the secondary server. Note only certain columns are checked.");
		setTeamResponsible(Team.GENEBUILD);
		setSecondTeamResponsible(Team.FUNCGEN);


	}

	/**
	 * This test Does not apply to sangervega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
		addAppliesToType(DatabaseType.FUNCGEN);
	}

	// ----------------------------------------------------------------------

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection currentCon = dbre.getConnection();

		DatabaseRegistryEntry previous = getEquivalentFromSecondaryServer(dbre);
		if (previous == null) {
			return result;
		}
		Connection previousCon = previous.getConnection();

		// Get data from current database, compare each one with equivalent on
		// previous
		String currentSQL = "SELECT a.logic_name, ad.display_label, ad.displayable, ad.web_data FROM analysis a, analysis_description ad WHERE a.analysis_id=ad.analysis_id";
		String previousSQL = "SELECT ad.display_label, ad.displayable, ad.web_data FROM analysis a, analysis_description ad WHERE a.analysis_id=ad.analysis_id AND a.logic_name=?";

		try {

			PreparedStatement currentStmt = currentCon.prepareStatement(currentSQL);
			PreparedStatement previousStmt = previousCon.prepareStatement(previousSQL);

			ResultSet currentRS = currentStmt.executeQuery();

			while (currentRS.next()) {

				String logicName = currentRS.getString(1);
				String currentDisplayLabel = currentRS.getString(2);
				int currentDisplayable = currentRS.getInt(3);
				String currentWebData = currentRS.getString(4);

				previousStmt.setString(1, logicName);
				ResultSet previousRS = previousStmt.executeQuery();
				if (previousRS == null) {
					continue;
				}
				if (!previousRS.next()) {
					continue;
				}
				if (currentDisplayLabel == null) {
					continue;
				}
				String previousDisplayLabel = previousRS.getString(1);
				int previousDisplayable = previousRS.getInt(2);
				String previousWebData = previousRS.getString(3);
				if (!currentDisplayLabel.equals(previousDisplayLabel)) {
					ReportManager.problem(this, currentCon, "Display label for logic name " + logicName + " differs; \ncurrent: '" + currentDisplayLabel + "' \nprevious: '" + previousDisplayLabel + "'");
					result = false;
				}

				if (currentDisplayable != previousDisplayable) {
					ReportManager.problem(this, currentCon, "Displayable flag for logic name " + logicName + " differs; \ncurrent: '" + currentDisplayable + "' \nprevious: '" + previousDisplayable + "'");
					result = false;
				}

				if (currentWebData != null && !currentWebData.equals("") && previousWebData != null && !previousWebData.equals("")) {
					// ignore whitespaces in web_data column
					String currentWebNoWhite = currentWebData.replaceAll(" ", "");
					String previousWebNoWhite = previousWebData.replaceAll(" ", "");
					// remove initial and final {} and split into a char
					String[] currentWeb = currentWebNoWhite.substring(1, currentWebNoWhite.length() - 1).split("");
					String[] previousWeb = previousWebNoWhite.substring(1, previousWebNoWhite.length() - 1).split("");
					// each position will be key=>value
					String[] currentWebKeyValue = splitWebDataString(currentWeb);
					String[] previousWebKeyValue = splitWebDataString(previousWeb);
					// the hashes to store the data
					Map<String, String> currentWebHashValueString = new HashMap<String, String>();
					Map<String, String> previousWebHashValueString = new HashMap<String, String>();
					Map<String, Map> currentWebHashValueHash = new HashMap<String, Map>();
					Map<String, Map> previousWebHashValueHash = new HashMap<String, Map>();
					// need to create the different hashes
					for (int i = 0; i < currentWebKeyValue.length; i++) {
						// first value will be the key of the hash
						if (currentWeb[i].startsWith("{")) {
							// need to return a Map<String,Map>
							createHashWebData(currentWebKeyValue[i], currentWebHashValueHash);
						} else {
							// need to return a Map<String,String>
							createHashWebData(currentWebKeyValue[i], currentWebHashValueString);
						}
					}
					for (int i = 0; i < previousWebKeyValue.length; i++) {
						if (previousWeb[i].startsWith("{")) {
							// need to return a Map<String,Map>
							createHashWebData(previousWebKeyValue[i], previousWebHashValueHash);
						} else {
							// need to return a Map<String,String>
							createHashWebData(previousWebKeyValue[i], previousWebHashValueString);
						}
					}

					// and finally compare them
					if (!currentWebHashValueString.equals(previousWebHashValueString) || !currentWebHashValueHash.equals(previousWebHashValueHash)) {
						ReportManager.problem(this, currentCon, "Web data for logic name " + logicName + " differs; \ncurrent: '" + currentWebData + "' \nprevious: '" + previousWebData + "'");
						result = false;
					}
					//
				}

				previousRS.close();

			}

			currentRS.close();

			currentStmt.close();
			previousStmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing SQL");
			e.printStackTrace();

		}

		return result;

	}

	// will create the key->value hash from the string
	// {'label_key' => '[text_label] [display_label]','default' => {'contigviewbottom' => 'transcript_label','contigviewtop' =>
	// 'gene_label','cytoview' => 'gene_label'}}
	// {'label_key' => '[text_label] [display_label]','default' => {'contigviewbottom' => 'transcript_label','contigviewtop' =>
	// 'gene_label','cytoview' => 'gene_label'}}

	private static String[] splitWebDataString(String[] webData) {

		List<String> keyValues = new ArrayList<String>();
		boolean subHash = false;
		String keyValue = "";
		// each position is one char, concatenate them until we find a ',' not between {}
		for (int i = 0; i < webData.length; i++) {
			if (webData[i].equals("{")) {
				subHash = true;
			}
			if (webData[i].equals("}")) {
				subHash = false;
			}
			// if we have a comma in a subHash, ignore
			if (webData[i].equals(",") && !subHash) {
				keyValues.add(keyValue);
				keyValue = "";
				continue;
			}
			keyValue = keyValue.concat(webData[i]);
		}
		// add last key-value pair
		keyValues.add(keyValue);
		String[] returnString = new String[keyValues.size()];
		keyValues.toArray(returnString);
		return returnString;
	}

	private static void createHashWebData(String webColumn, Map webData) {
		// split in the =>, there might be easier ways to do it
		String[] keyValue = webColumn.replaceFirst("=>", ":").split(":");

		if (keyValue.length > 1) {
			if (keyValue.length > 1 && keyValue[1].startsWith("{")) {
				// we have another hash 'default' => {'contigviewbottom' => 'transcript_label','contigviewtop' => 'gene_label','cytoview' =>
				// 'gene_label'}
				// remove {}
				String[] keyValues = keyValue[1].substring(1, keyValue[1].length() - 1).split(",");
				Map<String, String> subHash = new HashMap<String, String>();
				for (int i = 0; i < keyValues.length; i++) {
					createHashWebData(keyValues[i], subHash);
				}
				webData.put(keyValue[0], subHash);
			} else {
				webData.put(keyValue[0], keyValue[1]);
			}
		}
	}

	// ----------------------------------------------------------------------

} // ComparePreviousVersionAnalysisDescriptions

