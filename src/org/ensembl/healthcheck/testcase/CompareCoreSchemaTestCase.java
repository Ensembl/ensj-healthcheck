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
 * Test case to compare table structures between several schemas.
 *
 * Has several ways of deciding which schema to use as the "master" to compare
 * all the others against:<p>
 * <ol>
 * <li>If the property schema.file in database.properties exists, the table.sql file it points to</li>
 * <li>If the schema.file property is not present, the schema named by the property schema.master is used</li>
 * <li>If neither of the above properties are present, the (arbitrary) first schema is used as the master.</li>
 * </ol>
 */
public class CompareCoreSchemaTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CompareCoreSchemaTestCase.
   */
  public CompareCoreSchemaTestCase() {
    addToGroup("db_constraints");
    setDatabaseRegexp(".*_(core|estgene)_\\d.*");
  }
  
  /**
   * Compare each database with the master.
   */
  public TestResult run() {
    
    boolean result = true;
    
    Connection masterCon = null;
    Statement masterStmt = null;
    
    String definitionFile = null;
    String masterSchema = null;
    
    definitionFile = System.getProperty("schema.file");
    if (definitionFile == null) {
      logger.warning("CompareCoreSchemaTestCase: No schema definition file found! Set schema.file property in database.properties if you want to use a table.sql file or similar.");
      
      masterSchema = System.getProperty("master.schema");
      if (masterSchema != null) {
        logger.info("Will use " + masterSchema + " as specified master schema for comparisons.");
      } else {
        logger.warning("CompareCoreSchemaTestCase: No master schema defined file found! Set master.schema property in database.properties if you want to use a master schema.");
      }
    } else {
      logger.fine("Will use schema definition from " + definitionFile);
    }
    
    try {
      
      DatabaseConnectionIterator it = getDatabaseConnectionIterator();
      
      if (definitionFile != null) {        // use a schema definition file to generate a temporary database
        
        logger.info("About to import " + definitionFile);
        masterCon = importSchema(definitionFile);
        logger.info("Got connection to " + DBUtils.getShortDatabaseName(masterCon));
        
      } else if (masterSchema != null) {  // use the defined schema name as the master
                
        // get connection to master schema
        masterCon = getSchemaConnection(masterSchema);
        logger.fine("Opened connection to master schema in " + DBUtils.getShortDatabaseName(masterCon));
        
      } else {                            // just use the first one to compare with all the others
        
        if (it.hasNext()) {
          masterCon = (Connection)it.next();
          logger.info("Using " + DBUtils.getShortDatabaseName(masterCon) + " as 'master' for comparisions.");
        }
        
      }
      
      masterStmt = masterCon.createStatement();
      
      while (it.hasNext()) {
        
        Connection check_con = (Connection)it.next();
        logger.info("Comparing " + DBUtils.getShortDatabaseName(masterCon) + " with " + DBUtils.getShortDatabaseName(check_con));
        
        Statement dbStmt = check_con.createStatement();
        
        // check each table in turn
        List tableNames = getTableNames(masterCon);
        Iterator tableIterator = tableNames.iterator();
        while (tableIterator.hasNext()) {
          
          String table = (String)tableIterator.next();
          
          if (!checkTableExists(check_con, table)) {
            ReportManager.problem(this, check_con, "Table " + table + " exists in master schema but not in " + DBUtils.getShortDatabaseName(check_con));
            continue;
          }
          
          String sql = "DESCRIBE " + table;
          ResultSet masterRS = masterStmt.executeQuery(sql);
          ResultSet dbRS = dbStmt.executeQuery(sql);
          
          result &= DBUtils.compareResultSets(masterRS, dbRS, this, " [" + table + "]");
          
          masterRS.close();
          dbRS.close();
          
        } // while table
        
        dbStmt.close();
        
      } // while database
      
      masterStmt.close();
      
    } catch (SQLException se) {
      
      logger.severe(se.getMessage());
      
    } finally {  // avoid leaving temporary DBs lying around if something bad happens
      
      if (definitionFile == null && masterCon != null) {
        // double-check to make sure the DB we're going to remove is a temp one
        String dbName = DBUtils.getShortDatabaseName(masterCon);
        if (dbName.indexOf("_temp_") > -1) {
          removeDatabase(masterCon);
          logger.info("Removed " + DBUtils.getShortDatabaseName(masterCon));
        }
      }
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // GlennsDatabaseSchemaImportTestCase
