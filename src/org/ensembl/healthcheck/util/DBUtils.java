/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUsql
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

import org.ensembl.healthcheck.util.*;

/**
 * Various database utilities.
 */

public class DBUtils {
  
  private static final boolean USE_CONNECTION_POOLING = false;
  
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
        con = DriverManager.getConnection(databaseURL, user, password);
        
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
   * @param resultSetGroup The list of ResultSets to compare
   * @return The number of differences.
   */
  public static boolean compareResultSetGroup(List resultSetGroup) {
    
    boolean same = true;
    
    // avoid comparing the same two ResultSets more than once
    // i.e. only need the upper-right triangle of the comparison matrix
    int size = resultSetGroup.size();
    for (int i = 0; i < size; i++) {
      for (int j = i+1; j < size; j++) {
        ResultSet rsi = (ResultSet)resultSetGroup.get(i);
        ResultSet rsj = (ResultSet)resultSetGroup.get(j);
        same &= compareResultSets(rsi, rsj);
      }
    }
    
    return same;
    
  } // compareResultSetGroup
  
  // -------------------------------------------------------------------------
  /**
   * Compare two ResultSets.
   * @param rs1 The first ResultSet to compare.
   * @param rs2 The second ResultSet to compare.
   * @return True if all the following are true: <ol>
   * <li> rs1 and rs2 have the same number of columns</li>
   * <li> The name and type of each column in rs1 is equivalent to the corresponding column in rs2.</li>
   * <li> All the rows in rs1 have the same type and value as the corresponding rows in rs2.</li>
   *</ol>
   */
  public static boolean compareResultSets(ResultSet rs1, ResultSet rs2) {
    
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
        logger.warning("Column counts differ -  " + name1 + ": " + rsmd1.getColumnCount() + " " + name2 + ": " + rsmd2.getColumnCount());
        return false;  // Deliberate early return for performance reasons
      }
      for (int i=1; i <= rsmd1.getColumnCount(); i++) {                 // note columns indexed from 1
        if (!((rsmd1.getColumnName(i)).equals(rsmd2.getColumnName(i)))) {
          logger.warning("Column names differ for column " + i + " - " + name1 + ": " + rsmd1.getColumnName(i) + " " + name2 + ": " + rsmd2.getColumnName(i));
          return false;  // Deliberate early return for performance reasons
        }
        if (rsmd1.getColumnType(i) != rsmd2.getColumnType(i)) {
          logger.warning("Column types differ for column " + i + " - " + name1 + ": " + rsmd1.getColumnType(i) + " " + name2 + ": " + rsmd2.getColumnType(i));
          return false;  // Deliberate early return for performance reasons
        }
      } // for column
      
      // make sure both cursors are at the start of the ResultSet (default is before the start)
      rs1.first();
      rs2.first();
      // if quick checks didn't cause return, try comparing row-wise
      while (rs1.next() && rs2.next()) {
        
        for (int j=1; j <= rsmd1.getColumnCount(); j++) {              // note columns indexed from 1
          if (compareColumns(rs1, rs2, j) == false) {
            logger.info(name1 + " and " + name2 + " differ at column " + j +
            " (" + rsmd1.getColumnName(j) + ")" +
            " Values: " + Utils.truncate(rs1.getString(j), 25, true) + ", " +
            Utils.truncate(rs2.getString(j), 25, true));
            return false;  // Deliberate early return for performance reasons
          }
        }
        
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
  
  // -------------------------------------------------------------------------
  
} // DBUtils