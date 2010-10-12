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
package org.ensembl.healthcheck.testcase.mart;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the correct variation synonym dimension tables exist in the SNP mart.
 */

public class VariationSynonymDimensionTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public VariationSynonymDimensionTables() {

		setTeamResponsible("biomart");
		addToGroup("post_martbuild");
		setDescription("Check that the correct variation synonym dimension tables exist in the SNP mart.");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry martDbre) {

		boolean result = true;

		Connection martCon = martDbre.getConnection();

		// get the list of species, and find the variation synonyms in each one
		DatabaseRegistryEntry[] variationDBs = getDatabaseRegistryByPattern(".*_variation_59.*").getAll(); //XXX

		for (DatabaseRegistryEntry variationDB : variationDBs) {

			String speciesRoot = variationDB.getSpecies().getBioMartRoot();
			
			logger.finest(String.format("Getting list of variation synonyms used in %s (BioMart equivalent %s)", variationDB.getName(), speciesRoot));

			String[] sourceNames = getColumnValues(variationDB.getConnection(),"SELECT DISTINCT(name) FROM source WHERE somatic=0");
			
		
			// check that a BioMart table for each entry exists
			for (String source : sourceNames) {
				
				// if more than one "word" (space delimited), just use the first one, followed by any number
				source = source.replaceAll("^([^ ]+)[^0-9]*([0-9]*).*$", "$1$2");
				
				// and remove non-word characters
				source = source.replaceAll("\\W", "");

				// build table name and check it
				String table = String.format("%s_snp__variation_synonym_%s__dm", speciesRoot, source);
				
				if (!checkTableExists(martCon, table)) {
					
					ReportManager.problem(this, martCon, String.format("Variation named %s in species %s (%s) is missing", table, variationDB.getSpecies().toString(), speciesRoot));
					result = false;

				} else if (!tableHasRows(martCon, table)){

					ReportManager.problem(this, martCon, String.format("Variation table named %s in species %s (%s) exists but has zero rows", table, variationDB.getSpecies().toString(), speciesRoot));
					result = false;
					
				}
				
			}
			
			ReportManager.correct(this, martCon, String.format("All expected variation dimension tables from %s are present and populated", variationDB.getName()));
			
		}

		return result;

	} // run

} //  VariationSynonymDimensionTables
