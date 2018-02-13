/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Subclass of EnsTestCase for tests that apply to <em>multiple</em> databases.
 * Such tests should subclass <em>this</em> class and implement the
 * <code>run</code> method.
 */
public abstract class MultiDatabaseTestCase extends EnsTestCase {

	/**
	 * This method should be overridden by subclasses.
	 * 
	 * @param dbr
	 *            The database registry containing all the matched databases.
	 * @return true if the test passed.
	 */
	public abstract boolean run(DatabaseRegistry dbr) throws SQLException;

	// ---------------------------------------------------------------------
	/**
	 * Check that the same piece of SQL gives the same result across several
	 * species.
	 * 
	 * @param sql
	 *            The SQL to check.
	 * @param dbr
	 *            The registry containing the databases to check.
	 * @param types
	 *            Only databases from the registry whose types are contined in this
	 *            array will be used.
	 * @return true if SQL returns the same for all databases for each species in
	 *         dbr.
	 */
	public boolean checkSQLAcrossSpecies(String sql, DatabaseRegistry dbr, DatabaseType[] types,
			boolean comparingSchema) {

		boolean result = true;

		// Use whole registry to access all databases, but restrict to species of
		// interest
		DatabaseRegistry mainDbr = DBUtils.getMainDatabaseRegistry();

		for (String species : dbr.getUniqueSpecies()) {

			// filter by database type
			DatabaseRegistryEntry[] filteredDBs = filterByType(mainDbr.getAll(species), types);
			result &= checkSameSQLResult(sql, filteredDBs, comparingSchema);

		} // foreach species

		return result;
	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the contents of a table are the same across each of the species
	 * currently defined.
	 * 
	 * @param table
	 *            The table to check.
	 * @param dbr
	 *            The registry containing the databases to check.
	 * @param types
	 *            The DatabaseTypes to look at.
	 * @return true if the table is the same across all species.
	 */
	public boolean checkTableAcrossSpecies(String table, DatabaseRegistry dbr, DatabaseType[] types, String correct,
			String problem, String extraSQL) {

		String sql = "SELECT COUNT(*) FROM " + table + " " + extraSQL;
		boolean result = checkSQLAcrossSpecies(sql, dbr, types, false);

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Filter an array of DatabaseRegistryEntries.
	 * 
	 * @param The
	 *            databases to check.
	 * @param types
	 *            The types to look for.
	 * @return Those entries in databases that have a type that is in types.
	 */
	private DatabaseRegistryEntry[] filterByType(DatabaseRegistryEntry[] databases, DatabaseType[] types) {

		List filtered = new ArrayList();

		for (int i = 0; i < databases.length; i++) {

			if (Utils.objectInArray(databases[i].getType(), types)) {
				filtered.add(databases[i]);
			}
		}

		return (DatabaseRegistryEntry[]) filtered.toArray(new DatabaseRegistryEntry[filtered.size()]);

	}

}
