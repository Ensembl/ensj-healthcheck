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
package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.Team;


/**
 * Compare the number of variations having each evidence between the current database and the database on the secondary
 * server.
 */

public class ComparePreviousVersionValidationStatus extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionValidationStatus() {

		setDescription("Compare the number of variations having each an evidence annotation in the current database with those from the equivalent database on the secondary server");
                setTeamResponsible(Team.VARIATION);
	}

	/**
	 * Store the SQL queries in a Properties object.
	 */
	private Properties getSQLQueries() {

		// Store all the needed SQL statements in a Properties object
		Properties sqlQueries = new Properties();
		String query;

		// Query getting the structure of the evidence column
		query = "DESCRIBE variation evidence_attribs";
		sqlQueries.setProperty("describeEvidence", query);

		// Query counting the number of variations with a particular evidence
		query = "SELECT SET_ELEMENT, COUNT(*) FROM variation v WHERE FIND_IN_SET(SET_ELEMENT,evidence_attribs)";
		sqlQueries.setProperty("countVariationsByEvidence", query);

		return sqlQueries;
	} 

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map<String, Integer> counts = new HashMap<String, Integer>();
		Connection con = dbre.getConnection();

		try {
			// Get all the needed SQL statements in a Properties object
			Properties sqlQueries = getSQLQueries();
	
			// First, get the structure of the evidence
			String[] description = DBUtils.getRowValues(dbre.getConnection(), sqlQueries.getProperty("describeEvidence"));
	
			// The second column contains the type, strip out the individual, comma-separated set elements
			String[] setElements = description[1].split(",");
	
			// Loop over the set elements and count the number for each
			for (int i = 0; i < setElements.length; i++) {
	
				// We need to strip away any 'set(' or ')' strings from the set element
				setElements[i] = setElements[i].replaceAll("set\\(|\\)", "");
	
				// Replace the 'SET_ELEMENT' placeholder with the current evidence and do the query
				counts.putAll(getCountsBySQL(dbre, sqlQueries.getProperty("countVariationsByEvidence").replaceAll("SET_ELEMENT", setElements[i])));
			}
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		return counts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "number of variations having at least one evidence annotation";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.95;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ------------------------------------------------------------------------

} // ComparePreviousVersionValidationStatus
