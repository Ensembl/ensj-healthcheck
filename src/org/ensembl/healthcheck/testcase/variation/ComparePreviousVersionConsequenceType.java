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
import org.ensembl.healthcheck.ReportManager;

/**
 * Compare the number of transcript_variation alleles having each consequence type between the current database and the database on
 * the secondary server.
 */

public class ComparePreviousVersionConsequenceType extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionConsequenceType() {

		/*
		addToGroup("variation");
		addToGroup("variation-release");
		addToGroup("variation-post-import");
		*/
		setDescription("Compare the number of transcript_variation alleles having each consequence type status in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * Store the SQL queries in a Properties object.
	 */
	private Properties getSQLQueries() {

		// Store all the needed SQL statements in a Properties object
		Properties sqlQueries = new Properties();
		String query;

		// Query getting the structure of the transcript_variation consequence_types column
		query = "DESCRIBE transcript_variation_allele consequence_types";
		sqlQueries.setProperty("describeTranscriptVariation", query);

		// Query counting the total number of transcript_variation alleles and assigning the number to the variable @total
		query = "SELECT COUNT(*) FROM transcript_variation_allele tva INTO @total";
		sqlQueries.setProperty("countTranscriptVariations", query);

		// Query counting the number of transcript_variation for a particular consequence type
		query = "SELECT 'SET_ELEMENT', COUNT(*) FROM transcript_variation_allele tva WHERE FIND_IN_SET('SET_ELEMENT',tva.consequence_types)";
		sqlQueries.setProperty("countTranscriptVariationsByConsequence", query);

		// Query counting the proportional number of transcript_variation for a particular consequence type
		query = "SELECT 'SET_ELEMENT (percentage)', ROUND(100*COUNT(*)/@total) FROM transcript_variation_allele tva WHERE FIND_IN_SET('SET_ELEMENT',tva.consequence_types)";
		sqlQueries.setProperty("countTranscriptVariationProportionsByConsequence", query);

		// Query getting the structure of the regulatory_region_variation consequence_types column
		query = "DESCRIBE regulatory_region_variation_allele consequence_types";
		sqlQueries.setProperty("describeRegulatoryRegionVariation", query);

		// Query counting the total number of transcript_variation alleles and assigning the number to the variable @total
		query = "SELECT COUNT(*) FROM regulatory_region_variation_allele rrva INTO @total";
		sqlQueries.setProperty("countRegulatoryRegionVariations", query);

		// Query counting the number of transcript_variation for a particular consequence type
		query = "SELECT 'SET_ELEMENT', COUNT(*) FROM regulatory_region_variation_allele rrva WHERE FIND_IN_SET('SET_ELEMENT',rrva.consequence_types)";
		sqlQueries.setProperty("countRegulatoryRegionVariationsByConsequence", query);

		// Query counting the proportional number of transcript_variation for a particular consequence type
		query = "SELECT 'SET_ELEMENT (percentage)', ROUND(100*COUNT(*)/@total) FROM regulatory_region_variation_allele rrva WHERE FIND_IN_SET('SET_ELEMENT',rrva.consequence_types)";
		sqlQueries.setProperty("countRegulatoryRegionVariationProportionsByConsequence", query);

		return sqlQueries;
	}

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map<String, Integer> counts = new HashMap<String, Integer>();

		// Get all the needed SQL statements in a Properties object
		Properties sqlQueries = getSQLQueries();

		// Keep the different types we count the consequence types for in an array
		String[] types = new String[] { "TranscriptVariation", "RegulatoryRegionVariation" };

		// Wrap it up in a try-catch block
		try {
			// Do the count for each of the types
			for (int i = 0; i < types.length; i++) {

				// First, get the structure of the consequence_types
				String[] description = getRowValues(dbre.getConnection(), sqlQueries.getProperty("describe" + types[i]));

				// The second column contains the type, strip out the individual, comma-separated set elements
				String[] setElements = description[1].split("','");

				// Get the total number of rows for this type
				dbre.getConnection().createStatement().execute(sqlQueries.getProperty("count" + types[i] + "s"));

				// Loop over the set elements and count the number for each
				for (int j = 0; j < setElements.length; j++) {

					// We need to strip away any 'set(' or ')' strings from the set element
					setElements[j] = setElements[j].replaceAll("set\\('|'\\)", "");

					// Replace the 'SET_ELEMENT' placeholder with the current consequence_type and do the queries
					counts.putAll(getCountsBySQL(dbre, sqlQueries.getProperty("count" + types[i] + "sByConsequence").replaceAll("SET_ELEMENT", setElements[j])));
					counts.putAll(getCountsBySQL(dbre, sqlQueries.getProperty("count" + types[i] + "ProportionsByConsequence").replaceAll("SET_ELEMENT", setElements[j])));
				}
			}
		} catch (Exception e) {
			ReportManager.problem(this, dbre.getConnection(), "Exception occured during healthcheck: " + e.toString());
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
