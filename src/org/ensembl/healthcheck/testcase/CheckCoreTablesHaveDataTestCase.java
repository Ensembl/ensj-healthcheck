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

import org.ensembl.healthcheck.*;

import org.ensembl.healthcheck.util.*;

/**
 * Verify that important tables in core DBs actually have data.
 */
public class CheckCoreTablesHaveDataTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckCoreTablesHaveDataTestCase
   */
  public CheckCoreTablesHaveDataTestCase() {
    addToGroup("post_genebuild");
    setDescription("Verify that important tables in core DBs actually have data.");
  }
  
  /** 
   * Check that the number of rows in several tables is > 0.
   * @return Result.
   */
  public TestResult run() {
    
    String[] tables = { "chromosome", "assembly", "dna", "contig", "clone" };
    
    boolean result = true;
       
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      for (int i=0; i < tables.length; i++) {
        String table = tables[i];        
        logger.info("Checking " + DBUtils.getShortDatabaseName(con) + "." + table);
        if (countRowsInTable(con, table) == 0) {
          warn(con, table + " has no data!");
          ReportManager.problem(this, con, table + " has no data.");
        } else {
          ReportManager.correct(this, con, table + " is OK.");
        }
      } // foreach table
      
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckCoreTablesHaveDataTestCase
