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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.util;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseRegistryEntry.DatabaseInfo;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Various database utilities.
 */

public final class DBUtils {

    private static Logger logger = Logger.getLogger("HealthCheckLogger");

    private static List<DatabaseServer> mainDatabaseServers;
    private static List<DatabaseServer> secondaryDatabaseServers;

    private static DatabaseRegistry mainDatabaseRegistry;
    private static DatabaseRegistry secondaryDatabaseRegistry;

    private static ConfigureHost hostConfiguration;
    private static boolean useDefaultsFromFile = true;

    /**
     * <p>
     * Initialises all attributes of DBUtils.
     * </p>
     * 
     * <p>
     * This can be necessary in the GUI, if the user wants to change the
     * database server. In that case the user calls
     * </p>
     * 
     * <code>
     * 	DBUtils.initialise();
     * 	DBUtils.setHostConfiguration(newHostConfiguration);
     * </code>
     * 
     * <p>
     * then things like
     * </p>
     * 
     * <code> DatabaseRegistry databaseRegistry = new DatabaseRegistry(regexps,
     * null, null, false); <code>
     * 
     * <p>
     * will work as expected.
     * </p>
     * 
     */
    public static void initialise() {

        mainDatabaseServers = null;
        secondaryDatabaseServers = null;
        mainDatabaseRegistry = null;
        secondaryDatabaseRegistry = null;
        hostConfiguration = null;
        useDefaultsFromFile = true;
    }

    public static void initialise(boolean useDefaultsFromFile) {

        initialise();
        DBUtils.useDefaultsFromFile = useDefaultsFromFile;
    }

    public static ConfigureHost getHostConfiguration() {
        return hostConfiguration;
    }

    public static void setHostConfiguration(ConfigureHost hostConfiguration) {
        DBUtils.hostConfiguration = hostConfiguration;
    }

    public static String getSecondaryDatabase() {
        if (hostConfiguration != null) {
            return hostConfiguration.getSecondaryDb();
        } else {
            return "secondary.database";
        }
    }

    // hide constructor to stop instantiation
    private DBUtils() {

    }

    /**
     * Helper to avoid having to keep constructing tedious URLs - mysql only
     * 
     * @param driverClassName
     * @param host
     * @param port
     * @param user
     * @param password
     * @param database
     * @return Connection
     * @throws SQLException
     */
    public static Connection openConnection(String driverClassName, String host, String port, String user,
            String password, String database) throws SQLException {

        return ConnectionPool.getConnection(driverClassName, "jdbc:mysql://" + host + ":" + port + "/" + database, user,
                password);

    }

    // -------------------------------------------------------------------------
    /**
     * Open a connection to the database.
     * 
     * @param driverClassName
     *            The class name of the driver to load.
     * @param databaseURL
     *            The URL of the database to connect to.
     * @param user
     *            The username to connect with.
     * @param password
     *            Password for user.
     * @return A connection to the database, or null.
     * @throws SQLException
     */
    public static Connection openConnection(String driverClassName, String databaseURL, String user, String password)
            throws SQLException {

        return ConnectionPool.getConnection(driverClassName, databaseURL, user, password);

    } // openConnection

    // -------------------------------------------------------------------------
    /**
     * Get a list of the database names for a particular connection.
     * 
     * @param con
     *            The connection to query.
     * @return An array of Strings containing the database names.
     */

    public static String[] listDatabases(Connection con) {
        String release = getRelease();
        if(StringUtils.isEmpty(release)) {
            throw new IllegalArgumentException("Current release not specified");
        }
        Integer lastRelease = Integer.parseInt(release) - 1;
        String query = "SHOW DATABASES WHERE `Database` LIKE '%" + release + "%' OR `Database` LIKE '%"
                + lastRelease.toString() + "%' OR `Database` LIKE 'ensembl_production%' OR `Database` LIKE 'ncbi_taxonomy%'";
        List<String> dbs = getSqlTemplate(con).queryForDefaultObjectList(query, String.class);

        // Additional clean up for databases which are not needed
        List<String> good_dbs = new ArrayList<String>();
        for (String db : dbs) {
            if (db.matches("(.*)ccds(.*)")) {
                // Skip ccds databases
                // System.out.println("Found " + db);
            } else if (db.matches("ensembl_(.*)_(.*)")) {
                // Skip release multi databases
                // System.out.println("Found " + db);
            } else {
                good_dbs.add(db);
            }
        }
        if (good_dbs.size() == 0) {
            logger.warning("No databases selected using release " + release + ", please check your setting");
        }
        return good_dbs.toArray(new String[] {});

    } // listDatabases

    
    private static String release;
    public static String getRelease() {
        if(release == null && hostConfiguration!=null) {
            release = hostConfiguration.getRelease();
        }
        return release;
    }
    public static void setRelease(String r) {
        release = r;
    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of the database names that match a certain pattern for a
     * particular connection.
     * 
     * @param con
     *            The connection to query.
     * @param regex
     *            A regular expression to match. If null, match all.
     * @return An array of Strings containing the database names.
     */
    public static String[] listDatabases(Connection con, String regex) {

        ArrayList<String> dbMatches = new ArrayList<String>();

        // If no regex, query all databases
        if (regex == null) {
            return listDatabases(con);
            // Otherwise, just query for that one regex
        } else {
            String newRegex = regex.replace(".*", "%");
            String query = String.format("SHOW DATABASES LIKE '%s'", newRegex);
            List<String> dbs = getSqlTemplate(con).queryForDefaultObjectList(query, String.class);
            return dbs.toArray(new String[] {});
        }

    } // listDatabases

    // -------------------------------------------------------------------------
    /**
     * Compare a list of ResultSets to see if there are any differences. Note
     * that if the ResultSets are large and/or there are many of them, this may
     * take a long time!
     * 
     * @return The number of differences.
     * @param testCase
     *            The test case that is calling the comparison. Used for
     *            ReportManager.
     * @param resultSetGroup
     *            The list of ResultSets to compare
     */
    public static boolean compareResultSetGroup(List<ResultSet> resultSetGroup, EnsTestCase testCase,
            boolean comparingSchema) {

        boolean same = true;

        // avoid comparing the same two ResultSets more than once
        // i.e. only need the upper-right triangle of the comparison matrix
        int size = resultSetGroup.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                ResultSet rsi = resultSetGroup.get(i);
                ResultSet rsj = resultSetGroup.get(j);
                same &= compareResultSets(rsi, rsj, testCase, "", true, true, "", comparingSchema);
            }
        }

        return same;

    } // compareResultSetGroup

    // -------------------------------------------------------------------------
    /**
     * Compare two ResultSets.
     * 
     * @return True if all the following are true:
     *         <ol>
     *         <li>rs1 and rs2 have the same number of columns</li>
     *         <li>The name and type of each column in rs1 is equivalent to the
     *         corresponding column in rs2.</li>
     *         <li>All the rows in rs1 have the same type and value as the
     *         corresponding rows in rs2.</li>
     *         </ol>
     * @param testCase
     *            The test case calling the comparison; used in ReportManager.
     * @param text
     *            Additional text to put in any error reports.
     * @param rs1
     *            The first ResultSet to compare.
     * @param rs2
     *            The second ResultSet to compare.
     * @param reportErrors
     *            If true, error details are stored in ReportManager as they are
     *            found.
     * @param singleTableName
     *            If comparing 2 result sets from a single table (or from a
     *            DESCRIBE table) this should be the name of the table, to be
     *            output in any error text. Otherwise "".
     */
    public static boolean compareResultSets(ResultSet rs1, ResultSet rs2, EnsTestCase testCase, String text,
            boolean reportErrors, boolean warnNull, String singleTableName, boolean comparingSchema) {
        return compareResultSets(rs1, rs2, testCase, text, reportErrors, warnNull, singleTableName, null,
                comparingSchema);
    }

    /**
     * Check that a particular SQL statement has the same result when executed
     * on more than one database.
     * 
     * @return True if all matched databases provide the same result, false
     *         otherwise.
     * @param sql
     *            The SQL query to execute.
     * @param regexp
     *            A regexp matching the database names to check.
     */
    public static boolean checkSameSQLResult(EnsTestCase test, String sql, String regexp, boolean comparingSchema) {

        ArrayList<ResultSet> resultSetGroup = new ArrayList<ResultSet>();
        ArrayList<Statement> statements = new ArrayList<Statement>();

        try {

            DatabaseRegistry mainDatabaseRegistry = DBUtils.getMainDatabaseRegistry();

            for (DatabaseRegistryEntry dbre : mainDatabaseRegistry.getMatching(regexp)) {

                Connection con = dbre.getConnection();
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.createStatement();
                    rs = stmt.executeQuery(sql);
                    if (rs != null) {
                        resultSetGroup.add(rs);
                    }
                    logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
                    // note that the Statement can't be closed here as we use
                    // the
                    // ResultSet elsewhere so store a reference to it for
                    // closing
                    // later
                    statements.add(stmt);

                } catch (Exception e) {
                    throw new SqlUncheckedException("Could not check same SQL results", e);
                } finally {
                    closeQuietly(rs);
                    closeQuietly(stmt);
                }
            }

            logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
            return DBUtils.compareResultSetGroup(resultSetGroup, test, comparingSchema);

        } finally {
            for (ResultSet rs : resultSetGroup) {
                closeQuietly(rs);
            }
            for (Statement s : statements) {
                closeQuietly(s);
            }
        }

    } // checkSameSQLResult

    // -------------------------------------------------------------------------
    /**
     * Check that a particular SQL statement has the same result when executed
     * on more than one database.
     * 
     * @return True if all matched databases provide the same result, false
     *         otherwise.
     * @param sql
     *            The SQL query to execute.
     * @param databases
     *            The DatabaseRegistryEntries on which to execute sql.
     */
    public static boolean checkSameSQLResult(EnsTestCase test, String sql, DatabaseRegistryEntry[] databases,
            boolean comparingSchema) {

        List<ResultSet> resultSetGroup = new ArrayList<ResultSet>();
        List<Statement> statements = new ArrayList<Statement>();
        try {

            for (int i = 0; i < databases.length; i++) {

                Connection con = databases[i].getConnection();
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.createStatement();
                    rs = stmt.executeQuery(sql);
                    if (rs != null) {
                        resultSetGroup.add(rs);
                    }
                    logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
                    statements.add(stmt);
                } catch (Exception e) {
                    DBUtils.closeQuietly(rs);
                    DBUtils.closeQuietly(stmt);
                    throw new SqlUncheckedException("Could not check same SQL results", e);

                }
            }

            logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
            return DBUtils.compareResultSetGroup(resultSetGroup, test, comparingSchema);
        } catch (Exception e) {
            throw new SqlUncheckedException("Could not check same SQL results", e);
        } finally {
            for (ResultSet rs : resultSetGroup) {
                DBUtils.closeQuietly(rs);
            }
            for (Statement s : statements) {
                DBUtils.closeQuietly(s);
            }
        }

    } // checkSameSQLResult

    public static boolean compareResultSets(ResultSet rs1, ResultSet rs2, EnsTestCase testCase, String text,
            boolean reportErrors, boolean warnNull, String singleTableName, int[] columns, boolean comparingSchema) {

        // quick tests first
        // Check for object equality
        if (rs1.equals(rs2)) {
            return true;
        }

        try {

            // get some information about the ResultSets
            String name1 = getShortDatabaseName(rs1.getStatement().getConnection());
            String name2 = getShortDatabaseName(rs2.getStatement().getConnection());

            // Check for same column count, names and types
            ResultSetMetaData rsmd1 = rs1.getMetaData();
            ResultSetMetaData rsmd2 = rs2.getMetaData();
            if (rsmd1.getColumnCount() != rsmd2.getColumnCount() && columns == null) {

                if (reportErrors) {

                    ReportManager.problem(testCase, name1, "Column counts differ " + singleTableName + " " + name1
                            + ": " + rsmd1.getColumnCount() + " " + name2 + ": " + rsmd2.getColumnCount());
                }

                return false; // Deliberate early return for performance
                // reasons
            }

            if (columns == null) {
                columns = new int[rsmd1.getColumnCount()];
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = i + 1;
                }
            }

            for (int j = 0; j < columns.length; j++) {
                int i = columns[j];

                // note columns indexed from l
                if (!((rsmd1.getColumnName(i)).equals(rsmd2.getColumnName(i)))) {

                    if (reportErrors) {

                        ReportManager.problem(testCase, name1,
                                "Column names differ for " + singleTableName + " column " + i + " - " + name1 + ": "
                                        + rsmd1.getColumnName(i) + " " + name2 + ": " + rsmd2.getColumnName(i));
                    }

                    // Deliberate early return for performance reasons
                    return false;

                }
                if (rsmd1.getColumnType(i) != rsmd2.getColumnType(i)) {

                    if (reportErrors) {

                        ReportManager.problem(testCase, name1,
                                "Column types differ for " + singleTableName + " column " + i + " - " + name1 + ": "
                                        + rsmd1.getColumnType(i) + " " + name2 + ": " + rsmd2.getColumnType(i));
                    }
                    return false; // Deliberate early return for performance
                    // reasons
                }
            } // for column

            // make sure both cursors are at the start of the ResultSet
            // (default is before the start)
            rs1.beforeFirst();
            rs2.beforeFirst();
            // if quick checks didn't cause return, try comparing row-wise

            int row = 1;
            while (rs1.next()) {

                if (rs2.next()) {
                    String str = name1 + " and " + name2 + text + " " + singleTableName + " with columns ";
                    for (int j = 0; j < columns.length; j++) {
                        int i = columns[j];
                        str += rsmd1.getColumnName(i) + " " + Utils.truncate(rs1.getString(i), 250, true) + ", ";
                        // note columns indexed from 1
                        if (!compareColumns(rs1, rs2, i, warnNull)) {
                            str += " differ for values " + Utils.truncate(rs1.getString(i), 250, true) + ", "
                                    + Utils.truncate(rs2.getString(i), 250, true);
                            if (reportErrors) {
                                ReportManager.problem(testCase, name1, str);
                            }
                            return false;
                        }
                    }
                    row++;

                } else {
                    // rs1 has more rows than rs2
                    if (reportErrors) {
                        ReportManager.problem(testCase, name1,
                                singleTableName + " has more rows in " + name1 + " than in " + name2);
                    }
                    return false;
                }

            } // while rs1

            // if both ResultSets are the same, then we should be at the end of
            // both, i.e. .next() should return false
            String extra = comparingSchema ? ". This means that there are missing columns in the table, rectify!" : "";
            if (rs1.next()) {

                if (reportErrors) {
                    ReportManager.problem(testCase, name1,
                            name1 + " " + singleTableName + " has additional rows that are not in " + name2 + extra);
                }
                return false;
            } else if (rs2.next()) {

                if (reportErrors) {
                    ReportManager.problem(testCase, name1,
                            name2 + " " + singleTableName + " has additional rows that are not in " + name1 + extra);

                }
                return false;
            }

        } catch (SQLException se) {
            throw new SqlUncheckedException("Could not compare two result sets", se);
        }

        return true;

    } // compareResultSets

    // -------------------------------------------------------------------------
    /**
     * Compare a particular column in two ResultSets.
     * 
     * @param rs1
     *            The first ResultSet to compare.
     * @param rs2
     *            The second ResultSet to compare.
     * @param i
     *            The index of the column to compare.
     * @return True if the type and value of the columns match.
     */
    public static boolean compareColumns(ResultSet rs1, ResultSet rs2, int i, boolean warnNull) {

        try {

            ResultSetMetaData rsmd = rs1.getMetaData();

            Connection con1 = rs1.getStatement().getConnection();
            Connection con2 = rs2.getStatement().getConnection();

            if (rs1.getObject(i) == null) {
                if (warnNull) {
                    logger.fine("Column " + rsmd.getColumnName(i) + " is null in table " + rsmd.getTableName(i) + " in "
                            + DBUtils.getShortDatabaseName(con1));
                }
                return (rs2.getObject(i) == null); // true if both are null
            }
            if (rs2.getObject(i) == null) {
                if (warnNull) {
                    logger.fine("Column " + rsmd.getColumnName(i) + " is null in table " + rsmd.getTableName(i) + " in "
                            + DBUtils.getShortDatabaseName(con2));
                }
                return (rs1.getObject(i) == null); // true if both are null
            }

            // Note deliberate early returns for performance reasons
            switch (rsmd.getColumnType(i)) {

            case Types.INTEGER:
                return rs1.getInt(i) == rs2.getInt(i);

            case Types.SMALLINT:
                return rs1.getInt(i) == rs2.getInt(i);

            case Types.TINYINT:
                return rs1.getInt(i) == rs2.getInt(i);

            case Types.VARCHAR:
                String s1 = rs1.getString(i);
                String s2 = rs2.getString(i);
                // ignore "AUTO_INCREMENT=" part in final part of table
                // definition
                s1 = s1.replaceAll("AUTO_INCREMENT=[0-9]+ ", "");
                s2 = s2.replaceAll("AUTO_INCREMENT=[0-9]+ ", "");
                return s1.equals(s2);

            case Types.FLOAT:
                return rs1.getFloat(i) == rs2.getFloat(i);

            case Types.DOUBLE:
                return rs1.getDouble(i) == rs2.getDouble(i);

            case Types.TIMESTAMP:
                return rs1.getTimestamp(i).equals(rs2.getTimestamp(i));

            default:
                // treat everything else as a String (should deal with ENUM and
                // TEXT)
                if (rs1.getString(i) == null || rs2.getString(i) == null) {
                    return true; // ????
                } else {
                    return rs1.getString(i).equals(rs2.getString(i));
                }

            } // switch

        } catch (SQLException se) {
            throw new SqlUncheckedException("Could not compare two columns sets", se);
        }

    } // compareColumns

    // -------------------------------------------------------------------------
    /**
     * Print a ResultSet to standard out. Optionally limit the number of rows.
     * 
     * @param maxRows
     *            The maximum number of rows to print. -1 to print all rows.
     * @param rs
     *            The ResultSet to print.
     */
    public static void printResultSet(ResultSet rs, int maxRows) {

        int row = 0;

        try {
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println("");
                if (maxRows != -1 && ++row >= maxRows) {
                    break;
                }
            }
        } catch (SQLException se) {
            throw new SqlUncheckedException("Could not print result set", se);
        }

    } // printResultSet

    // -------------------------------------------------------------------------
    /**
     * Gets the database name, without the jdbc:// prefix.
     * 
     * @param con
     *            The Connection to query.
     * @return The name of the database (everything after the last / in the JDBC
     *         URL).
     */
    public static String getShortDatabaseName(Connection con) {

        String url = null;

        try {
            url = con.getMetaData().getURL();
        } catch (SQLException se) {
            throw new SqlUncheckedException("Could not get database name", se);
        }
        String name = url.substring(url.lastIndexOf('/') + 1);

        return name;

    } // getShortDatabaseName

    // -------------------------------------------------------------------------
    /**
     * Scans through a result set's metadata in an attempt to find a column
     * 
     * @param rs
     *            The ResultSet to scan
     * @param column
     *            The column to find
     * @return Boolean indicating if there was a column with said name
     * @throws SQLException
     *             Thrown in the event of an error whilst processing
     */
    public static boolean resultSetContainsColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int total = meta.getColumnCount();
        for (int i = 1; i <= total; i++) {
            if (meta.getColumnName(i).equals(column)) {
                return true;
            }
        }
        return false;
    } // resultSetContainsColumn

    // -------------------------------------------------------------------------
    /**
     * Generate a name for a temporary database. Should be fairly unique; name
     * is _temp_{user}_{time} where user is current user and time is current
     * time in ms.
     * 
     * @return The temporary name. Will not have any spaces.
     */
    public static String generateTempDatabaseName() {

        StringBuffer buf = new StringBuffer("_temp_");
        buf.append(System.getProperty("user.name"));
        buf.append("_" + System.currentTimeMillis());
        String str = buf.toString();
        str = str.replace(' ', '_'); // filter any spaces

        logger.fine("Generated temporary database name: " + str);

        return str;

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of all the table names.
     * 
     * @param con
     *            The database connection to use.
     * @return An array of Strings representing the names of the base tables.
     */
    public static String[] getTableNames(Connection con) {

        List<String> result = getSqlTemplate(con).queryForDefaultObjectList(
                "SELECT TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'",
                String.class);
        return result.toArray(new String[] {});

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of the table names that match a particular SQL pattern.
     * 
     * @param con
     *            The database connection to use.
     * @param pattern
     *            The SQL pattern to match the table names against.
     * @return An array of Strings representing the names of the tables.
     */
    public static String[] getTableNames(Connection con, String pattern) {

        List<String> result = getSqlTemplate(con).queryForDefaultObjectList("SHOW TABLES LIKE '" + pattern + "'",
                String.class);
        return result.toArray(new String[] {});

    }

    // -------------------------------------------------------------------------
    /**
     * List the columns in a particular table.
     * 
     * @param table
     *            The name of the table to list.
     * @param con
     *            The connection to use.
     * @return A List of Strings representing the column names.
     */
    public static List<String> getColumnsInTable(Connection con, String table) {

        return getSqlTemplate(con).queryForDefaultObjectList("DESCRIBE " + table, String.class);

    }

    // -------------------------------------------------------------------------

    /**
     * List the column information in a table - names, types, defaults etc.
     * 
     * @param table
     *            The name of the table to list.
     * @param con
     *            The connection to use.
     * @param typeFilters
     *            If not empty, only return columns whose types start with this
     *            string (case insensitive).
     * @return A List of 6-element String[] arrays representing: 0: Column name
     *         1: Type 2: Null? 3: Key 4: Default 5: Extra
     */
    public static List<String[]> getTableInfo(Connection con, String table, String[] typeFilters) {
        List<String[]> results = getSqlTemplate(con).queryForList("DESCRIBE " + table, new RowMapper<String[]>() {

            @Override
            public String[] mapRow(ResultSet rs, int position) throws SQLException {
                String[] info = new String[6];
                for (int i = 0; i < 6; i++) {
                    info[i] = rs.getString(i + 1);
                }
                return info;
            }
        });

        if (typeFilters != null && typeFilters.length > 0) {
            for (Iterator<String[]> i = results.iterator(); i.hasNext();) {
                String[] info = i.next();
                boolean passed = false;
                for (String typeFilter : typeFilters) {
                    typeFilter = typeFilter.toLowerCase();
                    if (info[1].toLowerCase().startsWith(typeFilter)) {
                        passed = true;
                        break;
                    }
                }
                if (!passed) {
                    i.remove();
                }
            }
        }
        return results;
    }

    /**
     * List the column information in a table - names, types, defaults etc.
     * Delegates to the array based version of this code
     * 
     * @param table
     *            The name of the table to list.
     * @param con
     *            The connection to use.
     * @param typeFilter
     *            If not null, only return columns whose types start with this
     *            string (case insensitive). Vargs so specify as many as you
     *            need
     * 
     * @see #getTableInfo(Connection, String, String[])
     * @return A List of 6-element String[] arrays representing: 0: Column name
     *         1: Type 2: Null? 3: Key 4: Default 5: Extra
     */
    public static List<String[]> getTableInfo(Connection con, String table, String typeFilter) {
        return getTableInfo(con, table, new String[] { typeFilter });
    }

    /**
     * Requests all known views in the current schema from the MySQL information
     * schema
     * 
     * @param con
     *            The connection to use
     * @return All known views in the given schema
     */
    public static List<String> getViews(Connection con) {
        String sql = "SELECT TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA = DATABASE() AND TABLE_TYPE =?";
        SqlTemplate t = new ConnectionBasedSqlTemplateImpl(con);
        return t.queryForDefaultObjectList(sql, String.class, "VIEW");
    }

    // -------------------------------------------------------------------------
    /**
     * Execute SQL and writes results to ReportManager.info().
     * 
     * @param testCase
     *            testCase which created the sql statement
     * @param con
     *            connection to execute sql on.
     * @param sql
     *            sql statement to execute.
     */
    public static void printRows(EnsTestCase testCase, Connection con, String sql) {

        ResultSet rs = null;
        try {
            rs = con.createStatement().executeQuery(sql);
            if (rs.next()) {
                int nCols = rs.getMetaData().getColumnCount();
                StringBuffer line = new StringBuffer();
                do {
                    line.delete(0, line.length());
                    for (int i = 1; i <= nCols; ++i) {
                        line.append(rs.getString(i));
                        if (i < nCols) {
                            line.append("\t");
                        }

                    }
                    ReportManager.info(testCase, con, line.toString());
                } while (rs.next());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs);
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Get the meta_value for a named key in the meta table.
     */
    public static String getMetaValue(Connection con, String key) {

        List<String> results = getSqlTemplate(con)
                .queryForDefaultObjectList("SELECT meta_value FROM meta WHERE meta_key='" + key + "'", String.class);
        return CollectionUtils.getFirstElement(results, StringUtils.EMPTY);

    }

    // -------------------------------------------------------------------------
    /**
     * <p>
     * Depends on system properties being set by a call like
     * </p>
     * 
     * <pre>
     * Utils.readPropertiesFileIntoSystem(getPropertiesFile(), false);
     * </pre>
     * 
     * <p>
     * New code should use the method getMainDatabaseServersConf which gets
     * configuration information from a configuration object. If all goes well,
     * this method will eventually become deprecated.
     * </p>
     * 
     */
    public static List<DatabaseServer> getMainDatabaseServers() {

        if (mainDatabaseServers == null) {

            if (DBUtils.hostConfiguration == null) {

                if (useDefaultsFromFile) {
                    return getMainDatabaseServersProperties();
                } else {

                    // If nothing was preconfigured and defaults should not be
                    // used, return empty list.
                    //
                    mainDatabaseServers = new ArrayList<DatabaseServer>();
                }
            } else {
                return getMainDatabaseServersConf();
            }
        }
        return mainDatabaseServers;
    }

    public static List<DatabaseServer> getMainDatabaseServersProperties() {

        // EG replace literal reference to file with variable
        Utils.readPropertiesFileIntoSystem(TestRunner.getPropertiesFile(), false);

        if (mainDatabaseServers == null) {

            mainDatabaseServers = new ArrayList<DatabaseServer>();

            checkAndAddDatabaseServer(mainDatabaseServers, "host", "port", "user", "password", "driver");
            checkAndAddDatabaseServer(mainDatabaseServers, "host1", "port1", "user1", "password1", "driver1");
            checkAndAddDatabaseServer(mainDatabaseServers, "host2", "port2", "user2", "password2", "driver2");
        }

        logger.fine("Number of main database servers found: " + mainDatabaseServers.size());

        return mainDatabaseServers;
    }

    public static List<DatabaseServer> getMainDatabaseServersConf() {

        if (DBUtils.hostConfiguration == null) {
            throw new NullPointerException("hostConfiguration is null, so was probably never set!");
        }

        if (mainDatabaseServers == null) {

            mainDatabaseServers = new ArrayList<DatabaseServer>();

            ConfigureHost hostConfiguration = DBUtils.hostConfiguration;

            if (hostConfiguration.isHost() && hostConfiguration.isPort() && hostConfiguration.isUser()) {

                // Passwords handled this way, because it might have not
                // been set, if no password is required. In that case,
                // calling hostConfiguration.getPassword() without checking
                // if it is set, will make this throw an
                // OptionNotPresentException.
                //
                String password = null;

                if (hostConfiguration.isPassword()) {
                    password = hostConfiguration.getPassword();
                }

                checkAndAddDatabaseServerConf(mainDatabaseServers, hostConfiguration.getHost(),
                        hostConfiguration.getPort(), hostConfiguration.getUser(), password,
                        hostConfiguration.getDriver());
            }

            if (hostConfiguration.isHost1() && hostConfiguration.isPort1() && hostConfiguration.isUser1()) {

                String password = null;

                if (hostConfiguration.isPassword1()) {
                    password = hostConfiguration.getPassword1();
                }

                checkAndAddDatabaseServerConf(mainDatabaseServers, hostConfiguration.getHost1(),
                        hostConfiguration.getPort1(), hostConfiguration.getUser1(), password,
                        hostConfiguration.getDriver1());
            }

            if (hostConfiguration.isHost2() && hostConfiguration.isPort2() && hostConfiguration.isUser2()) {

                String password = null;

                if (hostConfiguration.isPassword2()) {
                    password = hostConfiguration.getPassword2();
                }

                checkAndAddDatabaseServerConf(mainDatabaseServers, hostConfiguration.getHost2(),
                        hostConfiguration.getPort2(), hostConfiguration.getUser2(), password,
                        hostConfiguration.getDriver2());
            }
        }
        return mainDatabaseServers;
    }

    // -------------------------------------------------------------------------
    /**
     * 
     * <p>
     * Check for the existence of a particular database server. Assumes
     * properties file has already been read in. If it exists, add it to the
     * list.
     * </p>
     * 
     * <p>
     * Gets called by getMainDatabaseServers() and should not be used any
     * further, because it uses system properties.
     * </p>
     * 
     */
    private static void checkAndAddDatabaseServer(List<DatabaseServer> servers, String hostProp, String portProp,
            String userProp, String passwordProp, String driverProp) {

        if (System.getProperty(hostProp) != null && System.getProperty(portProp) != null
                && System.getProperty(userProp) != null) {

            DatabaseServer server = new DatabaseServer(System.getProperty(hostProp), System.getProperty(portProp),
                    System.getProperty(userProp), System.getProperty(passwordProp), System.getProperty(driverProp));
            servers.add(server);
            logger.fine("Added server: " + server.toString());
        }
    }

    public static List<DatabaseServer> getSecondaryDatabaseServers() {
        if (DBUtils.hostConfiguration == null) {
            return getSecondaryDatabaseServersProperties();
        } else {
            return getSecondaryDatabaseServersConf();
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Look for secondary database servers.
     */
    public static List<DatabaseServer> getSecondaryDatabaseServersProperties() {

        if (secondaryDatabaseServers == null) {

            Utils.readPropertiesFileIntoSystem(TestRunner.getPropertiesFile(), false);

            secondaryDatabaseServers = new ArrayList<DatabaseServer>();

            checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host", "secondary.port", "secondary.user",
                    "secondary.password", "secondary.driver");

            checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host1", "secondary.port1", "secondary.user1",
                    "secondary.password1", "secondary.driver1");

            checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host2", "secondary.port2", "secondary.user2",
                    "secondary.password2", "secondary.driver2");

            logger.fine("Number of secondary database servers found: " + secondaryDatabaseServers.size());

        }

        return secondaryDatabaseServers;

    }

    public static void overrideSecondaryDatabaseServer(DatabaseServer srv) {
        secondaryDatabaseServers = new ArrayList<DatabaseServer>();
        secondaryDatabaseServers.add(srv);
    }

    public static void overrideMainDatabaseServer(DatabaseServer srv, boolean clear) {
        if(clear)
            mainDatabaseServers = new ArrayList<DatabaseServer>();
        mainDatabaseServers.add(srv);
    }

    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    /**
     * 
     * Look for secondary database servers.
     * 
     */
    public static List<DatabaseServer> getSecondaryDatabaseServersConf() {

        if (secondaryDatabaseServers == null) {

            secondaryDatabaseServers = new ArrayList<DatabaseServer>();

            boolean secondaryHostConfigured = DBUtils.hostConfiguration.isSecondaryHost()
                    && DBUtils.hostConfiguration.isSecondaryPort() && DBUtils.hostConfiguration.isSecondaryUser()
                    && DBUtils.hostConfiguration.isSecondaryPassword() && DBUtils.hostConfiguration.isSecondaryDriver();

            if (secondaryHostConfigured) {

                logger.config("Adding database " + DBUtils.hostConfiguration.getSecondaryHost());

                checkAndAddDatabaseServerConf(secondaryDatabaseServers, DBUtils.hostConfiguration.getSecondaryHost(),
                        DBUtils.hostConfiguration.getSecondaryPort(), DBUtils.hostConfiguration.getSecondaryUser(),
                        DBUtils.hostConfiguration.getSecondaryPassword(),
                        DBUtils.hostConfiguration.getSecondaryDriver());
            } else {

                logger.config("No secondary database configured.");
            }
        }

        logger.fine("Number of secondary database servers found: " + secondaryDatabaseServers.size());

        return secondaryDatabaseServers;
    }

    /**
     * 
     * <p>
     * Adds a DatabaseServer object to the List<DatabaseServer> passed as the
     * first argument.
     * </p>
     * 
     * @param servers
     * @param host
     * @param port
     * @param user
     * @param password
     * @param driver
     * 
     */
    private static void checkAndAddDatabaseServerConf(List<DatabaseServer> servers, String host, String port,
            String user, String password, String driver) {

        DatabaseServer server = new DatabaseServer(host, port, user, password, driver);

        if (server.isConnectedSuccessfully()) {

            servers.add(server);
            logger.fine("Added server: " + server.toString());

        } else {

            logger.fine("Couldn't connect to server: " + server.toString());
        }
    }

    // -------------------------------------------------------------------------

    public static DatabaseRegistry getSecondaryDatabaseRegistry() {

        if (secondaryDatabaseRegistry == null) {

            secondaryDatabaseRegistry = new DatabaseRegistry(null, null, null, true);

        }

        return secondaryDatabaseRegistry;

    }

    // -------------------------------------------------------------------------

    public static DatabaseRegistry getSecondaryDatabaseRegistry(String regexp) {

        List<String> regexps = new ArrayList<String>();
        regexp = "%" + regexp + "%";
        regexps.add(regexp);

        if (secondaryDatabaseRegistry == null) {

            secondaryDatabaseRegistry = new DatabaseRegistry(regexps, null, null, true);

        }

        return secondaryDatabaseRegistry;

    }

    // -------------------------------------------------------------------------

    public static void setMainDatabaseRegistry(DatabaseRegistry dbr) {

        mainDatabaseRegistry = dbr;

    }

    // -------------------------------------------------------------------------

    public static DatabaseRegistry getMainDatabaseRegistry() {

        if (mainDatabaseRegistry == null) {

            mainDatabaseRegistry = new DatabaseRegistry(null, null, null, false);

        }

        return mainDatabaseRegistry;

    }

    // -------------------------------------------------------------------------

    public static void setSecondaryDatabaseRegistry(DatabaseRegistry dbr) {

        secondaryDatabaseRegistry = dbr;

    }

    // -------------------------------------------------------------------------

    public static boolean tableExists(Connection con, String table) {

        boolean result = false;
        ResultSet rs = null;
        try {

            DatabaseMetaData dbm = con.getMetaData();
            rs = dbm.getTables(null, null, table, null);

            if (rs.next()) {
                result = true;
            }

        } catch (SQLException e) {
            throw new SqlUncheckedException("Could not check for table " + table, e);
        } finally {
            closeQuietly(rs);
        }

        return result;

    }

    // -------------------------------------------------------------------------

    public static boolean columnExists(Connection con, String table, String column) {

        boolean result = false;
        ResultSet rs = null;
        try {

            DatabaseMetaData dbm = con.getMetaData();
            rs = dbm.getColumns(null, null, table, column);

            if (rs.next()) {
                result = true;
            }

        } catch (SQLException e) {
            throw new SqlUncheckedException("Could not check for table " + table, e);
        } finally {
            closeQuietly(rs);
        }

        return result;

    }

    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static void closeQuietly(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static void closeQuietly(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Produce an instance of {@link SqlTemplate} from a
     * {@link DatabaseRegistryEntry}.
     */
    public static SqlTemplate getSqlTemplate(DatabaseRegistryEntry dbre) {
        return new ConnectionBasedSqlTemplateImpl(dbre);
    }

    /**
     * Produce an instance of {@link SqlTemplate} from a {@link Connection}.
     */
    public static SqlTemplate getSqlTemplate(Connection conn) {
        return new ConnectionBasedSqlTemplateImpl(conn);
    }

    // -------------------------------------------------------------------------
    /**
     * Count the number of rows in a table.
     * 
     * @param con
     *            The database connection to use. Should have been opened
     *            already.
     * @param table
     *            The name of the table to analyse.
     * @return The number of rows in the table.
     */
    public static int countRowsInTable(Connection con, String table) {

        if (con == null) {
            logger.severe("countRowsInTable: Database connection is null");
        }

        return getRowCount(con, "SELECT COUNT(*) FROM " + table);

    } // countRowsInTable

    // -------------------------------------------------------------------------
    /**
     * Use SELECT COUNT(*) to get a row count.
     */
    public static int getRowCountFast(Connection con, String sql) {

        return getSqlTemplate(con).queryForDefaultObject(sql, Integer.class);

    } // getRowCountFast

    // -------------------------------------------------------------------------
    /**
     * Use a row-by-row approach to counting the rows in a table.
     */
    public static int getRowCountSlow(Connection con, String sql) {

        int result = -1;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs != null) {
                if (rs.last()) {
                    result = rs.getRow();
                } else {
                    result = 0; // probably signifies an empty ResultSet
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new SqlUncheckedException("Could not retrieve row count", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }

        return result;

    } // getRowCountSlow

    // -------------------------------------------------------------------------
    /**
     * Count the rows in a particular table or query.
     * 
     * @param con
     *            A connection to the database. Should already be open.
     * @param sql
     *            The SQL to execute. Note that if possible this should begin
     *            with <code>SELECT COUNT FROM</code> since this is much quicker
     *            to execute. If a standard SELECT statement is used, a
     *            row-by-row count will be performed, which may be slow if the
     *            table is large.
     * @return The number of matching rows, or -1 if the query did not execute
     *         for some reason.
     */
    public static int getRowCount(Connection con, String sql) {

        if (con == null) {
            logger.severe("getRowCount: Database connection is null");
        }
        int result = -1;

        // if the query starts with SELECT COUNT and does not include a GROUP
        // BY clause
        // we can execute it and just take the first result, which is the count
        if (sql.toLowerCase().contains("select count") && !sql.toLowerCase().contains("group by")) {

            result = getRowCountFast(con, sql);

        } else if (!sql.toLowerCase().contains("select count")) {
            // otherwise, do it row-by-row
            logger.fine(
                    "getRowCount() executing SQL which does not appear to begin with SELECT COUNT - performing row-by-row count, which may take a long time if the table is large.");
            result = getRowCountSlow(con, sql);

        } else if (sql.toLowerCase().contains("select count") && sql.toLowerCase().contains("group by")) {
            // query has both SELECT COUNT and GROUP BY clause
            logger.fine(
                    "getRowCount() executing SQL which appears to begin with SELECT COUNT and contains GROUP BY clause - performing row-by-row count, which may take a long time if the table is large.");
            result = getRowCountSlow(con, sql);

        }

        return result;

    } // getRowCount

    // -------------------------------------------------------------------------
    /**
     * Execute a SQL statement and return the value of one column of one row.
     * Only the FIRST row matched is returned.
     * 
     * @param con
     *            The Connection to use.
     * @param sql
     *            The SQL to check; should return ONE value.
     * @return The value returned by the SQL.
     */
    public static String getRowColumnValue(Connection con, String sql) {

        return CollectionUtils.getFirstElement(getSqlTemplate(con).queryForDefaultObjectList(sql, String.class),
                StringUtils.EMPTY);

    } // DBUtils.getRowColumnValue

    // -------------------------------------------------------------------------
    /**
     * Execute a SQL statement and return the value of the columns of one row.
     * Only the FIRST row matched is returned.
     * 
     * @param con
     *            The Connection to use.
     * @param sql
     *            The SQL to check; can return several values.
     * @return The value(s) returned by the SQL in an array of Strings.
     */
    public static String[] getRowValues(Connection con, String sql) {
        List<String[]> v = getRowValuesList(con, sql);

        if (v.isEmpty()) {
            return new String[] {};
        }

        return v.get(0);
    } // getRowValues

    /**
     * Returns a List of String arrays for working with multiple values
     * 
     * @param con
     *            Connection to use
     * @param sql
     *            SQL to run; can return several values
     * @return Returns a list of values
     */
    public static List<String[]> getRowValuesList(Connection con, String sql) {
        return getSqlTemplate(con).queryForList(sql, new RowMapper<String[]>() {
            @Override
            public String[] mapRow(ResultSet resultSet, int position) throws SQLException {
                int length = resultSet.getMetaData().getColumnCount();
                String[] values = new String[length];
                for (int sqlIndex = 1, arrayIndex = 0; sqlIndex <= length; sqlIndex++, arrayIndex++) {
                    values[arrayIndex] = resultSet.getString(sqlIndex);
                }
                return values;
            }
        });
    }

    // -------------------------------------------------------------------------
    /**
     * Execute a SQL statement and return the values of one column of the
     * result.
     * 
     * @param con
     *            The Connection to use.
     * @param sql
     *            The SQL to check; should return ONE column.
     * @return The value(s) making up the column, in the order that they were
     *         read.
     */
    public static String[] getColumnValues(Connection con, String sql) {

        return getColumnValuesList(con, sql).toArray(new String[] {});

    } // getColumnValues

    // -------------------------------------------------------------------------
    /**
     * Execute a SQL statement and return the values of one column of the
     * result.
     * 
     * @param con
     *            The Connection to use.
     * @param sql
     *            The SQL to check; should return ONE column.
     * @return The value(s) making up the column, in the order that they were
     *         read.
     */
    public static List<String> getColumnValuesList(Connection con, String sql) {

        return getSqlTemplate(con).queryForDefaultObjectList(sql, String.class);

    } // getColumnValues

    // -------------------------------------------------------------------------
    /**
     * Check for the presence of a particular String in a table column.
     * 
     * @param con
     *            The database connection to use.
     * @param table
     *            The name of the table to examine.
     * @param column
     *            The name of the column to look in.
     * @param str
     *            The string to search for; can use database wildcards (%, _)
     *            Note that if you want to search for one of these special
     *            characters, it must be backslash-escaped.
     * @return The number of times the string is matched.
     */
    public static int findStringInColumn(Connection con, String table, String column, String str) {

        if (con == null) {
            logger.severe("findStringInColumn: Database connection is null");
        }

        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " LIKE \"" + str + "\"";
        logger.fine(sql);

        return getRowCount(con, sql);

    } // findStringInColumn

    // -------------------------------------------------------------------------
    /**
     * Check that all entries in column match a particular pattern.
     * 
     * @param con
     *            The database connection to use.
     * @param table
     *            The name of the table to examine.
     * @param column
     *            The name of the column to look in.
     * @param pattern
     *            The SQL pattern (can contain _,%) to look for.
     * @return The number of columns that <em>DO NOT</em> match the pattern.
     */
    public static int checkColumnPattern(Connection con, String table, String column, String pattern) {

        // @todo - what about NULLs?

        // cheat by looking for any rows that DO NOT match the pattern
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " NOT LIKE \"" + pattern + "\"";
        logger.fine(sql);

        return getRowCount(con, sql);

    } // checkColumnPattern

    // -------------------------------------------------------------------------
    /**
     * Check that all entries in column match a particular value.
     * 
     * @param con
     *            The database connection to use.
     * @param table
     *            The name of the table to examine.
     * @param column
     *            The name of the column to look in.
     * @param value
     *            The string to look for (not a pattern).
     * @return The number of columns that <em>DO NOT</em> match value.
     */
    public static int checkColumnValue(Connection con, String table, String column, String value) {

        // @todo - what about NULLs?

        // cheat by looking for any rows that DO NOT match the pattern
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " != '" + value + "'";
        logger.fine(sql);

        return getRowCount(con, sql);

    } // checkColumnPattern

    // -------------------------------------------------------------------------
    /**
     * Check if there are any blank entires in a column that is not supposed to
     * be null.
     * 
     * @param con
     *            The database connection to use.
     * @param table
     *            The table to use.
     * @param column
     *            The column to examine.
     * @return An list of the row indices of any blank entries. Will be
     *         zero-length if there are none.
     */
    public static List<String> checkBlankNonNull(Connection con, String table, String column) {

        if (con == null) {
            logger.severe("checkBlankNonNull (column): Database connection is null");
            return null;
        }

        List<String> blanks = new ArrayList<String>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT " + column + " FROM " + table;
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                String columnValue = rs.getString(1);
                // should it be non-null?
                if (rsmd.isNullable(1) == ResultSetMetaData.columnNoNulls) {
                    if (StringUtils.isEmpty(columnValue)) {
                        blanks.add(Integer.toString(rs.getRow()));
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new SqlUncheckedException("Could not check blanks or nulls", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }

        return blanks;

    } // checkBlankNonNull

    // -------------------------------------------------------------------------
    /**
     * Check all columns of a table for blank entires in columns that are marked
     * as being NOT NULL.
     * 
     * @param con
     *            The database connection to use.
     * @param table
     *            The table to use.
     * @return The total number of blank null enums.
     */
    public static int checkBlankNonNull(Connection con, String table) {

        if (con == null) {
            logger.severe("checkBlankNonNull (table): Database connection is null");
            return 0;
        }

        int blanks = 0;

        String sql = "SELECT * FROM " + table;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String columnValue = rs.getString(i);
                    String columnName = rsmd.getColumnName(i);
                    // should it be non-null?
                    if (rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls) {
                        if (columnValue == null || columnValue.equals("")) {
                            blanks++;
                            logger.warning("Found blank non-null value in column " + columnName + " in " + table);
                        }
                    }
                } // for column
            }
        } catch (Exception e) {
            throw new SqlUncheckedException("Could not check for blank non-nulls", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }

        return blanks;

    } // checkBlankNonNull

    // -------------------------------------------------------------------------
    /**
     * Check if a particular table exists in a database.
     * 
     * @param con
     *            The database connection to check.
     * @param table
     *            The table to check for.
     * @return true if the table exists in the database.
     */
    public static boolean checkTableExists(Connection con, String table) {

        String tables = DBUtils.getRowColumnValue(con, "SHOW TABLES LIKE '" + table + "'");

        boolean result = false;
        if (tables != null && tables.length() != 0) {
            result = true;
        }

        return result;

    } // checkTableExists

    /**
     * @param con
     * @param tableName
     * @return checksum for table
     */
    public static long getChecksum(Connection con, String tableName) {
        String sql = "CHECKSUM TABLE " + tableName;
        RowMapper<Long> mapper = new DefaultObjectRowMapper<Long>(Long.class, 2);
        return getSqlTemplate(con).queryForObject(sql, mapper);
    }

    // -------------------------------------------------------------------------
    
	/**
	 * Read a database schema from a file and create a temporary database from
	 * it.
	 * 
	 * @param fileName
	 *            The name of the schema to read.
	 * @return A connection to a database built from the schema.
	 * @throws FileNotFoundException
	 */
	public static Connection importSchema(String fileName, String databaseURL, String user, String password)
			throws FileNotFoundException {
		Connection con = null;

		// ----------------------------------------------------
		// Parse the file first in case there are problems
		SQLParser sqlParser = new SQLParser();

		// try {
		List sqlCommands = sqlParser.parse(fileName);
		// sqlParser.printLines();
		// } catch (FileNotFoundException fnfe) {
		// fnfe.printStackTrace();
		// }

		// ----------------------------------------------------
		// create the database

		String tempDBName = DBUtils.generateTempDatabaseName();

		try {

			Class.forName(System.getProperty("driver"));


			Connection tmpCon = DriverManager.getConnection(databaseURL, user,
					password);

			String sql = "CREATE DATABASE " + tempDBName;
			logger.finest(sql);
			Statement stmt = tmpCon.createStatement();
			stmt.execute(sql);
			logger.fine("Database " + tempDBName + " created!");

			// close the temporary connection and create a "real" one
			tmpCon.close();
			con = DriverManager.getConnection(databaseURL + tempDBName, user,
					password);

		} catch (Exception e) {
			String msg = "Could not create database " + tempDBName;
			logger.severe(msg);
			throw new RuntimeException(msg, e);
		}

		// ----------------------------------------------------
		// Build the schema

		try {

			Statement stmt = con.createStatement();

			// Fill the batch of SQL commands
			stmt = sqlParser.populateBatch(stmt);

			// execute the batch that has been built up previously
			logger.info("Creating temporary database ...");
			stmt.executeBatch();
			logger.info("Done.");

			// close statement
			stmt.close();

		} catch (Exception e) {

			String msg = "Could not load schema for database " + tempDBName;
			logger.severe(msg);
			throw new RuntimeException(msg, e);

		}

		return con;

	}
	
	public static DatabaseRegistryEntry importSchema(String databaseName, String fileName, String databaseURL, String user, String password)
			throws FileNotFoundException {
		Connection con  = importSchema(fileName, databaseURL, user, password);
		DatabaseInfo info = new DatabaseInfo(databaseName, null, null, null, null, null);
		return new DatabaseRegistryEntry(info, con);		
	}

} // DBUtils
