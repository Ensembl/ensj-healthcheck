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

package org.ensembl.healthcheck.util;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import java.util.logging.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Various database utilities.
 */

public class DBUtils {
  
  private static final boolean USE_CONNECTION_POOLING = true;
  
  private static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // -------------------------------------------------------------------------
  /**
   * Open a connection to the database.
   * @param driverClassName The class name of the driver to load.
   * @param databaseURL The URL of the database to connect to.
   * @param user The username to connect with.
   * @param password Password for user.
   * @return A connection to the database, or null.
   */
  public static Connection openConnection(String driverClassName, String databaseURL, String user, String password) {
    
    Connection con = null;
    
    if (USE_CONNECTION_POOLING) {
      
      con = ConnectionPool.getConnection(driverClassName, databaseURL, user, password);
      
    } else {
      
      try {
        
        Class.forName(driverClassName);
        Properties props = new Properties();
        props.put("user",     user);
        props.put("password", password);
        props.put("maxRows",  "" + 100);
        con = DriverManager.getConnection(databaseURL, props);
        
      } catch (Exception e) {
        
        e.printStackTrace();
        System.exit(1);
        
      }
      
    } // if use pooling
    
    return con;
    
  } // openConnection
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the database names for a particular connection.
   * @param con The connection to query.
   * @return An array of Strings containing the database names.
   */
  
  public static String[] listDatabases(Connection con) {
    
    if (con == null) {
      logger.severe("Database connection is null");
    }
    
    ArrayList dbNames = new ArrayList();
    
    try {
      
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SHOW DATABASES");
      
      while (rs.next()) {
        dbNames.add(rs.getString(1));
      }
      
      rs.close();
      
      stmt.close();
      
    } catch (Exception e) {
      
      e.printStackTrace();
      System.exit(1);
      
    }
    
    String[] ret = new String[dbNames.size()];
    
    return (String[])dbNames.toArray(ret);
    
  } // listDatabases
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the database names that match a certain pattern for a particular connection.
   * @param conn The connection to query.
   * @param regex A regular expression to match.
   * @param preFilterRegexp If non-null and non-blank, this is applied to the list <em>before</em> regex.
   * @return An array of Strings containing the database names.
   */
  public static String[] listDatabases(Connection conn, String regex, String preFilterRegexp) {
    
    Pattern pattern, preFilterPattern = null;
    Matcher matcher, preFilterMatcher = null;
    boolean usePreFilter = false;
    
    if (conn == null) {
      logger.severe("Database connection is null");
    }
    
    if (preFilterRegexp != null && preFilterRegexp.length() != 0) {
      preFilterPattern = Pattern.compile(preFilterRegexp);
      usePreFilter = true;
    }
    
    ArrayList dbMatches = new ArrayList();
    
    pattern = Pattern.compile(regex);
    
    String[] allDBNames = listDatabases(conn);
    
    for (int i = 0; i < allDBNames.length; i++) {
      
      matcher = pattern.matcher(allDBNames[i]);
      
      if (usePreFilter) {
        preFilterMatcher = preFilterPattern.matcher(allDBNames[i]);
        if (preFilterMatcher.matches() && matcher.matches()) {
          dbMatches.add(allDBNames[i]);
        }
      } else { // not using preFilter
        if (matcher.matches()) {
          dbMatches.add(allDBNames[i]);
        }
      }
      
    } // for i
    
    
    String[] ret = new String[dbMatches.size()];
    
    return (String[])dbMatches.toArray(ret);
    
  } // listDatabases
  
  // -------------------------------------------------------------------------
  /**
   * Compare a list of ResultSets to see if there are any differences.
   * Note that if the ResultSets are large and/or there are many of them, this may take a long time!
   * @return The number of differences.
   * @param testCase The test case that is calling the comparison. Used for ReportManager.
   * @param resultSetGroup The list of ResultSets to compare
   */
  public static boolean compareResultSetGroup(List resultSetGroup, EnsTestCase testCase) {
    
    boolean same = true;
    
    // avoid comparing the same two ResultSets more than once
    // i.e. only need the upper-right triangle of the comparison matrix
    int size = resultSetGroup.size();
    for (int i = 0; i < size; i++) {
      for (int j = i+1; j < size; j++) {
        ResultSet rsi = (ResultSet)resultSetGroup.get(i);
        ResultSet rsj = (ResultSet)resultSetGroup.get(j);
        same &= compareResultSets(rsi, rsj, testCase, "");
      }
    }
    
    return same;
    
  } // compareResultSetGroup
  
  // -------------------------------------------------------------------------
  /**
   * Compare two ResultSets.
   * @return True if all the following are true: <ol>
   * <li> rs1 and rs2 have the same number of columns</li>
   * <li> The name and type of each column in rs1 is equivalent to the corresponding column in rs2.</li>
   * <li> All the rows in rs1 have the same type and value as the corresponding rows in rs2.</li>
   * </ol>
   * @param testCase The test case calling the comparison; used in ReportManager.
   * @param text Additional text to put in any error reports.
   * @param rs1 The first ResultSet to compare.
   * @param rs2 The second ResultSet to compare.
   */
  public static boolean compareResultSets(ResultSet rs1, ResultSet rs2, EnsTestCase testCase, String text) {
    
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
      if (rsmd1.getColumnCount() != rsmd2.getColumnCount()) {
        ReportManager.problem(testCase, name1, "Column counts differ -  " + name1 + ": " + rsmd1.getColumnCount() + " " + name2 + ": " + rsmd2.getColumnCount());
        return false;  // Deliberate early return for performance reasons
      }
      for (int i=1; i <= rsmd1.getColumnCount(); i++) {                 // note columns indexed from 1
        if (!((rsmd1.getColumnName(i)).equals(rsmd2.getColumnName(i)))) {
          ReportManager.problem(testCase, name1, "Column names differ for column " + i + " - " + name1 + ": " + rsmd1.getColumnName(i) + " " + name2 + ": " + rsmd2.getColumnName(i));
          return false;  // Deliberate early return for performance reasons
        }
        if (rsmd1.getColumnType(i) != rsmd2.getColumnType(i)) {
          ReportManager.problem(testCase, name1, "Column types differ for column " + i + " - " + name1 + ": " + rsmd1.getColumnType(i) + " " + name2 + ": " + rsmd2.getColumnType(i));
          return false;  // Deliberate early return for performance reasons
        }
      } // for column
      
      // make sure both cursors are at the start of the ResultSet (default is before the start)
      rs1.first();
      rs2.first();
      // if quick checks didn't cause return, try comparing row-wise
      int row = 0;
      while (rs1.next() && rs2.next()) {
        
        for (int j=1; j <= rsmd1.getColumnCount(); j++) {              // note columns indexed from 1
          if (compareColumns(rs1, rs2, j) == false) {
            String str = name1 + " and " + name2 + text + " differ at row " + row + " column " + j + " (" + rsmd1.getColumnName(j) + ")" + " Values: " + Utils.truncate(rs1.getString(j), 25, true) + ", " + Utils.truncate(rs2.getString(j), 25, true);
            ReportManager.problem(testCase, name1, str);
            return false;  // Deliberate early return for performance reasons
          }
        }
        row++;
        
      }
      
      // if both ResultSets are the same, then we should be at the end of both, i.e. .next() should return false
      if (rs1.next() == true) {
        ReportManager.problem(testCase, name1, text + " in " + name1 + " seems to have additional rows that are not in " + name2);
        return false;
      } else if (rs2.next() == true) {
        ReportManager.problem(testCase, name2, text + " in " + name2 + " seems to have additional rows that are not in " + name1);
        return false;
      }
      
    } catch (SQLException se) {
      se.printStackTrace();
    }
    
    return true;
    
  } // compareResultSets
  
  // -------------------------------------------------------------------------
  /**
   * Compare a particular column in two ResultSets.
   * @param rs1 The first ResultSet to compare.
   * @param rs2 The second ResultSet to compare.
   * @param i The index of the column to compare.
   * @return True if the type and value of the columns match.
   */
  public static boolean compareColumns(ResultSet rs1, ResultSet rs2, int i) {
    
    try {
      
      ResultSetMetaData rsmd = rs1.getMetaData();
      
      // Note deliberate early returns for performance reasons
      switch (rsmd.getColumnType(i)) {
        
        case Types.INTEGER:
          return rs1.getInt(i) == rs2.getInt(i);
          
        case Types.SMALLINT:
          return rs1.getInt(i) == rs2.getInt(i);
          
        case Types.TINYINT:
          return rs1.getInt(i) == rs2.getInt(i);
          
        case Types.VARCHAR:
          return rs1.getString(i).equals(rs2.getString(i));
          
        case Types.FLOAT:
          return rs1.getFloat(i) == rs2.getFloat(i);
          
        case Types.DOUBLE:
          return rs1.getDouble(i) == rs2.getDouble(i);
          
        case Types.TIMESTAMP:
          return rs1.getTimestamp(i).equals(rs2.getTimestamp(i));
          
        default:  // treat everything else as a String (should deal with ENUM and TEXT)
          if (rs1.getString(i) == null || rs2.getString(i) == null) {
            return true; // ????
          } else {
            return rs1.getString(i).equals(rs2.getString(i));
          }
          
      } // switch
      
    } catch (SQLException se) {
      se.printStackTrace();
    }
    
    return true;
    
  } // compareColumns
  
  // -------------------------------------------------------------------------
  /**
   * Print a ResultSet to standard out. Optionally limit the number of rows.
   * @param maxRows The maximum number of rows to print. -1 to print all rows.
   * @param rs The ResultSet to print.
   */
  public static void printResultSet(ResultSet rs, int maxRows) {
    
    int row = 0;
    
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      
      while (rs.next()) {
        for (int i=1; i <= rsmd.getColumnCount(); i++) {
          System.out.print(rs.getString(i) + "\t");
        }
        System.out.println("");
        if (maxRows != -1 && ++row >= maxRows) {
          break;
        }
      }
    } catch (SQLException se) {
      se.printStackTrace();
    }
    
  } // printResultSet
  
  // -------------------------------------------------------------------------
  /** Gets the database name, without the jdbc:// prefix.
   * @param con The Connection to query.
   * @return The name of the database (everything after the last / in the JDBC URL).
   */
  public static String getShortDatabaseName(Connection con) {
    
    String url = null;
    
    try {
      url = con.getMetaData().getURL();
    } catch (SQLException se) {
      se.printStackTrace();
    }
    String name = url.substring(url.lastIndexOf('/') + 1);
    
    return name;
    
  } // getShortDatabaseName
  
  // -------------------------------------------------------------------------
  /**
   * Convert properties used by Healthcheck into properties suitable for ensj.
   * ensj properties host, port, user, password are converted.
   * Note ensj property database is <em>not</em> set.
   * @return A Properties object containing host, port, user and password NOT database.
   * @param testRunnerProps A set of properties in the format used by HealthCheck, e.g. databaseURL etc.
   */
  public static Properties convertHealthcheckToEnsjProperties(Properties testRunnerProps) {
    
    Properties props = new Properties();
    
    // user & password are straightforward
    props.put("user"     , testRunnerProps.get("user"));
    props.put("password" , testRunnerProps.get("password"));
    
    // get host and port from URL
    // java.net.URL doesn't support JDBC URLs(!) so we have to hack things a bit
    String dbUrl = (String)testRunnerProps.get("databaseURL");
    java.net.URL url = null;
    try {
      url = new java.net.URL("http" + dbUrl.substring(10)); // strip off jdbc:mysql: and pretend it's http
    } catch (java.net.MalformedURLException e) {
      e.printStackTrace();
    }
    if (url != null) {
      String host = url.getHost();
      String port = "" + url.getPort();
      props.put("host"      , host);
      props.put("port"      , port);
    } else {
      logger.severe("Unable to get host/port from url");
    }
    
    return props;
    
  } // convertHealthcheckToEnsjProperties
  
  // -------------------------------------------------------------------------
  /**
   * Convert properties used by Healthcheck into properties suitable for ensj.
   * ensj properties host, port, user, password are converted.
   * Note ensj property database is <em>not</em> set.
   * Input properties are obtained from System.properties.
   * @return A Properties object containing host, port, user and password NOT database.
   */
  public static Properties convertHealthcheckToEnsjProperties() {
    
    return convertHealthcheckToEnsjProperties(System.getProperties());
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Generate a name for a temporary database. Should be fairly unique;
   * name is _temp_{user}_{time} where user is current user and time is current
   * time in ms.
   * @return The temporary name. Will not have any spaces.
   */
  public static String generateTempDatabaseName() {
    
    StringBuffer buf = new StringBuffer("_temp_");
    buf.append(System.getProperty("user.name"));
    buf.append("_" + System.currentTimeMillis());
    String str = buf.toString();
    str = str.replace(' ', '_');  // filter any spaces
    
    logger.fine("Generated temporary database name: " + str);
    
    return str;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of all the table names.
   * @param con The database connection to use.
   * @return A list of Strings representing the names of the tables, obtained
   * from the SHOW TABLES command.
   */
  public static List getTableNames(Connection con) {
    
    List result = new ArrayList();
    
    if (con == null) {
      logger.severe("getTableNames(): Database connection is null");
    }
    
    try {
      
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SHOW TABLES");
      
      while (rs.next()) {
        result.add(rs.getString(1));
      }
      
      rs.close();
      stmt.close();
      
    } catch (SQLException se) {
      logger.severe(se.getMessage());
    }
    
    return result;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of the table names that match a particular SQL pattern.
   * @param con The database connection to use.
   * @param pattern The SQL pattern to match the table names against.
   * @return A list of Strings representing the names of the tables.
   */
  public static List getTableNames(Connection con, String pattern) {
    
    List result = new ArrayList();
    
    if (con == null) {
      logger.severe("getTableNames(): Database connection is null");
    }
    
    try {
      
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + pattern + "'");
      
      while (rs.next()) {
        result.add(rs.getString(1));
      }
      
      rs.close();
      stmt.close();
      
    } catch (SQLException se) {
      logger.severe(se.getMessage());
    }
    
    return result;
    
  }
  
  // -------------------------------------------------------------------------
  /** 
   * List the columns in a particular table.
   * @param table The name of the table to list.
   * @param con The connection to use.
   * @return A List of Strings representing the column names.
   */
  public static List getColumnsInTable(Connection con, String table) {
    
    List result = new ArrayList();
    
     try {
      
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("DESCRIBE " + table);
      
      while (rs.next()) {
        result.add(rs.getString(1));
      }
      
      rs.close();
      stmt.close();
      
    } catch (SQLException se) {
      logger.severe(se.getMessage());
    }
    
    return result;
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Execute SQL and writes results to ReportManager.info().
   * @param testCase testCase which created the sql statement
   * @param con connection to execute sql on.
   * @param sql sql statement to execute.
   */
  public static void printRows(EnsTestCase testCase, Connection con, String sql) {
    // TODO Auto-generated method stub
    try {
      ResultSet rs = con.createStatement().executeQuery( sql );
      if ( rs.next() ) {
        int nCols = rs.getMetaData().getColumnCount();
        StringBuffer line = new StringBuffer();
        do {
          line.delete(0, line.length());
          for(int i=1; i<=nCols; ++i) {
            line.append( rs.getString(i) );
            if ( i <nCols ) line.append("\t");
            
          }
          ReportManager.info( testCase, con, line.toString() );
        } while ( rs.next() );
      }
      
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
    
  // -------------------------------------------------------------------------

} // DBUtils
