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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.util.Utils;

/**
 * Subclass of EnsTestCase for tests that apply to <em>multiple</em>
 * databases. Such tests should subclass <em>this</em> class and implement
 * the <code>run</code> method.
 */
public abstract class MultiDatabaseTestCase extends EnsTestCase {

    public abstract boolean run(DatabaseRegistry dbr);

    //---------------------------------------------------------------------
    /**
     * Build a hash of arrays of DatabaseRegistryEntries, one key for each
     * species.
     * 
     * @param dbr
     *          The DatabaseRegistry to use.
     * @return HashMap of DatabaseRegistryEntry[], one key/value pair for each
     *         Species.
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
    /**
     * Check that the same piece of SQL gives the same result across several
     * species.
     * 
     * @param sql
     *          The SQL to check.
     * @param dbr
     *          The registry containing the databases to check.
     * @param types
     *          Only databases from the registry whose types are contined in
     *          this array will be used.
     * @return true if SQL returns the same for all databases for each species
     *         in dbr.
     */
    public boolean checkSQLAcrossSpecies(String sql, DatabaseRegistry dbr, DatabaseType[] types) {

        boolean result = true;

        Map speciesMap = getSpeciesDatabaseMap(dbr);

        // check that the table has the same number of rows across the species
        Iterator it = speciesMap.keySet().iterator();
        while (it.hasNext()) {

            Species species = (Species) it.next();

            DatabaseRegistryEntry[] dbsForSpecies = (DatabaseRegistryEntry[]) speciesMap.get(species);
            // filter by database type
            DatabaseRegistryEntry[] filteredDBs = filterByType(dbsForSpecies, types);
            result &= checkSameSQLResult(sql, filteredDBs);

        } // foreach species

        return result;
    }

    //---------------------------------------------------------------------

    public boolean checkTableAcrossSpecies(String table, DatabaseRegistry dbr, DatabaseType[] types) {

        String sql = "SELECT COUNT(*) FROM " + table;
        boolean result = checkSQLAcrossSpecies(sql, dbr, types);

        if (!result) {
            ReportManager.problem(this, "", "Differences in " + table + " table across species");
        } else {
            ReportManager.correct(this, "", "All " + table + " tables the same");
        }

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

    //---------------------------------------------------------------------
}
