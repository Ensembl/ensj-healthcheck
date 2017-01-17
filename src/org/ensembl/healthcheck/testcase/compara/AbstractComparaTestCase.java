/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.util.HashMap;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * An extension of the SingleDatabaseTestCase that adds some Compara requirements
 *  - Some foreign keys actually link a table to itself
 *  - We need an easy way of getting the core database of each species
 */

public abstract class AbstractComparaTestCase extends SingleDatabaseTestCase {


	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if
	 * necessary.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            First column in table to check.
	 * @param col2
	 *            Second column in table to check.
	 * @param col1_can_be_null
	 *            Whether NULLs in the first column are allowed
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphansSameTable(Connection con, String table, String col1, String col2, boolean col1_can_be_null) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
			return false;
		}

		String sql = String.format(" FROM %s hc_table1 LEFT JOIN %s hc_table2 ON hc_table1.%s = hc_table2.%s WHERE hc_table2.%s IS NULL", table, table, col1, col2, col2);
		if (col1_can_be_null) {
			sql = sql + String.format(" AND hc_table1.%s IS NOT NULL", col1);
		}

		int orphans = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

		logger.finest("Orphans: " + orphans);

		if (orphans > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT hc_table1." + col1 + sql + " LIMIT 20");
			for (String this_value : values) {
				ReportManager.info(this, con, table + "." + col1 + " " + this_value + " is not linked.");
			}

			ReportManager.problem(this, con, "FAILED " + table + " : " + col1 + " -> " + col2 + " using FK " + " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table + " entries are not linked from " + col1 + " to " + col2);
			ReportManager.problem(this, con, "USEFUL SQL: SELECT hc_table1.*" + sql);
			return false;

		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table + " : " + col1 + " -> " + col2 + " using FK, look at the StackTrace if any");
			return false;
		}

		return true;

	} // checkForOrphansSameTable


	/**
	 * Get a Map associating each species with its core database
	 *
	 * @return A map (key:Species, value:DatabaseRegistryEntry).
	 */
	public final HashMap<Species, DatabaseRegistryEntry> getSpeciesCoreDbMap(final DatabaseRegistry dbr) {

		HashMap<Species, DatabaseRegistryEntry> speciesCoreMap = new HashMap<Species, DatabaseRegistryEntry>();

		for (DatabaseRegistryEntry entry : dbr.getAllEntries()) {
			// We need to check the database name because some _cdna_
			// databases have the DatabaseType.CORE type
			if (entry.getType().equals(DatabaseType.CORE) && entry.getName().contains("_core_")) {
				speciesCoreMap.put(entry.getSpecies(), entry);
			}
		}

		return speciesCoreMap;

	} // getSpeciesCoreDbMap


	/**
	 * Tells whether the current database being tested is a Master database
	 *
	 * @return a boolean
	 */
	public boolean isMasterDB(Connection con) {
		return DBUtils.getShortDatabaseName(con).contains(System.getProperty("compara_master.database"));
	}

} // AbstractComparaTestCase
