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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that certain tables for protein features and object xrefs exist in the Vega Biomart.
 */

public class VegaMartDimensionTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public VegaMartDimensionTables() {

		setTeamResponsible("biomart");
		addToGroup("post_martbuild");
		setDescription("Check that certain tables for protein features and object xrefs exist in the Vega Biomart.");

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

		DatabaseRegistryEntry[] vegaDBs = getDatabaseRegistryByPattern(".*_vega_.*").getAll();

		for (DatabaseRegistryEntry vegaDB : vegaDBs) {

			if (vegaDB.getSpecies().equals(Species.UNKNOWN)) {
				continue;
			}

			String speciesRoot = vegaDB.getSpecies().getBioMartRoot();

			// ------------------------------
			// Check protein feature analysis names
			logger.finest(String.format("Getting list of protein feature analysis logic names used in %s (BioMart equivalent %s)", vegaDB.getName(), speciesRoot));

			String[] logicNames = getColumnValues(vegaDB.getConnection(),
					"SELECT DISTINCT(REPLACE(analysis.logic_name,'-','_')) FROM protein_feature LEFT JOIN analysis ON (protein_feature. analysis_id = analysis.analysis_id)");

			// check that a BioMart table for each entry exists
			for (String logicName : logicNames) {

				String tableName = String.format("%s_gene_vega__protein_feature_%s__dm", speciesRoot, logicName);

				if (!checkTableExists(martCon, tableName)) {

					ReportManager.problem(this, martCon, String.format("protein_feature dimension table named %s in species %s (%s) is missing", tableName, vegaDB.getSpecies().toString(), speciesRoot));
					result = false;

				} else if (!tableHasRows(martCon, tableName)) {

					ReportManager.problem(this, martCon, String.format("protein_feature dimension table named %s in species %s (%s) exists but has zero rows", tableName, vegaDB.getSpecies().toString(),
							speciesRoot));
					result = false;

				}

			}

			ReportManager.correct(this, martCon, String.format("All expected protein_feature dimension tables from %s are present and populated", vegaDB.getName()));

			// ------------------------------
			// Check external_db names
			logger.finest(String.format("Getting list of external_db names used in %s (BioMart equivalent %s)", vegaDB.getName(), speciesRoot));

			String[] externalDBNames = getColumnValues(
					vegaDB.getConnection(),
					"SELECT DISTINCT(REPLACE(external_db.db_name,'/','')) FROM object_xref LEFT JOIN gene ON (gene.gene_id = object_xref.ensembl_id) LEFT JOIN xref ON (object_xref.xref_id = xref.xref_id) LEFT JOIN external_db ON (xref.external_db_id = external_db.external_db_id)");

			// check that a BioMart table for each entry exists
			for (String externalDBName : externalDBNames) {

				String tableName = String.format("%s_gene_vega__ox_%s__dm", speciesRoot, externalDBName);

				if (!checkTableExists(martCon, tableName)) {

					ReportManager.problem(this, martCon, String.format("external_db dimension table named %s in species %s (%s) is missing", tableName, vegaDB.getSpecies().toString(), speciesRoot));
					result = false;

				} else if (!tableHasRows(martCon, tableName)) {

					ReportManager.problem(this, martCon, String
							.format("external_db dimension table named %s in species %s (%s) exists but has zero rows", tableName, vegaDB.getSpecies().toString(), speciesRoot));
					result = false;

				}

			}

			ReportManager.correct(this, martCon, String.format("All expected ox dimension tables from %s are present and populated", vegaDB.getName()));

		}

		return result;

	} // run

} // VegaMartDimensionTables
