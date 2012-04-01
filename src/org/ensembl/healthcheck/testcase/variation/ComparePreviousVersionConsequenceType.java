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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Compare the number of transcript_variation alleles having each consequence type between the current database and the database on
 * the secondary server.
 */

public class ComparePreviousVersionConsequenceType extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionConsequenceType() {

		addToGroup("variation");
		addToGroup("variation-release");
		
		setDescription("Compare the number of transcript_variations having each consequence type status in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * Store the SQL queries in a Properties object.
	 */
	private Properties getSQLQueries() {

		// Store all the needed SQL statements in a Properties object
		Properties sqlQueries = new Properties();
		String query;

		// Query getting the structure of the consequence_types column
		query = "DESCRIBE transcript_variation consequence_types";
		sqlQueries.setProperty("describeConsequenceTypes", query);

		// Query counting the number of transcript variations with a particular consequence type
		query = "SELECT SET_ELEMENT, COUNT(*) FROM transcript_variation tv WHERE FIND_IN_SET(SET_ELEMENT,tv.consequence_types)";
		sqlQueries.setProperty("countVariationsByConsequenceType", query);

		return sqlQueries; 
	}

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map<String, Integer> counts = new HashMap<String, Integer>();
		Connection con = dbre.getConnection();

		try {
			// Get all the needed SQL statements in a Properties object
			Properties sqlQueries = getSQLQueries();
	
			// First, get the structure of the consequence_types
			String[] description = DBUtils.getRowValues(dbre.getConnection(), sqlQueries.getProperty("describeConsequenceTypes"));
	
			// The second column contains the type, strip out the individual, comma-separated set elements
			String[] setElements = description[1].split(",");
	
			// Loop over the set elements and count the number for each
			for (int i = 0; i < setElements.length; i++) {
	
				// We need to strip away any 'set(' or ')' strings from the set element
				setElements[i] = setElements[i].replaceAll("set\\(|\\)", "");
	
				// Replace the 'SET_ELEMENT' placeholder with the current consequence_type and do the query
				counts.putAll(getCountsBySQL(dbre, sqlQueries.getProperty("countVariationsByConsequenceType").replaceAll("SET_ELEMENT", setElements[i])));
			}
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
		}
		
		return counts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "consequence type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.95;

	}

	// ------------------------------------------------------------------------

} // ComparePreviousVersionConsequenceType
