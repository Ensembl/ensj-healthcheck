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
 * Subclass of EnsTestCase for tests that apply to <em>multiple</em> databases. Such tests should
 * subclass <em>this</em> class and implement the <code>run</code> method.
 */
public abstract class MultiDatabaseTestCase extends EnsTestCase {

    /**
     * This method should be overridden by subclasses.
     * 
     * @param dbr
     *          The database registry containing all the matched databases.
     * @return true if the test pased.
     */
    public abstract boolean run(DatabaseRegistry dbr);

    //---------------------------------------------------------------------
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

    //---------------------------------------------------------------------
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
    public Map getSpeciesDatabaseMap(DatabaseRegistry dbr, boolean fromSecondary) {

        Map speciesMap = new HashMap();

        DatabaseRegistryEntry[] allDBs, speciesDBs;
        if (fromSecondary) {
            allDBs = dbr.getAllSecondary();
        } else {
            allDBs = dbr.getAll();
        }
        for (int i = 0; i < allDBs.length; i++) {
            Species s = allDBs[i].getSpecies();
            if (fromSecondary) {
                speciesDBs = dbr.getAllSecondary(s);
            } else {
                speciesDBs = dbr.getAll(s);
            }
            logger.finest("Got " + speciesDBs.length + " databases for " + s.toString());
            if (!speciesMap.containsKey(s)) {
                speciesMap.put(s, speciesDBs);
            }
        }

        return speciesMap;

    } // getSpeciesDatabaseMap

    //---------------------------------------------------------------------
    /**
     * Check that the same piece of SQL gives the same result across several species.
     * 
     * @param sql
     *          The SQL to check.
     * @param dbr
     *          The registry containing the databases to check.
     * @param types
     *          Only databases from the registry whose types are contined in this array will be
     *          used.
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

    //---------------------------------------------------------------------

    /**
     * Check that the contents of a table are the same across each of the species currently
     * defined.
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

        if (!result) {
            ReportManager.problem(this, "", problem);
        } else {
            ReportManager.correct(this, "", correct);
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
    
    /**
     * Run different queries in two databases and compare the results
     * 
     * @param con1
     *          Connection to database1
     * @param sql1
     *          SQL query to run in database1
     * @param con2
     *          Connection to database2
     * @param sql2
     *          SQL query to run in database2
     * @return true if both queries return the same rows.
     */
    public boolean compareQueries(Connection con1, String sql1, Connection con2, String sql2) {
      boolean result = true;
      String dbName1 = (con1 == null) ? "no_database" : DBUtils.getShortDatabaseName(con1);
      String dbName2 = (con2 == null) ? "no_database" : DBUtils.getShortDatabaseName(con2);
      Map values1 = runQuery(con1, sql1);
      Map values2 = runQuery(con2, sql2);
      Iterator it1 = values1.keySet().iterator();
      while (it1.hasNext()) {
        String thisValue = (String) it1.next();
        if (values2.get(thisValue) == null) {
          result = false;
          ReportManager.problem(this, dbName1, thisValue + " is not in " + dbName2);
        }
      } // foreach it1

      Iterator it2 = values2.keySet().iterator();
      while (it2.hasNext()) {
        String thisValue = (String) it2.next();
        if (values1.get(thisValue) == null) {
          result = false;
          ReportManager.problem(this, dbName2, thisValue + " is not in " + dbName1);
        }
      } // foreach it1

      return result;
    }

    /**
     * Run a query in a database and return the results as a HashMap where the keys
     * are the rows (cols are concatenated with "::").
     * 
     * @param con
     *          Connection to database
     * @param sql
     *          SQL query to run in database
     * @return Map where the keys are the rows (cols are concatenated with "::").
     */
    private Map runQuery(Connection con, String sql) {
      Map values = new HashMap();
      try {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
                
        while (rs.next()) {
          String thisValue = rs.getString(1);
          for (int a = 2; a <= rs.getMetaData().getColumnCount(); a++) {
            thisValue += "::" + rs.getString(a);
          }
          values.put(thisValue, "1");
        }
        rs.close();
        stmt.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
      return values;
    }

}
