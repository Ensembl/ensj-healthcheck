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
 * Check that there is a table (e.g. hsapiens_gene_ensembl__ox_UniprotSWISSPROT__dm ) corresponding to each species and xref type.
 */

public class XrefTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public XrefTables() {

		setTeamResponsible("biomart");
		addToGroup("post_martbuild");
		setDescription("Check that there is a table (e.g. hsapiens_gene_ensembl__ox_UniprotSWISSPROT__dm) corresponding to each species and xref type.");

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
		DatabaseRegistryEntry[] coreDBs = getDatabaseRegistryByPattern(".*_core_.*").getAll();

		for (DatabaseRegistryEntry coreDB : coreDBs) {

			String speciesRoot = coreDB.getSpecies().getBioMartRoot();
			
			logger.finest(String.format("Getting list of external DB names used in %s (BioMart equivalent %s)", coreDB.getName(), speciesRoot));

			String[] externalDBs = getColumnValues(coreDB.getConnection(),
					"SELECT DISTINCT(e.db_name) FROM external_db e, xref x, object_xref ox WHERE e.external_db_id = x.external_db_id AND ox.xref_id = x.xref_id");

			// check that a BioMart table for each entry exists
			for (String externalDB : externalDBs) {

				externalDB = externalDB.replace("/", ""); // e.g. Uniprot/SWISSPROT
				
				String tableName = String.format("%s_gene_ensembl__ox_%s__dm", speciesRoot, externalDB);

				if (!checkTableExists(martCon, tableName)) {
					
					ReportManager.problem(this, martCon, String.format("Table named %s for xref type %s in species %s (%s) is missing", tableName, externalDB, coreDB.getSpecies().toString(), speciesRoot));
					result = false;

				} else if (!tableHasRows(martCon, tableName)){

					ReportManager.problem(this, martCon, String.format("Table named %s for xref type %s in species %s (%s) exists but has zero rows", tableName, externalDB, coreDB.getSpecies().toString(), speciesRoot));
					result = false;
					
				}
				
			}
			
			ReportManager.correct(this, martCon, String.format("All expected xref dimension tables from %s are present and populated", coreDB.getName()));
			
		}

		return result;

	} // run

} // XrefTables
