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

package org.ensembl.healthcheck.testcase;

import java.sql.*;

import org.ensembl.healthcheck.*;

import org.ensembl.healthcheck.util.*;

/**
 * Checks the metadata table to make sure it is OK.
 */
public class CheckMetaDataTableTestCase extends EnsTestCase {

	// these should be updated as necessary
	private static final String[] validPrefixes = { "RGSC", "DROM", "ZFISH", "FUGU", "MOZ", "CEL", "CBR", "MGSC", "NCBI", "NCBIM" };

	// these should be updated as necessary; an up-to-date list can be found in
	// ensembl-compara/sql/taxon.txt
	private static final String[] validTaxonIDs = { "9606", "10090", "10116", "31033", "7165", "7227", "6239", "6238", "7955" };

	/**
	 * Creates a new instance of CheckMetaDataTableTestCase
	 */
	public CheckMetaDataTableTestCase() {
		addToGroup("post_genebuild");
		setDescription(
			"Check that the meta table exists, has data, the entries correspond to the "
				+ "database name, and that the values in assembly.type match what's in the meta table");
	}

	/**
	 * Check various aspects of the meta table.
	 * @return Result.
	 */
	public TestResult run() {

		boolean result = true;

		DatabaseConnectionIterator it = getDatabaseConnectionIterator();

		while (it.hasNext()) {

			Connection con = (Connection)it.next();

			String dbName = DBUtils.getShortDatabaseName(con);

			// ----------------------------------------
			// Check that the meta table exists
			if (!checkTableExists(con, "meta")) {
				result = false;
				//logger.severe(dbName + " does not have a meta table!");
				ReportManager.problem(this, con, "Meta table not present");
			} else {
				ReportManager.correct(this, con, "Meta table present");
			}

			// ----------------------------------------
			// check meta table has > 0 rows
			int rows = countRowsInTable(con, "meta");
			if (rows == 0) {
				result = false;
				//warn(con, " has empty meta table");
				ReportManager.problem(this, con, "meta table is empty");
			} else {
				ReportManager.correct(this, con, "meta table has data");
			}

			// ----------------------------------------
			// check that there are species, classification and taxonomy_id entries
			String[] meta_keys = { "assembly.default", "species.classification", "species.common_name", "species.taxonomy_id" };
			for (int i = 0; i < meta_keys.length; i++) {
				String meta_key = meta_keys[i];
				rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='" + meta_key + "'");
				if (rows == 0) {
					result = false;
					//warn(con, "No entry in meta table for " + meta_key);
					ReportManager.problem(this, con, "No entry in meta table for " + meta_key);
				} else {
					ReportManager.correct(this, con, meta_key + " entry present");
				}
			}
			// ----------------------------------------
			// Use an AssemblyNameInfo object to get te assembly information
			AssemblyNameInfo assembly = new AssemblyNameInfo(con);

			String metaTableAssemblyDefault = assembly.getMetaTableAssemblyDefault();
			logger.finest("assembly.default from meta table: " + metaTableAssemblyDefault);
			String dbNameAssemblyVersion = assembly.getDBNameAssemblyVersion();
			logger.finest("Assembly version from DB name: " + dbNameAssemblyVersion);
			String metaTableAssemblyVersion = assembly.getMetaTableAssemblyVersion();
			logger.finest("meta table assembly version: " + metaTableAssemblyVersion);
			String metaTableAssemblyPrefix = assembly.getMetaTableAssemblyPrefix();
			logger.finest("meta table assembly prefix: " + metaTableAssemblyPrefix);

			if (metaTableAssemblyVersion == null
				|| metaTableAssemblyDefault == null
				|| metaTableAssemblyPrefix == null
				|| dbNameAssemblyVersion == null) {

				ReportManager.problem(this, con, "Cannot get all information from meta table - check for null values");

			} else {

				if (!metaTableAssemblyVersion.equalsIgnoreCase(dbNameAssemblyVersion)) {
					result = false;
					//warn(con, "Database name assembly version (" + dbNameAssemblyVersion + ") does not match meta table assembly version (" + metaTableAssemblyVersion + ").");
					ReportManager.problem(
						this,
						con,
						"Database name assembly version ("
							+ dbNameAssemblyVersion
							+ ") does not match meta table assembly version ("
							+ metaTableAssemblyVersion
							+ ")");
				} else {
					ReportManager.correct(this, con, "Assembly version in database name matches assembly version in meta table");
				}

				// ----------------------------------------
				// Check that assembly prefix is one of the correct ones
				boolean member = false;
				for (int i = 0; i < validPrefixes.length; i++) {
					if (metaTableAssemblyPrefix.equalsIgnoreCase(validPrefixes[i])) {
						member = true;
					}
				}
				if (!member) {
					result = false;
					//warn(con, "Assembly prefix (" + metaTableAssemblyPrefix + ") is not valid");
					ReportManager.problem(this, con, "Assembly prefix (" + metaTableAssemblyPrefix + ") is not valid");
				} else {
					ReportManager.correct(this, con, "Meta table assembly prefix (" + metaTableAssemblyPrefix + ") is valid");
				}
			}
			// ----------------------------------------
			// Check that species.classification matches database name

			String[] metaTableSpeciesGenusArray =
				getColumnValues(
					con,
					"SELECT LCASE(meta_value) FROM meta WHERE meta_key='species.classification' ORDER BY meta_id LIMIT 2");
			// if all is well, metaTableSpeciesGenusArray should contain the species and genus (in that order) from the meta table

			if (metaTableSpeciesGenusArray != null
				&& metaTableSpeciesGenusArray.length == 2
				&& metaTableSpeciesGenusArray[0] != null
				&& metaTableSpeciesGenusArray[1] != null) {

				String[] dbNameGenusSpeciesArray = dbName.split("_");
				String dbNameGenusSpecies = dbNameGenusSpeciesArray[0] + "_" + dbNameGenusSpeciesArray[1];
				String metaTableGenusSpecies = metaTableSpeciesGenusArray[1] + "_" + metaTableSpeciesGenusArray[0];
				logger.finest("Classification from DB name:" + dbNameGenusSpecies + " Meta table: " + metaTableGenusSpecies);
				if (!dbNameGenusSpecies.equalsIgnoreCase(metaTableGenusSpecies)) {
					result = false;
					//warn(con, "Database name does not correspond to species/genus data from meta table");
					ReportManager.problem(this, con, "Database name does not correspond to species/genus data from meta table");
				} else {
					ReportManager.correct(this, con, "Database name corresponds to species/genus data from meta table");
				}

			} else {
				//logger.warning("Cannot get species information from meta table");
				ReportManager.problem(this, con, "Cannot get species information from meta table");
			}

			// ------------------------------------------
			// Check that all values in assembly.type match what's in the meta table

			rows = checkColumnValue(con, "assembly", "type", metaTableAssemblyDefault);
			if (rows > 0) {
				result = false;
				//warn(con, "Not all values in assembly.type match assembly.default in meta table");
				ReportManager.problem(this, con, "Not all values in assembly.type match assembly.default in meta table");
			} else {
				ReportManager.correct(this, con, "All values in assembly.type match assembly.default in meta table");
			}

			// -------------------------------------------
			// Check that the taxonomy ID matches a known one. Note this will not catch cases
			// where the taxonomy ID is valid for some other species!
			// The array validTaxonIDs holds the "correct" ones - see top of this file
			String dbTaxonID = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='species.taxonomy_id'");
			logger.finest("Taxonomy ID from database: " + dbTaxonID);
			if (Utils.stringInArray(dbTaxonID, validTaxonIDs, false)) {
				ReportManager.correct(this, con, "Taxonomy ID " + dbTaxonID + " is in list of valid IDs");
			} else {
				ReportManager.problem(this, con, "Taxonomy ID " + dbTaxonID + " is not list of valid IDs");
			}

		} // while connection

		// ------------------------------------------
		// Check meta table species, classification and taxonomy_id is the same
		// in all DBs for each species
		String[] species = getListOfSpecies();

		for (int i = 0; i < species.length; i++) {

			String speciesRegexp = species[i] + CORE_DB_REGEXP;
			logger.info("Checking meta tables in " + speciesRegexp);
			;

			boolean allMatch =
				checkSameSQLResult(
					"SELECT LCASE(meta_value) FROM meta WHERE meta_key LIKE \'species.%' ORDER BY meta_id",
					speciesRegexp);
			if (!allMatch) {
				result = false;
				ReportManager.problem(this, "all databases", "meta information not the same for all " + "all databases");
			} else {
				ReportManager.correct(this, "all databases", "meta information is the same for all " + species[i] + " databases");
			}

		} // foreach species

		return new TestResult(getShortTestName(), result);

	} // run

} // CheckMetaDataTableTestCase
