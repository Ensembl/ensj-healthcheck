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

/**
 * <p>Title: CountSQLTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 12, 2003, 4:08 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */


package org.ensembl.healthcheck;

import java.sql.*;
import org.ensembl.healthcheck.util.*;

public class CountSQLTestCase extends EnsTestCase {
  
  /** Creates a new instance of CountSQLTestCase */
  public CountSQLTestCase() {
    databaseRegexp = "^homo_sapiens_core_12.*";
  }
  
  TestResult run() {
        
    DatabaseConnectionIterator it = testRunner.getDatabaseConnectionIterator(getAffectedDatabases(databaseRegexp, preFilterRegexp));
    
    boolean result = true;
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rowCount = super.countRowsInTable(con, "gene");
      result &= (rowCount > 0);
      System.out.println("gene table in " + it.getCurrentDatabaseName() + " has " + rowCount + " rows.");
      
    }
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CountSQLTestCase
