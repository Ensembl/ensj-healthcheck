/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Statement;
import java.sql.ResultSet;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Subclass of EnsTestCase for tests that apply to <em>multiple</em> databases. Such tests should subclass <em>this</em> class and
 * implement the <code>run</code> method.
 */
public abstract class MultiDatabaseTestCase extends EnsTestCase {

	/**
	 * This method should be overridden by subclasses.
	 * 
	 * @param dbr
	 *          The database registry containing all the matched databases.
	 * @return true if the test passed.
	 */
	public abstract boolean run(DatabaseRegistry dbr);

	// ---------------------------------------------------------------------
	/**
	 * Build a hash of arrays of DatabaseRegistryEntries, one key for each species.
	 * 
	 * @param dbr
	 *          The DatabaseRegistry to use.
	 * @return HashMap of DatabaseRegistryEntry[], one key/value pair for each Species.
	 */
	public Map getSpeciesDatabaseMap(DatabaseRegistry dbr) {

		return getSpeciesDatabaseMap(dbr, false);

	} // getSpeciesDatabaseMap

	// ---------------------------------------------------------------------
	/**
	 * Build a hash of arrays of DatabaseRegistryEntries, one key for each species.
	 * 
	 * @param dbr
	 *          The DatabaseRegistry to use.
	 * @param fromSecondary
	 *          boolean value for getting species map from secondary server instead
	 * 
	 * @return HashMap of DatabaseRegistryEntry[], one key/value pair for each Species.
	 */
	public Map<Species, DatabaseRegistryEntry[]> getSpeciesDatabaseMap(DatabaseRegistry dbr, boolean fromSecondary) {

		Map<Species, DatabaseRegistryEntry[]> speciesMap = new HashMap<Species, DatabaseRegistryEntry[]>();

		DatabaseRegistryEntry[] allDBs, speciesDBs;

		allDBs = fromSecondary ? DBUtils.getSecondaryDatabaseRegistry().getAll() : DBUtils.getMainDatabaseRegistry().getAll();

		for (int i = 0; i < allDBs.length; i++) {

			Species s = allDBs[i].getSpecies();
                        boolean propagated = true;
                        if (ReportManager.usingDatabase()) {
                                propagated = ReportManager.hasPropagated(allDBs[i]);
                        }


			speciesDBs = fromSecondary ? DBUtils.getSecondaryDatabaseRegistry().getAll(s) : DBUtils.getMainDatabaseRegistry().getAll(s);

			logger.finest("Got " + speciesDBs.length + " databases for " + s.toString());
			if (!speciesMap.containsKey(s) && propagated) {
				speciesMap.put(s, speciesDBs);
			}
		}

		return speciesMap;

	} // getSpeciesDatabaseMap

	// ---------------------------------------------------------------------
	/**
	 * Check that the same piece of SQL gives the same result across several species.
	 * 
	 * @param sql
	 *          The SQL to check.
	 * @param dbr
	 *          The registry containing the databases to check.
	 * @param types
	 *          Only databases from the registry whose types are contined in this array will be used.
	 * @return true if SQL returns the same for all databases for each species in dbr.
	 */
	public boolean checkSQLAcrossSpecies(String sql, DatabaseRegistry dbr, DatabaseType[] types, boolean comparingSchema) {

		boolean result = true;

		Map speciesMap = getSpeciesDatabaseMap(dbr);

		// check that the table has the same number of rows across the species
		Iterator it = speciesMap.keySet().iterator();
		while (it.hasNext()) {

			Species species = (Species) it.next();

			DatabaseRegistryEntry[] dbsForSpecies = (DatabaseRegistryEntry[]) speciesMap.get(species);
			// filter by database type
			DatabaseRegistryEntry[] filteredDBs = filterByType(dbsForSpecies, types);
			result &= checkSameSQLResult(sql, filteredDBs, comparingSchema);

		} // foreach species

		return result;
	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the contents of a table are the same across each of the species currently defined.
	 * 
	 * @param table
	 *          The table to check.
	 * @param dbr
	 *          The registry containing the databases to check.
	 * @param types
	 *          The DatabaseTypes to look at.
	 * @return true if the table is the same across all species.
	 */
	public boolean checkTableAcrossSpecies(String table, DatabaseRegistry dbr, DatabaseType[] types, String correct, String problem, String extraSQL) {

		String sql = "SELECT COUNT(*) FROM " + table + " " + extraSQL;
		boolean result = checkSQLAcrossSpecies(sql, dbr, types, false);

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Filter an array of DatabaseRegistryEntries.
	 * 
	 * @param The
	 *          databases to check.
	 * @param types
	 *          The types to look for.
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
