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
import java.io.*;

import org.ensembl.healthcheck.util.*;

/**
 * Utility to aid in debugging database regular expressions by listing which databases match 
 * a regular expression given on the command line.
 */

public class DatabaseNameMatcher {
  
  private String databaseRegexp = "";
  private Properties dbProps;
  
  // -------------------------------------------------------------------------
  /**
   * Command-line entry point.
   * @param args Command-line arguments.
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
    
    String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "database.properties";
    dbProps = Utils.readPropertiesFile(propsFile);
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
   * Show matching database names.
   */
  public void showMatches() {
    
    Connection con;
    
    String[] databaseNames = null;
    
    try {
      
      con = DBUtils.openConnection(dbProps.getProperty("driver"), dbProps.getProperty("databaseURL"), dbProps.getProperty("user"), dbProps.getProperty("password"));
      
      databaseNames = DBUtils.listDatabases(con, databaseRegexp, "");
      
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
