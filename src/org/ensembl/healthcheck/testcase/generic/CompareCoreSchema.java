/*
 Copyright (C) 2004 EBI, GRL
 
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Test case to compare table structures between several schemas.
 * 
 * Has several ways of deciding which schema to use as the "master" to compare
 * all the others against:
 * <p>
 * <ol>
 * <li>If the property schema.file in database.properties exists, the
 * table.sql file it points to</li>
 * <li>If the schema.file property is not present, the schema named by the
 * property schema.master is used</li>
 * <li>If neither of the above properties are present, the (arbitrary) first
 * schema is used as the master.</li>
 * </ol>
 */

public class CompareCoreSchema extends MultiDatabaseTestCase {

    /**
     * Creates a new instance of CompareCoreSchemaTestCase.
     */
    public CompareCoreSchema() {

        addToGroup("release");

    }

    /**
     * Compare each database with the master.
     * 
     * @param dbr
     *          The database registry containing all the specified databases.
     * @return true if the test passed.
     */
    public boolean run(DatabaseRegistry dbr) {

        boolean result = true;

        Connection masterCon = null;
        Statement masterStmt = null;

        String definitionFile = null;
        String masterSchema = null;

        DatabaseRegistryEntry[] databases = dbr.getAll();

        definitionFile = System.getProperty("schema.file");
        if (definitionFile == null) {
            logger
                    .warning("CompareCoreSchema: No schema definition file found! Set schema.file property in database.properties if you want to use a table.sql file or similar.");

            masterSchema = System.getProperty("master.schema");
            if (masterSchema != null) {
                logger.info("Will use " + masterSchema + " as specified master schema for comparisons.");
            } else {
                logger
                        .warning("CompareCoreSchema: No master schema defined file found! Set master.schema property in database.properties if you want to use a master schema.");
            }
        } else {
            logger.fine("Will use schema definition from " + definitionFile);
        }

        try {

            if (definitionFile != null) { // use a schema definition file to
                // generate a temporary database

                logger.info("About to import " + definitionFile);
                masterCon = importSchema(definitionFile);
                logger.info("Got connection to " + DBUtils.getShortDatabaseName(masterCon));

            } else if (masterSchema != null) {
                // use the defined schema name as the master
                // get connection to master schema
                masterCon = getSchemaConnection(masterSchema);
                logger.fine("Opened connection to master schema in " + DBUtils.getShortDatabaseName(masterCon));

            } else {
                // just use the first one to compare with all the others
                if (databases.length > 0) {
                    masterCon = databases[0].getConnection();
                    logger.info("Using " + DBUtils.getShortDatabaseName(masterCon) + " as 'master' for comparisions.");
                } else {
                    logger.warning("Can't find any databases to check against");
                }

            }

            masterStmt = masterCon.createStatement();

            for (int i = 0; i < databases.length; i++) {

                if (appliesToType(databases[i].getType())) {

                    Connection checkCon = databases[i].getConnection();

                    if (checkCon != masterCon) {

                        logger.info("Comparing " + DBUtils.getShortDatabaseName(masterCon) + " with "
                                + DBUtils.getShortDatabaseName(checkCon));

                        // check that both schemas have the same tables
                        if (!compareTablesInSchema(masterCon, checkCon)) {
                            // if not the same, this method will generate a
                            // report
                            logger.info("Table name discrepancy detected, skipping rest of checks for "
                                    + DBUtils.getShortDatabaseName(checkCon));
                            continue;
                        }

                        Statement dbStmt = checkCon.createStatement();

                        // check each table in turn
                        String[] tableNames = getTableNames(masterCon);
                        for (int j = 0; j < tableNames.length; j++) {

                            String table = tableNames[j];

                            String sql = "SHOW CREATE TABLE " + table;
                            ResultSet masterRS = masterStmt.executeQuery(sql);
                            ResultSet dbRS = dbStmt.executeQuery(sql);

                            result &= DBUtils.compareResultSets(masterRS, dbRS, this, " [" + table + "]");

                            masterRS.close();
                            dbRS.close();

                        } // while table

                        dbStmt.close();

                    } // if checkCon != masterCon

                } // if appliesToType

            } // for database

            masterStmt.close();

        } catch (SQLException se) {

            logger.severe(se.getMessage());

        } finally {

            // void leaving temporary DBs lying around if something bad happens
            if (definitionFile == null && masterCon != null) {
                // double-check to make sure the DB we're going to remove is a
                // temp one
                String dbName = DBUtils.getShortDatabaseName(masterCon);
                if (dbName.indexOf("_temp_") > -1) {
                    removeDatabase(masterCon);
                    logger.info("Removed " + DBUtils.getShortDatabaseName(masterCon));
                }
            }

        }

        return result;

    } // run

} // CompareCoreSchema
