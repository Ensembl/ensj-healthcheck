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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * Compare the number of variations having each validation status between the current database and the database on the secondary
 * server.
 */

public class ComparePreviousVersionValidationStatus extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionValidationStatus() {

		addToGroup("variation");
		addToGroup("variation-release");
		addToGroup("variation-post-import");
		setDescription("Compare the number of variations having each validation status in the current database with those from the equivalent database on the secondary server");
	}

	/**
	 * Store the SQL queries in a Properties object.
	 */
	private Properties getSQLQueries() {

		// Store all the needed SQL statements in a Properties object
		Properties sqlQueries = new Properties();
		String query;

		// Query getting the structure of the validation_status column
		query = "DESCRIBE variation validation_status";
		sqlQueries.setProperty("describeValidationStatus", query);

		// Query counting the number of variations with a particular validation_status
		query = "SELECT SET_ELEMENT, COUNT(*) FROM variation v WHERE FIND_IN_SET(SET_ELEMENT,validation_status)";
		sqlQueries.setProperty("countVariationsByValidationStatus", query);

		return sqlQueries;
	}

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map<String, Integer> counts = new HashMap<String, Integer>();

		// Get all the needed SQL statements in a Properties object
		Properties sqlQueries = getSQLQueries();

		// First, get the structure of the validation_status
		String[] description = getRowValues(dbre.getConnection(), sqlQueries.getProperty("describeValidationStatus"));

		// The second column contains the type, strip out the individual, comma-separated set elements
		String[] setElements = description[1].split(",");

		// Loop over the set elements and count the number for each
		for (int i = 0; i < setElements.length; i++) {

			// We need to strip away any 'set(' or ')' strings from the set element
			setElements[i] = setElements[i].replaceAll("set\\(|\\)", "");

			// Replace the 'SET_ELEMENT' placeholder with the current validation_status and do the query
			counts.putAll(getCountsBySQL(dbre, sqlQueries.getProperty("countVariationsByValidationStatus").replaceAll("SET_ELEMENT", setElements[i])));
		}
		return counts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "number of variations having validation status";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.95;

	}

	// ------------------------------------------------------------------------

} // ComparePreviousVersionValidationStatus
