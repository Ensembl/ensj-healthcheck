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

package org.ensembl.healthcheck;

import java.sql.*;

import org.ensembl.healthcheck.util.*;

/**
 * Utility to aid in debugging database regular expressions by listing which databases match 
 * a regular expression given on the command line.
 */

public class DatabaseNameMatcher {
  
  private String databaseRegexp = "";
  
  // -------------------------------------------------------------------------
  /**
   * Command-line entry point.
   * @param args Arguments.
   */
  public static void main(String[] args) {
    
    DatabaseNameMatcher dnm = new DatabaseNameMatcher();
    
    dnm.parseCommandLine(args);
    
    dnm.readPropertiesFile();
    
    dnm.showMatches();
    
  } // main
  
  // -------------------------------------------------------------------------
  private void parseCommandLine(String[] args) {
    
    if (args.length == 0) {
      printUsage();
      System.exit(1);
    } else {
      databaseRegexp = args[0];
    }
    
  } // parseCommandLine
  
  // -------------------------------------------------------------------------
  
  private void printUsage() {
    
    System.out.println("\nUsage: DatabaseNameMatcher regexp\n");
    
  } // printUsage

  // -------------------------------------------------------------------------
  
  private void readPropertiesFile() {
    
    String propsFile = "database.properties";
    Utils.readPropertiesFileIntoSystem(propsFile);
    System.out.println("Read database properties from " + propsFile);
    //Enumeration e = dbProps.propertyNames();
    //String propName;
    //while (e.hasMoreElements()) {
    //  propName = (String)e.nextElement();
    //  System.out.println("\t" + propName + " = " + dbProps.getProperty(propName));
    //}
    
  } // readPropertiesFile
  
  // -------------------------------------------------------------------------
  /**
   * Show the databases that have names that match the regexp.
   */
  public void showMatches() {
    
    Connection con;
    
    String[] databaseNames = null;
    
    try {
      
      con = DBUtils.openConnection(System.getProperty("driver"), System.getProperty("databaseURL"), System.getProperty("user"), System.getProperty("password"));
      
      databaseNames = DBUtils.listDatabases(con, databaseRegexp);
      
      con.close();
      
    } catch (Exception e) {
      
      e.printStackTrace();
      System.exit(1);
      
    }
    
    if (databaseNames.length > 0) {
      System.out.println("\n" + databaseNames.length + " database names matched " + databaseRegexp + " :");
      for (int i = 0; i < databaseNames.length; i++) {
	System.out.println("\t" + databaseNames[i]);
      }
    } else {
      System.out.println("Warning: No database names matched");
    }
    
  } // showMatches
  
  // -------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------
  
  // -------------------------------------------------------------------------
  
} // listDatabaseMatching
