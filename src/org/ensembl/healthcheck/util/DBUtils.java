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

/**
 * <p>Title: DBUtils.java</p>
 * <p>Description: Various database utilities.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class DBUtils {

  public DBUtils() {
  }

  /**
    * Open a connection to the database.
    * @param driverClassName The class name of the driver to load.
    * @param databaseURL The URL of the database to connect to.
    * @param user The username to connect with.
    * @param password Password for user.
    * @return A connection to the database, or null.
    */
   public static Connection openConnection(String driverClassName, String databaseURL, String user, String password) {

     Connection conn = null;

     try {

       Class.forName(driverClassName);
       conn = DriverManager.getConnection(databaseURL, user, password);

     } catch (Exception e) {

       e.printStackTrace();
       System.exit(1);

     }

     return conn;

   } // openConnection

   /**
    * Get a list of the database names for a particular connection.
    * @param conn The connection to query.
    * @return An array of Strings containing the database names.
    */

   public static String[] listDatabases(Connection conn) {

     ArrayList dbNames = new ArrayList();

     try {

       Statement stmt = conn.createStatement();
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
    * @return An array of Strings containing the database names.
    */
   public static String[] listDatabases(Connection conn, String regex) {

     ArrayList dbMatches = new ArrayList();

     Pattern pattern = Pattern.compile(regex);

     Matcher matcher;

     String[] allDBNames = listDatabases(conn);

     for (int i = 0; i < allDBNames.length; i++) {

       matcher = pattern.matcher(allDBNames[i]);
       if (matcher.matches()) {
         dbMatches.add(allDBNames[i]);
       }
     }

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
   public static boolean compareResultSetGroup(ArrayList resultSetGroup) {
     
     boolean same = true;;
     
     Iterator it1 = resultSetGroup.iterator();
     while (it1.hasNext()) {
       ResultSet rs1 = (ResultSet)it1.next();
       // compare this ResultSet with all the others ...
       Iterator it2 = resultSetGroup.iterator();
       while (it2.hasNext()) {
	ResultSet rs2 = (ResultSet)it2.next();
	same &= compareResultSets(rs1, rs2);
       } // it2
     } // it1
     
     return same;
     
   } // compareResultSetGroup

   // -------------------------------------------------------------------------
   
   public static boolean compareResultSets(ResultSet rs1, ResultSet rs2) {
     
     boolean same = true;
     
     if (rs1.equals(rs2)) {
       return true;
     } else {
       
       try {
	 while (rs1.next()) {
	   
	   // TBC
	   
	 }
       } catch (SQLException se) {
	 se.printStackTrace();
       }
       return same;
       
     }
     
   } // compareResultSets
   
   // -------------------------------------------------------------------------
} // DBUtils