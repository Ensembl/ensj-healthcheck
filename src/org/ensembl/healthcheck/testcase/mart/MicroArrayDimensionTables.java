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
 * Check that there is a table (e.g. hsapiens_gene_ensembl__eFG_PHALANX_OneArray__dm) corresponding to each species and microarray type in the funcgen databases.
 */

public class MicroArrayDimensionTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public MicroArrayDimensionTables() {

		setTeamResponsible("biomart");
		addToGroup("post_martbuild");
		setDescription("Check that there is a table (e.g. hsapiens_gene_ensembl__eFG_PHALANX_OneArray__dm) corresponding to each species and microarray type in the funcgen databases.");

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

		// get the list of species, and find the xref types in each one
		DatabaseRegistryEntry[] funcgenDBs = getDatabaseRegistryByPattern(".*_funcgen_.*").getAll();

		for (DatabaseRegistryEntry funcgenDB : funcgenDBs) {

			String speciesRoot = funcgenDB.getSpecies().getBioMartRoot();
			
			logger.finest(String.format("Getting list of microarray names used in %s (BioMart equivalent %s)", funcgenDB.getName(), speciesRoot));

			String[] tables = getColumnValues(funcgenDB.getConnection(),
					String.format("SELECT DISTINCT(CONCAT('%s_gene_ensembl__eFG_',vendor,'_',REPLACE(name,'-','_'),'__dm')) FROM array WHERE format = 'EXPRESSION'", speciesRoot));

			// check that a BioMart table for each entry exists
			for (String table : tables) {
				
				if (!checkTableExists(martCon, table)) {
					
					ReportManager.problem(this, martCon, String.format("Microarray table named %s in species %s (%s) is missing", table, funcgenDB.getSpecies().toString(), speciesRoot));
					result = false;

				} else if (!tableHasRows(martCon, table)){

					ReportManager.problem(this, martCon, String.format("Microarray table named %s in species %s (%s) exists but has zero rows", table, funcgenDB.getSpecies().toString(), speciesRoot));
					result = false;
					
				}
				
			}
			
			ReportManager.correct(this, martCon, String.format("All expected microarray dimension tables from %s are present and populated", funcgenDB.getName()));
			
		}

		return result;

	} // run

} //  MicroArrayDimensionTables
