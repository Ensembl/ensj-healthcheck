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

package org.ensembl.healthcheck;

import java.util.*;
import java.sql.*;
import java.util.logging.*;

import org.ensembl.healthcheck.util.*;

/**
 * <p>The EnsTestCase class is the base class for all test cases in the EnsEMBL Healthcheck system.
 * <em>It is not intended to be instantiated directly</em>; subclasses should implement the <code>run()</code>
 * method and use that to provide test-case specific behaviour.</p>
 *
 *<p>EnsTestCase provides a number of methods which are intended to make writing test cases simple; in many cases
 * an extension test case will involve little more than calling one of the methods in this class and
 * setting some return value based on the result.</p>
 *
 * <p>For example, the following test case gets a {@link org.ensembl.healthcheck.util.DatabaseConnectionIterator DatabaseConnectionIterator}, then uses it to loop over each
 * affected database and call the <code>countOrphans()</code> method in EnsTestCase. In this particular situation,
 * if there are any orphans in any of the databases, the test fails.</p>
 * <pre>
 * public class OrphanTestCase extends EnsTestCase {
 *
 *   public OrphanTestCase() {
 *     databaseRegexp = "^homo_sapiens_core_\\d.*";
 *     addToGroup("group2");
 *   }
 *
 *  TestResult run() {
 *
 *     boolean result = true;
 *
 *     DatabaseConnectionIterator it = getDatabaseConnectionIterator();
 *
 *     while (it.hasNext()) {
 *
 *       Connection con = (Connection)it.next();
 *       int orphans = super.countOrphans(con, "gene", "gene_id", "gene_stable_id", "gene_id", false);
 *       result &= (orphans == 0);
 *
 *     }
 *
 *     return new TestResult(getShortTestName(), result, "");
 *
 *   }
 *
 * } // OrphanTestCase
 * </pre>
 *
 * <p>Most test cases you write will take this form:</p>
 * <ol>
 * <li>Set up the database regexp and any groups that this test case is a member of; Note that all tests by
 * default are members of a group called "all". There is no need to explicitly add your new test to this group.</li>
 * <li>Implement the run() method; normally this will involve getting a DatabaseConnectionIterator,
 * calling one or more superclass methods on each database connection.</li>
 * <li>Create and return a TestResult object according to the results of your tests.</li>
 * </ol>
 *
 *
 */

public abstract class EnsTestCase {
  
  /** The TestRunner associated with this EnsTestCase */
  protected TestRunner testRunner;
  /** The regular expression to match the names of the databases that the test case will apply to. */
  protected String databaseRegexp = "";
  /** If set, this is applied to the database names before databaseRegexp. */
  protected String preFilterRegexp = "";
  /** A list of Strings representing the groups that this test is a member of.
   * All tests are members of the group "all", and also of a group with the same name as the test. */
  protected List groups;
  /** Regexp that, when combined with a species name, will match core databases */
  protected static final String CORE_DB_REGEXP = "_(core|est|estgene|vega)_\\d.*";

  /** Logger object to use */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // -------------------------------------------------------------------------
  /**
   * Creates a new instance of EnsTestCase
   */
  public EnsTestCase() {
    
    groups = new ArrayList();
    addToGroup("all");               // everything is in all, by default
    addToGroup(getShortTestName());  // each test is in a one-test group
    
  } // EnsTestCase
  
  // -------------------------------------------------------------------------
  /**
   * The principal run method. Subclasses of EnsTestCase should implement this
   * to provide test-specific behaviour.
   */
  abstract TestResult run();
  
  // -------------------------------------------------------------------------
  
  /**
   * Get the TestRunner that is controlling this EnsTestCase.
   * @return The parent TestRunner.
   */
  public TestRunner getTestRunner() {
    
    return testRunner;
    
  } // getTestRunner
  
  // -------------------------------------------------------------------------
  
  /**
   * Sets up this test. <B>Must</B> be called before the object is used.
   * @param tr The TestRunner to associate with this test. Usually just <CODE>this</CODE>
   * if being called from the TestRunner.
   */
  public void init(TestRunner tr) {
    
    this.testRunner = tr;
    
  } // init
  
  // -------------------------------------------------------------------------
  
  /**
   * Gets the full name of this test.
   * @return The full name of the test, e.g. org.ensembl.healthcheck.EnsTestCase
   */
  public String getTestName() {
    
    return this.getClass().getName();
    
  }
  
  // -------------------------------------------------------------------------
  
  /**
   * Get the short form of the test name, ie the name of the test class without the
   * package qualifier.
   *
   * @return The short test name, e.g. EnsTestCase
   */
  public String getShortTestName() {
    
    String longName = getTestName();
    
    return longName.substring(longName.lastIndexOf('.')+1);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the names of the groups which this test case is a member of.
   * @return The list of names as Strings.
   */
  public List getGroups() {
    
    return groups;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the groups that this test case is a member of, formatted for easy
   * printing.
   * @return The comma-separated list of group names.
   */
  public String getCommaSeparatedGroups() {
    
    StringBuffer gString = new StringBuffer();
    
    java.util.Iterator it = groups.iterator();
    while (it.hasNext()) {
      gString.append((String)it.next());
      if (it.hasNext()) {
        gString.append(",");
      }
    }
    return gString.toString();
  }
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method for assigning this test case to several groups at once.
   * @param s A list of Strings containing the group names.
   */
  public void setGroups(List s) {
    
    groups = s;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method for assigning this test case to several groups at once.
   * @param s Array of group names.
   */
  public void setGroups(String[] s) {
    for (int i = 0; i < s.length; i++) {
      groups.add(s[i]);
    }
  }
  
  // -------------------------------------------------------------------------
  /**
   * Add this test case to a new group.
   * If the test case is already a member of the group, a warning is printed and
   * it is not added again.
   * @param newGroupName The name of the new group.
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
   * Remove this test case from the specified group.
   * If the test case is not a member of the specified group, a warning is printed.
   * @param groupName The name of the group from which this test case is to be removed.
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
   * @param group The name of the group to check.
   * @return True if this test case is a member of the named group, false otherwise.
   */
  public boolean inGroup(String group) {
    
    return groups.contains(group);
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method for checking if this test case belongs to any of several groups.
   * @param checkGroups The list of group names to check.
   * @return True if this test case is in any of the groups, false if it is in none.
   */
  public boolean inGroups(List checkGroups) {
    
    boolean result = false;
    
    java.util.Iterator it = checkGroups.iterator();
    while (it.hasNext()) {
      if (inGroup((String)it.next())) {
        result = true;
      }
    }
    return result;
    
  } // inGroups
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the databases matching a particular pattern.
   * Uses pre-filter regexp if it is defined.
   * @return The list of database names matched.
   */
  public String[] getAffectedDatabases() {
    
    return testRunner.getListOfDatabaseNames(databaseRegexp);
    
  } // getAffectedDatabases
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method to return a DatabaseConnectionIterator from the
   * parent TestRunner class, with the current database regular expression.
   * @return A new DatabaseConnectionIterator.
   */
  public DatabaseConnectionIterator getDatabaseConnectionIterator() {
    
    return testRunner.getDatabaseConnectionIterator(databaseRegexp);
    
  }
  // -------------------------------------------------------------------------
  /**
   * Prints (to stdout) all the databases that match the current class' database regular expression.
   * Uses pre-filter regexp if it is defined.
   */
  public void printAffectedDatabases() {
    
    System.out.println("Databases matching " + databaseRegexp + ":");
    String[] databaseList = getAffectedDatabases();
    Utils.printArray(databaseList);
    for (int i = 0; i < databaseList.length; i++) {
      System.out.println("\t\t" + databaseList[i]);
    }
    
  } // printAffectedDatabases
  
  // -------------------------------------------------------------------------
  /**
   * Count the number of rows in a table.
   * @param con The database connection to use. Should have been opened already.
   * @param table The name of the table to analyse.
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
   * Count the rows in a particular table or query.
   * @param con A connection to the database. Should already be open.
   * @param sql The SQL to execute; should be of the form <code>SELECT COUNT(*) FROM </code> ...
   * @return The number of matching rows, or -1 if the query did not execute for some reason.
   */
  public int getRowCount(Connection con, String sql) {
    
    if (con == null) {
      logger.severe("getRowCount: Database connection is null");
    }
    int result = -1;
    
    try {
      Statement stmt = con.createStatement();
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
    
  } // getRowCount
  
  // -------------------------------------------------------------------------
  /**
   * Execute a SQL statement and return the value of one column of one row.
   * Only the FIRST row matched is returned.
   * @param con The Connection to use.
   * @param sql The SQL to check; should return ONE value.
   * @return The value returned by the SQL.
   */
  public String getRowColumnValue(Connection con, String sql) {
    
    String result = "";
    
    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      if (rs != null) {
        rs.first();
        result = rs.getString(1);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
    
  } // getRowColumnValue
  
  // -------------------------------------------------------------------------
  /**
   * Execute a SQL statement and return the values of one column of the result.
   * @param con The Connection to use.
   * @param sql The SQL to check; should return ONE column.
   * @return The value(s) making up the column, in the order that they were read.
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
    
    String[] dummy = {"", ""};
    return (String[])list.toArray(dummy);
    
  } // getRowColumnValue
  
  
  // -------------------------------------------------------------------------
  /**
   * Verify foreign-key relations.
   * @param con A connection to the database to be tested. Should already be open.
   * @param table1 With col1, specifies the first key to check.
   * @param col1 Column in table1 to check.
   * @param table2 With col2, specifies the second key to check.
   * @param col2 Column in table2 to check.
   * @param oneWayOnly If false, only a "left join" is performed on table1 and table2. If false, the
   * @return The number of "orphans"
   */
  public int countOrphans(Connection con, String table1, String col1, String table2, String col2, boolean oneWayOnly) {
    
    if (con == null) {
      logger.severe("countOrphans: Database connection is null");
    }
    
    int resultLeft, resultRight;
    
    String sql = "SELECT COUNT(*) FROM " + table1 +
    " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 +
    " WHERE " + table2 + "." + col2 + " iS NULL";
    
    resultLeft = getRowCount(con, sql);
    
    if (!oneWayOnly) {
      // and the other way ... (a right join?)
      sql = "SELECT COUNT(*) FROM " + table2 +
      " LEFT JOIN " + table1 + " ON " + table2 + "." + col2 + " = " + table1 + "." + col1 +
      " WHERE " + table1 + "." + col1 + " IS NULL";
      
      resultRight = getRowCount(con, sql);
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
   * @return True if all matched databases provide the same result, false otherwise.
   * @param sql The SQL query to execute.
   * @param regexp A regexp matching the database names to check.
   */
  public boolean checkSameSQLResult(String sql, String regexp) {
    
    ArrayList resultSetGroup = new ArrayList();
    ArrayList statements = new ArrayList();
    
    DatabaseConnectionIterator dcit = testRunner.getDatabaseConnectionIterator(regexp);
    
    while (dcit.hasNext()) {
      
      Connection con = (Connection)dcit.next();
      
      try {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs != null) {
          resultSetGroup.add(rs);
        }
        logger.fine("Added ResultSet for " + sql);
        //DBUtils.printResultSet(rs, 100);
        // note that the Statement can't be closed here as we use the ResultSet elsewhere
        // so store a reference to it for closing later
        statements.add(stmt);
        //con.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
    boolean same = DBUtils.compareResultSetGroup(resultSetGroup);
    
    Iterator it = statements.iterator();
    while (it.hasNext()) {
      try {
        ((Statement)it.next()).close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return same;
    
  } // checkSameSQLResult
  
  // -------------------------------------------------------------------------
  /**
   * Check that a particular SQL statement has the same result when executed
   * on more than one database.
   * The test case's build-in regexp is used to decide which database names to match.
   * @return True if all matched databases provide the same result, false otherwise.
   * @param sql The SQL query to execute.
   */
  public boolean checkSameSQLResult(String sql) {
    
    return checkSameSQLResult(sql, databaseRegexp);
    
  } // checkSameSQLResult
  
  // -------------------------------------------------------------------------
  /**
   * Over-ride the database regular expression set in the subclass' constructor.
   * @param re The new regular expression to use.
   */
  public void setDatabaseRegexp(String re) {
    
    databaseRegexp = re;
    
  } // setDatabaseRegexp
  
  // -------------------------------------------------------------------------
  /**
   * Get the regular expression that will be applied to database names before the built-in regular expression.
   * @return The value of preFilterRegexp
   */
  public String getPreFilterRegexp() {
    return preFilterRegexp;
  }
  
  /**
   * Set the regular expression that will be applied to database names before the built-in regular expression.
   * @param s The new value for preFilterRegexp.
   **/
  public void setPreFilterRegexp(String s) {
    preFilterRegexp = s;
  }
  
  // -------------------------------------------------------------------------
  /**
   * Check for the presence of a particular String in a table column.
   * @param con The database connection to use.
   * @param table The name of the table to examine.
   * @param column The name of the column to look in.
   * @param str The string to search for; can use database wildcards (%, _) Note that if you want to search for one of these special characters, it must be backslash-escaped.
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
   * @param con The database connection to use.
   * @param table The name of the table to examine.
   * @param column The name of the column to look in.
   * @param pattern The SQL pattern (can contain _,%) to look for.
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
   * @param con The database connection to use.
   * @param table The name of the table to examine.
   * @param column The name of the column to look in.
   * @param value The string to look for (not a pattern).
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
   * Check if there are any blank entires in a column that is not supposed to be null.
   * @param con The database connection to use.
   * @param table The table to use.
   * @param column The column to examine.
   * @return An list of the row indices of any blank entries. Will be zero-length if there are none.
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
        if (rsmd.isNullable(1) == rsmd.columnNoNulls) {
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
   * Check all columns of a table for blank entires in columns that are marked as being NOT NULL.
   * @param con The database connection to use.
   * @param table The table to use.
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
          if (rsmd.isNullable(i) == rsmd.columnNoNulls) {
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
   * @param con The database connection to check.
   * @param table The table to check for.
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
   * @param con The database connection involved.
   * @param message The message to print.
   */
  protected void warn(Connection con, String message) {
    
    logger.warning( "Problem in " + DBUtils.getShortDatabaseName( con ));
    logger.warning( message );
    
  } //warn
  
  // -------------------------------------------------------------------------
  /** 
   * Get a list of the databases which represent species. Filter out any which don't seem to represent species.
   * @return A list of the species; each species will occur only once, and be of the form homo_sapiens (no trailing _).
   */  
  public String[] getListOfSpecies() {
    
    ArrayList list = new ArrayList();
    
    DatabaseConnectionIterator dbci = testRunner.getDatabaseConnectionIterator(".*");
    while (dbci.hasNext()) {
      
      String dbName = DBUtils.getShortDatabaseName((Connection)dbci.next());
      
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
    
    String[] dummy = { "" };

    return (String[])list.toArray(dummy);
    
  }
  
  // -------------------------------------------------------------------------
  
} // EnsTestCase
