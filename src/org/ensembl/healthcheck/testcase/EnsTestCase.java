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

package org.ensembl.healthcheck.testcase;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;
import org.ensembl.healthcheck.util.SQLParser;
import org.ensembl.healthcheck.util.Utils;

/**
 * Base class for all healthcheck tests.
 */

public abstract class EnsTestCase {

    /** The TestRunner associated with this EnsTestCase */
    protected TestRunner testRunner;

    /**
     * A list of Strings representing the groups that this test is a member of.
     * Every test is (at least) a member of a group with the same name as the
     * test.
     */
    protected List groups;

    /** Description field */
    protected String description;

    /** Logger object to use */
    protected static Logger logger = Logger.getLogger("HealthCheckLogger");

    /**
     * Boolean variable that can be set if the test case is likely to take a
     * long time to run
     */
    protected boolean hintLongRunning = false;

    /**
     * Store a list of which types of database this test applies to.
     */
    protected List appliesToTypes = new ArrayList();

    // -------------------------------------------------------------------------
    /**
     * Creates a new instance of EnsTestCase
     */
    public EnsTestCase() {

        groups = new ArrayList();
        addToGroup(getShortTestName()); // each test is in a one-test group
        setDescription("No description set for this test.");

    } // EnsTestCase

    // -------------------------------------------------------------------------

    /**
     * Get the TestRunner that is controlling this EnsTestCase.
     * 
     * @return The parent TestRunner.
     */
    public TestRunner getTestRunner() {

        return testRunner;

    } // getTestRunner

    // -------------------------------------------------------------------------

    /**
     * Sets up this test. <B>Must </B> be called before the object is used.
     * 
     * @param tr
     *          The TestRunner to associate with this test. Usually just <CODE>
     *          this</CODE> if being called from the TestRunner.
     */
    public void init(TestRunner tr) {

        this.testRunner = tr;

    } // init

    // -------------------------------------------------------------------------

    /**
     * Gets the full name of this test.
     * 
     * @return The full name of the test, e.g.
     *         org.ensembl.healthcheck.EnsTestCase
     */
    public String getTestName() {

        return this.getClass().getName();

    }

    // -------------------------------------------------------------------------

    /**
     * Gets the full name of this test.
     * 
     * @return The full name of the test, e.g.
     *         org.ensembl.healthcheck.EnsTestCase
     */
    public String getName() {

        return this.getClass().getName();

    }

    // -------------------------------------------------------------------------
    /**
     * Get the short form of the test name, ie the name of the test class
     * without the package qualifier.
     * 
     * @return The short test name, e.g. EnsTestCase
     */
    public String getShortTestName() {

        String longName = getTestName();

        return longName.substring(longName.lastIndexOf('.') + 1);

    }

    // -------------------------------------------------------------------------
    /**
     * Get the very short form of the test name; ie that returned by
     * getShortTestName() without the trailing "TestCase"
     * 
     * @return The very short test name, e.g. CheckMetaTables
     */
    public String getVeryShortTestName() {

        String name = getShortTestName();

        return name.substring(0, name.lastIndexOf("TestCase"));

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of the names of the groups which this test case is a member
     * of.
     * 
     * @return The list of names as Strings.
     */
    public List getGroups() {

        return groups;

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of the groups that this test case is a member of, formatted
     * for easy printing.
     * 
     * @return The comma-separated list of group names.
     */
    public String getCommaSeparatedGroups() {

        StringBuffer gString = new StringBuffer();

        java.util.Iterator it = groups.iterator();
        while (it.hasNext()) {
            gString.append((String) it.next());
            if (it.hasNext()) {
                gString.append(",");
            }
        }
        return gString.toString();
    }

    // -------------------------------------------------------------------------
    /**
     * Convenience method for assigning this test case to several groups at
     * once.
     * 
     * @param s
     *          A list of Strings containing the group names.
     */
    public void setGroups(List s) {

        groups = s;

    }

    // -------------------------------------------------------------------------
    /**
     * Convenience method for assigning this test case to several groups at
     * once.
     * 
     * @param s
     *          Array of group names.
     */
    public void setGroups(String[] s) {

        for (int i = 0; i < s.length; i++) {
            groups.add(s[i]);
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Add this test case to a new group. If the test case is already a member
     * of the group, a warning is printed and it is not added again.
     * 
     * @param newGroupName
     *          The name of the new group.
     */
    public void addToGroup(String newGroupName) {

        if (!groups.contains(newGroupName)) {
            groups.add(newGroupName);
        } else {
            logger.warning(getTestName() + " is already a member of " + newGroupName + " not added again.");
        }

    } // addToGroup

    // -------------------------------------------------------------------------
    /**
     * Remove this test case from the specified group. If the test case is not
     * a member of the specified group, a warning is printed.
     * 
     * @param groupName
     *          The name of the group from which this test case is to be
     *          removed.
     */
    public void removeFromGroup(String groupName) {

        if (groups.contains(groupName)) {
            groups.remove(groupName);
        } else {
            logger.warning(getTestName() + " was not a memeber of " + groupName);
        }

    } // removeFromGroup

    // -------------------------------------------------------------------------
    /**
     * Test if this test case is a member of a particular group.
     * 
     * @param group
     *          The name of the group to check.
     * @return True if this test case is a member of the named group, false
     *         otherwise.
     */
    public boolean inGroup(String group) {

        return groups.contains(group);

    }

    // -------------------------------------------------------------------------
    /**
     * Convenience method for checking if this test case belongs to any of
     * several groups.
     * 
     * @param checkGroups
     *          The list of group names to check.
     * @return True if this test case is in any of the groups, false if it is
     *         in none.
     */
    public boolean inGroups(List checkGroups) {

        boolean result = false;

        java.util.Iterator it = checkGroups.iterator();
        while (it.hasNext()) {
            if (inGroup((String) it.next())) {
                result = true;
            }
        }
        return result;

    } // inGroups

    // -------------------------------------------------------------------------
    /**
     * Count the number of rows in a table.
     * 
     * @param con
     *          The database connection to use. Should have been opened
     *          already.
     * @param table
     *          The name of the table to analyse.
     * @return The number of rows in the table.
     */
    public int countRowsInTable(Connection con, String table) {

        if (con == null) {
            logger.severe("countRowsInTable: Database connection is null");
        }

        return getRowCount(con, "SELECT COUNT(*) FROM " + table);

    } // countRowsInTable

    // -------------------------------------------------------------------------
    /**
     * Use SELECT COUNT(*) to get a row count.
     */
    private int getRowCountFast(Connection con, String sql) {

        int result = -1;

        try {
            Statement stmt = con.createStatement();
            //System.out.println("Executing " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs != null) {
                if (rs.first()) {
                    result = rs.getInt(1);
                } else {
                    result = -1; // probably signifies an empty ResultSet
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    } // getRowCountFast

    // -------------------------------------------------------------------------
    /**
     * Use a row-by-row approach to counting the rows in a table.
     */
    private int getRowCountSlow(Connection con, String sql) {

        int result = -1;

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs != null) {
                if (rs.last()) {
                    result = rs.getRow();
                } else {
                    result = -1; // probably signifies an empty ResultSet
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    } // getRowCountSlow

    // -------------------------------------------------------------------------
    /**
     * Count the rows in a particular table or query.
     * 
     * @param con
     *          A connection to the database. Should already be open.
     * @param sql
     *          The SQL to execute. Note that if possible this should begin
     *          with <code>SELECT COUNT FROM</code> since this is much
     *          quicker to execute. If a standard SELECT statement is used, a
     *          row-by-row count will be performed, which may be slow if the
     *          table is large.
     * @return The number of matching rows, or -1 if the query did not execute
     *         for some reason.
     */
    public int getRowCount(Connection con, String sql) {

        if (con == null) {
            logger.severe("getRowCount: Database connection is null");
        }
        int result = -1;

        // if the query starts with SELECT COUNT and does not include a GROUP
        // BY clause
        // we can execute it and just take the first result, which is the count
        if (sql.toLowerCase().indexOf("select count") >= 0 && sql.toLowerCase().indexOf("group by") < 0) {

            result = getRowCountFast(con, sql);

        } else {//  otherwise, do it row-by-row

            logger
                    .warning("getRowCount() executing SQL which does not appear to begin with SELECT COUNT - performing row-by-row count, which may take a long time if the table is large.");
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
     *          The Connection to use.
     * @param sql
     *          The SQL to check; should return ONE value.
     * @return The value returned by the SQL.
     */
    public String getRowColumnValue(Connection con, String sql) {

        String result = "";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs != null && rs.first()) {
                result = rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    } // getRowColumnValue

    // -------------------------------------------------------------------------
    /**
     * Execute a SQL statement and return the values of one column of the
     * result.
     * 
     * @param con
     *          The Connection to use.
     * @param sql
     *          The SQL to check; should return ONE column.
     * @return The value(s) making up the column, in the order that they were
     *         read.
     */
    public String[] getColumnValues(Connection con, String sql) {

        ArrayList list = new ArrayList();

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (String[]) list.toArray(new String[list.size()]);

    } // getRowColumnValue

    // -------------------------------------------------------------------------
    /**
     * Verify foreign-key relations.
     * 
     * @param con
     *          A connection to the database to be tested. Should already be
     *          open.
     * @param table1
     *          With col1, specifies the first key to check.
     * @param col1
     *          Column in table1 to check.
     * @param table2
     *          With col2, specifies the second key to check.
     * @param col2
     *          Column in table2 to check.
     * @param oneWayOnly
     *          If false, only a "left join" is performed on table1 and table2.
     *          If false, the
     * @return The number of "orphans"
     */
    public int countOrphans(Connection con, String table1, String col1, String table2, String col2, boolean oneWayOnly) {

        if (con == null) {
            logger.severe("countOrphans: Database connection is null");
        }

        int resultLeft, resultRight;

        String sql = " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "."
                + col2 + " WHERE " + table2 + "." + col2 + " iS NULL";

        resultLeft = getRowCount(con, "SELECT COUNT(*)" + sql);

        if (resultLeft > 0) {
            String[] values = getColumnValues(con, "SELECT " + table1 + "." + col1 + sql + " LIMIT 20");
            for (int i = 0; i < values.length; i++) {
                ReportManager.info(this, con, table1 + "." + col1 + " " + values[i] + " is not linked.");
            }
        }

        if (!oneWayOnly) {
            // and the other way ... (a right join?)
            sql = " FROM " + table2 + " LEFT JOIN " + table1 + " ON " + table2 + "." + col2 + " = " + table1 + "."
                    + col1 + " WHERE " + table1 + "." + col1 + " IS NULL";

            resultRight = getRowCount(con, "SELECT COUNT(*)" + sql);
            if (resultRight > 0) {
                String[] values = getColumnValues(con, "SELECT " + table2 + "." + col2 + sql + " LIMIT 20");
                for (int i = 0; i < values.length; i++) {
                    ReportManager.info(this, con, table2 + "." + col2 + " " + values[i] + " is not linked.");
                }
            }
        } else {
            resultRight = 0;
        }

        logger.finest("Left: " + resultLeft + " Right: " + resultRight);

        return resultLeft + resultRight;

    } // countOrphans

    // -------------------------------------------------------------------------
    /**
     * Check that a particular SQL statement has the same result when executed
     * on more than one database.
     * 
     * @return True if all matched databases provide the same result, false
     *         otherwise.
     * @param sql
     *          The SQL query to execute.
     * @param regexp
     *          A regexp matching the database names to check.
     */
    public boolean checkSameSQLResult(String sql, String regexp) {

        ArrayList resultSetGroup = new ArrayList();
        ArrayList statements = new ArrayList();

        DatabaseConnectionIterator dcit = testRunner.getDatabaseConnectionIterator(regexp);

        while (dcit.hasNext()) {

            Connection con = (Connection) dcit.next();

            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs != null) {
                    resultSetGroup.add(rs);
                }
                logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
                //DBUtils.printResultSet(rs, 100);
                // note that the Statement can't be closed here as we use the
                // ResultSet elsewhere
                // so store a reference to it for closing later
                statements.add(stmt);
                //con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
        boolean same = DBUtils.compareResultSetGroup(resultSetGroup, this);

        Iterator it = statements.iterator();
        while (it.hasNext()) {
            try {
                ((Statement) it.next()).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return same;

    } // checkSameSQLResult

    //-------------------------------------------------------------------------
    /**
     * Check that a particular SQL statement has the same result when executed
     * on more than one database.
     * 
     * @return True if all matched databases provide the same result, false
     *         otherwise.
     * @param sql
     *          The SQL query to execute.
     * @param databases
     *          The DatabaseRegistryEntries on which to execute sql.
     */
    public boolean checkSameSQLResult(String sql, DatabaseRegistryEntry[] databases) {

        ArrayList resultSetGroup = new ArrayList();
        ArrayList statements = new ArrayList();

        for (int i = 0; i < databases.length; i++) {

            Connection con = databases[i].getConnection();

            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs != null) {
                    resultSetGroup.add(rs);
                }
                logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
                //DBUtils.printResultSet(rs, 100);
                // note that the Statement can't be closed here as we use the
                // ResultSet elsewhere
                // so store a reference to it for closing later
                statements.add(stmt);
                //con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
        boolean same = DBUtils.compareResultSetGroup(resultSetGroup, this);

        Iterator it = statements.iterator();
        while (it.hasNext()) {
            try {
                ((Statement) it.next()).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return same;

    } // checkSameSQLResult

    // -------------------------------------------------------------------------
    /**
     * Check for the presence of a particular String in a table column.
     * 
     * @param con
     *          The database connection to use.
     * @param table
     *          The name of the table to examine.
     * @param column
     *          The name of the column to look in.
     * @param str
     *          The string to search for; can use database wildcards (%, _)
     *          Note that if you want to search for one of these special
     *          characters, it must be backslash-escaped.
     * @return The number of times the string is matched.
     */
    public int findStringInColumn(Connection con, String table, String column, String str) {

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
     *          The database connection to use.
     * @param table
     *          The name of the table to examine.
     * @param column
     *          The name of the column to look in.
     * @param pattern
     *          The SQL pattern (can contain _,%) to look for.
     * @return The number of columns that <em>DO NOT</em> match the pattern.
     */
    public int checkColumnPattern(Connection con, String table, String column, String pattern) {

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
     *          The database connection to use.
     * @param table
     *          The name of the table to examine.
     * @param column
     *          The name of the column to look in.
     * @param value
     *          The string to look for (not a pattern).
     * @return The number of columns that <em>DO NOT</em> match value.
     */
    public int checkColumnValue(Connection con, String table, String column, String value) {

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
     *          The database connection to use.
     * @param table
     *          The table to use.
     * @param column
     *          The column to examine.
     * @return An list of the row indices of any blank entries. Will be
     *         zero-length if there are none.
     */
    public List checkBlankNonNull(Connection con, String table, String column) {

        if (con == null) {
            logger.severe("checkBlankNonNull (column): Database connection is null");
        }

        ArrayList blanks = new ArrayList();

        String sql = "SELECT " + column + " FROM " + table;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                String columnValue = rs.getString(1);
                // should it be non-null?
                if (rsmd.isNullable(1) == ResultSetMetaData.columnNoNulls) {
                    if (columnValue == null || columnValue.equals("")) {
                        blanks.add("" + rs.getRow());
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blanks;

    } // checkBlankNonNull

    // -------------------------------------------------------------------------
    /**
     * Check all columns of a table for blank entires in columns that are
     * marked as being NOT NULL.
     * 
     * @param con
     *          The database connection to use.
     * @param table
     *          The table to use.
     * @return The total number of blank null enums.
     */
    public int checkBlankNonNull(Connection con, String table) {

        if (con == null) {
            logger.severe("checkBlankNonNull (table): Database connection is null");
        }

        int blanks = 0;

        String sql = "SELECT * FROM " + table;

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
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
            } // while rs
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blanks;

    } // checkBlankNonNull

    // -------------------------------------------------------------------------
    /**
     * Check if a particular table exists in a database.
     * 
     * @param con
     *          The database connection to check.
     * @param table
     *          The table to check for.
     * @return true if the table exists in the database.
     */
    public boolean checkTableExists(Connection con, String table) {

        String tables = getRowColumnValue(con, "SHOW TABLES LIKE '" + table + "'");

        boolean result = false;
        if (tables != null && tables.length() != 0) {
            result = true;
        }

        return result;

    } // checkTableExists

    // -------------------------------------------------------------------------
    /**
     * Print a warning message about a specific database.
     * 
     * @param con
     *          The database connection involved.
     * @param message
     *          The message to print.
     */
    protected void warn(Connection con, String message) {

        logger.warning("Problem in " + DBUtils.getShortDatabaseName(con));
        logger.warning(message);

    } //warn

    // -------------------------------------------------------------------------
    /**
     * Get a list of the databases which represent species. Filter out any
     * which don't seem to represent species.
     * 
     * @return A list of the species; each species will occur only once, and be
     *         of the form homo_sapiens (no trailing _).
     */
    public String[] getListOfSpeciesDatabases() {

        ArrayList list = new ArrayList();

        DatabaseConnectionIterator dbci = testRunner.getDatabaseConnectionIterator(".*");
        while (dbci.hasNext()) {

            String dbName = DBUtils.getShortDatabaseName((Connection) dbci.next());

            String[] bits = dbName.split("_");
            if (bits.length > 2) {
                String species = bits[0] + "_" + bits[1];
                if (!list.contains(species)) {
                    list.add(species);
                }
            } else {
                logger.fine("Database " + dbName + " does not seem to represent a species; ignored");
            }

        }

        return (String[]) list.toArray(new String[list.size()]);

    }

    // -------------------------------------------------------------------------
    /**
     * Get the description.
     * 
     * @return The description for this test.
     */
    public String getDescription() {

        return description;

    } // getDescription

    // -------------------------------------------------------------------------
    /**
     * Set the text description of this test case.
     * 
     * @param s
     *          The new description.
     */
    public void setDescription(String s) {

        description = s;

    } // setDescription

    // -------------------------------------------------------------------------
    /**
     * Read a database schema from a file and create a temporary database from
     * it.
     * 
     * @param fileName
     *          The name of the schema to read.
     * @return A connection to a database buit from the schema.
     */
    public Connection importSchema(String fileName) {

        Connection con = null;

        // ----------------------------------------------------
        // Parse the file first in case there are problems
        SQLParser sqlParser = new SQLParser();

        try {
            List sqlCommands = sqlParser.parse(fileName);
            //sqlParser.printLines();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        // ----------------------------------------------------
        // create the database

        String tempDBName = DBUtils.generateTempDatabaseName();

        // read properties file
        String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator")
                + "database.properties";
        Utils.readPropertiesFileIntoSystem(propsFile);
        logger.fine("Read database properties from " + propsFile);

        try {

            Class.forName(System.getProperty("driver"));
            Connection tmpCon = DriverManager.getConnection(System.getProperty("databaseURL"), System
                    .getProperty("user"), System.getProperty("password"));

            String sql = "CREATE DATABASE " + tempDBName;
            logger.finest(sql);
            Statement stmt = tmpCon.createStatement();
            stmt.execute(sql);
            logger.fine("Database " + tempDBName + " created!");

            // close the temporary connection and create a "real" one
            tmpCon.close();
            con = DriverManager.getConnection(System.getProperty("databaseURL") + tempDBName, System
                    .getProperty("user"), System.getProperty("password"));

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(1);

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

            e.printStackTrace();
            System.exit(1);

        }

        return con;
    }

    // -------------------------------------------------------------------------
    /**
     * Remove a whole database. Generally should *only* be used with temporary
     * databases. Use at your own risk!
     * 
     * @param con
     *          The connection pointing to the database to remove. Should be
     *          connected as a user that has sufficient permissions to remove
     *          it.
     */
    public void removeDatabase(Connection con) {

        String dbName = DBUtils.getShortDatabaseName(con);

        try {

            String sql = "DROP DATABASE " + dbName;
            logger.finest(sql);
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            logger.fine("Database " + dbName + " removed!");

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(1);

        }

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of all the table names.
     * 
     * @param con
     *          The database connection to use.
     * @return An array of Strings representing the names of the tables,
     *         obtained from the SHOW TABLES command.
     */
    public String[] getTableNames(Connection con) {

        return DBUtils.getTableNames(con);

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of the table names that match a particular pattern.
     * 
     * @param con
     *          The database connection to use.
     * @param pattern
     *          The pattern to use - note that this is a <em>SQL</em>
     *          pattern, not a regexp.
     * @return An array of Strings representing the names of the tables that
     *         match the pattern.
     */
    public String[] getTableNames(Connection con, String pattern) {

        return DBUtils.getTableNames(con, pattern);

    }

    // -------------------------------------------------------------------------
    /**
     * Convenience method for getting a connection to a named schema.
     * 
     * @param schema
     *          The name of the schema to connect to.
     * @return A connection to schema.
     */
    public Connection getSchemaConnection(String schema) {

        Connection con = DBUtils.openConnection(System.getProperty("driver"), System.getProperty("databaseURL")
                + schema, System.getProperty("user"), System.getProperty("password"));

        return con;
    }

    // -------------------------------------------------------------------------
    /**
     * Compare two schemas to see if they have the same tables. The comparison
     * is done in both directions, so will return false if a table exists in
     * schema1 but not in schema2, <em>or</em> if a table exists in schema2
     * but not in schema2.
     * 
     * @param schema1
     *          The first schema to compare.
     * @param schema2
     *          The second schema to compare.
     * @return true if all tables in schema1 exist in schema2, and vice-versa.
     */
    public boolean compareTablesInSchema(Connection schema1, Connection schema2) {

        boolean result = true;

        String name1 = DBUtils.getShortDatabaseName(schema1);
        String name2 = DBUtils.getShortDatabaseName(schema2);

        // check each table in turn
        String[] tables = getTableNames(schema1);
        for (int i = 0; i < tables.length; i++) {
            String table = tables[i];
            if (!checkTableExists(schema2, table)) {
                ReportManager.problem(this, schema1, "Table " + table + " exists in " + name1 + " but not in " + name2);
                result = false;
            }
        }
        // and now the other way
        tables = getTableNames(schema2);
        for (int i = 0; i < tables.length; i++) {
            String table = tables[i];
            if (!checkTableExists(schema1, table)) {
                ReportManager.problem(this, schema2, "Table " + table + " exists in " + name2 + " but not in " + name1);
                result = false;
            }
        }

        return result;

    }

    // -------------------------------------------------------------------------

    /**
     * Check if the current test has repair capability. Signified by
     * implementing the Repair interface.
     * 
     * @return True if this test implements Repair, false otherwise.
     */
    public boolean canRepair() {

        return (this instanceof Repair);

    }

    // -------------------------------------------------------------------------
    /**
     * Check if a table has rows.
     * 
     * @param con
     *          The connection to the database to use.
     * @param table
     *          The table to check.
     * @return true if the table has >0 rows, false otherwise.
     */
    public boolean tableHasRows(Connection con, String table) {

        return (getRowCount(con, "SELECT COUNT(*) FROM " + table) > 0);

    }

    // -------------------------------------------------------------------------
    /**
     * See if the "hintLongRunning" flag is set.
     * 
     * @return The value of the hintLongRunning flag.
     */
    public boolean isLongRunning() {

        return hintLongRunning;

    }

    // -------------------------------------------------------------------------
    /**
     * Set the flag that indicates that this test may take a long time to run.
     * 
     * @param b
     *          The new value of the flag.
     */
    public void setHintLongRunning(boolean b) {

        hintLongRunning = b;

    }

    //---------------------------------------------------------------------
    /**
     * Check if this test case applies to a particular DatabaseType.
     */
    public boolean appliesToType(DatabaseType t) {

        Iterator it = appliesToTypes.iterator();
        while (it.hasNext()) {
            DatabaseType type = (DatabaseType) it.next();
            if (t.equals(type)) {
                return true;
            }
        }

        return false;

    }

    // -----------------------------------------------------------------
    /**
     * Add another database type to the list of types that this test case
     * applies to.
     * 
     * @param t
     *          The new type.
     */
    public void addAppliesToType(DatabaseType t) {

        appliesToTypes.add(t);

    }

    // -----------------------------------------------------------------
    /**
     * Remove a database type from the list of types that this test case
     * applies to.
     * 
     * @param t
     *          The type to remove.
     */
    public void removeAppliesToType(DatabaseType t) {

        appliesToTypes.remove(t);

    }

    // -----------------------------------------------------------------
    /**
     * Specify the database types that a test applies to.
     * 
     * @param types
     *          A List of DatabaseTypes - overwrites the current setting.
     */
    public void setAppliesToTypes(List types) {

        appliesToTypes = types;

    }

    // -----------------------------------------------------------------
    /**
     * @return the list of database types that a test applies to.
     */
    public DatabaseType[] getAppliesToTypes() {

        return (DatabaseType[]) appliesToTypes.toArray(new DatabaseType[appliesToTypes.size()]);

    }

    // -----------------------------------------------------------------
    /**
     * Set the database type(s) that this test applies to based upon the
     * directory name. For directories called "generic", the type is set to
     * core, est, estgene and vega. For all other directories the type is set
     * based upon the directory name.
     */
    public void setTypeFromDirName(String dirName) {

        List types = new ArrayList();

        if (dirName.equals("generic")) {

            types.add(DatabaseType.CORE);
            types.add(DatabaseType.VEGA);
            types.add(DatabaseType.EST);
            types.add(DatabaseType.ESTGENE);
            logger.finest("Set generic types for " + getName());

        } else {

            DatabaseType type = DatabaseType.resolveAlias(dirName);
            if (type != DatabaseType.UNKNOWN) {

                types.add(type);
                logger.finest("Set type to " + type.toString() + " for " + getName());
            } else {
                logger.warning("Cannot deduce test type from directory name " + dirName + " for " + getName());
            }
        }

        setAppliesToTypes(types);

    }

    // -------------------------------------------------------------------------

    /**
     * This method can be overridden in subclasses to define (via
     * addAppliesToType/removeAppliesToType) which types of databases the test
     * applies to.
     */
    public void types() {

    };

    // -------------------------------------------------------------------------

} // EnsTestCase
