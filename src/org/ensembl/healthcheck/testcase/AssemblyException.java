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

package org.ensembl.healthcheck.testcase;

import java.sql.*;

import org.ensembl.healthcheck.*;

import org.ensembl.healthcheck.util.*;

/**
 * Healthcheck for the assembly_exception table.
 */

public class AssemblyException extends EnsTestCase {
  
  /**
   * Check the assembly_exception table.
   */
  public AssemblyException() {
    addToGroup("post_genebuild");
    setDescription("Check assembly_exception table");
  }
  
  /**
   * Check the data in the assembly_exception table.
   * Note referential integrity checks are done in CoreForeignKeyTestCase.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      // check that seq_region_end > seq_region_start
      int rows = getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE seq_region_start > seq_region_end");
      if (rows > 0) {
      	result = false;
        ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where seq_region_start > seq_region_end");
      }
      
      // check that exc_seq_region_start > exc_seq_region_end
      rows = getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE exc_seq_region_start > exc_seq_region_end");
      if (rows > 0) {
      	result = false;
        ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where exc_seq_region_start > exc_seq_region_end");
      }
      
      // more tests to be added later
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
}