/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase;

import java.util.*;

import org.ensembl.healthcheck.*;

/**
 * Subclass of EnsTestCase for tests that apply to <em>multiple</em> databases. Such
 * tests should subclass <em>this</em> class and implement the <code>run</code>
 * method.
 */
public abstract class MultiDatabaseTestCase extends EnsTestCase {

	public abstract boolean run(DatabaseRegistry dbr);

	//---------------------------------------------------------------------
	/**
	 * Build a hash of arrays of DatabaseRegistryEntries, one key for each species.
	 * @param dbr The DatabaseRegistry to use.
	 * @return HashMap of DatabaseRegistryEntry[], one key/value pair for each Species.
	 */
	public Map getSpeciesDatabaseMap(DatabaseRegistry dbr) {

		Map speciesMap = new HashMap();
		DatabaseRegistryEntry[] allDBs = dbr.getAll();
		for (int i = 0; i < allDBs.length; i++) {
			Species s = allDBs[i].getSpecies();
			DatabaseRegistryEntry[] speciesDBs = dbr.getAll(s);
			logger.finest("Got " + speciesDBs.length + " databases for " + s.toString());
			if (!speciesMap.containsKey(s)) {
				speciesMap.put(s, speciesDBs);
			}
		}

		return speciesMap;

	} // getSpeciesDatabaseMap

	//---------------------------------------------------------------------
	
	public boolean checkTableAcrossSpecies(String table, DatabaseRegistry dbr) {
		
		boolean result = true;

		Map speciesMap = getSpeciesDatabaseMap(dbr);

		// check that the table has the same number of rows across the species
		Iterator it = speciesMap.keySet().iterator();
		while (it.hasNext()) {

			Species species = (Species)it.next();

			boolean allMatch = checkSameSQLResult("SELECT COUNT(*) FROM " + table, (DatabaseRegistryEntry[])speciesMap
					.get(species));
			if (!allMatch) {
				result = false;
				ReportManager.problem(this, species.toString(), "Differences in " + table + " table across species");
			} else {
				ReportManager.correct(this, species.toString(), "All " + table + " tables the same");
			}

		} // foreach species

		return result;
	}
	
	//---------------------------------------------------------------------
}
