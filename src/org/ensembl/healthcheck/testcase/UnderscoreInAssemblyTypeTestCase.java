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
 * EnsEMBL Healthcheck test case that looks for _ characters in the type
 * column of the assembly table.
 */

public class UnderscoreInAssemblyTypeTestCase extends EnsTestCase {
  
  /** Creates a new instance of UnderscoreInAssemblyTypeTestCase */
  public UnderscoreInAssemblyTypeTestCase() {
    addToGroup("post_genebuild");
    setDescription("Checks for the presence of _ characters in assembly.type");
  }
  
  /** 
   * Check the assembly.type column in each database to look for _.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      // note on escaping: _ is a SQL wildcard, so we need to escape it with \_ to actually search for an _
      // the \ itself needs to be escaped with another \ to remove its special meaning in a Java String
      int underscores = findStringInColumn(con, "assembly", "type", "%\\_%");
      logger.fine("" + underscores);
      if (underscores > 0) {
        result = false;       // don't want any underscores in this case
        ReportManager.problem(this, con, "Underscore character found in assembly.type");
      } else {
       ReportManager.info(this, con, "No underscores found in assembly.type"); 
      }
    }
    
    return new TestResult(getShortTestName(), result);
  }
  
  
} // UnderscoreInAssemblyTypeTestCase
