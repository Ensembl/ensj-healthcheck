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

package org.ensembl.healthcheck.testcase;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Temporary test case to check database importing.
 */
public class CompareCoreSchemaTestCase extends EnsTestCase {
  
  private static final String definitionFile = "/homes/glenn/work/ensj-healthcheck/table.sql";
  /**
   * Creates a new instance of GlennsDatabaseSchemaImportTestCase
   */
  public CompareCoreSchemaTestCase() {
    addToGroup("dev");
    setDatabaseRegexp(".*_(core|est|estgene)_.*");
  }
  
  /**
   * Create a temproary database schema from the table.sql file and compare each database
   * with it in turn.
   */
  public TestResult run() {
    
    boolean result = true;
    
    try {
      
      logger.info("About to import " + definitionFile);
      Connection tableSQLCon = importSchema(definitionFile);
      Statement tableSQLStmt = tableSQLCon.createStatement();
      logger.info("Got connection to " + DBUtils.getShortDatabaseName(tableSQLCon));
      
      DatabaseConnectionIterator it = getDatabaseConnectionIterator();
      
      while (it.hasNext()) {
        
        Connection check_con = (Connection)it.next();
        logger.info("Comparing " + DBUtils.getShortDatabaseName(tableSQLCon) + " with " + DBUtils.getShortDatabaseName(check_con));
        
        Statement dbStmt = check_con.createStatement();
        
        // check each table in turn
        List tableNames = getTableNames(tableSQLCon);
        Iterator tableIterator = tableNames.iterator();
        while (tableIterator.hasNext()) {
          
          String table = (String)tableIterator.next();
          
          // TODO - check table exists
          String sql = "DESCRIBE " + table;
          ResultSet tableSQLRS = tableSQLStmt.executeQuery(sql);
          ResultSet dbRS = dbStmt.executeQuery(sql);
          
          result &= DBUtils.compareResultSets(tableSQLRS, dbRS, this, " [" + table + "]");
          
          tableSQLRS.close();
          dbRS.close();
          
        } // while table
        
        dbStmt.close();
        
      } // while database
      
      tableSQLStmt.close();
      
      removeDatabase(tableSQLCon);
      // commented-out for testing purposes
      //logger.info("Removed " + DBUtils.getShortDatabaseName(con));
      
    } catch (SQLException se) {
      logger.severe(se.getMessage());
    }
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // GlennsDatabaseSchemaImportTestCase
