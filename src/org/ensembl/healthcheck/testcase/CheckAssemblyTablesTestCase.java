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
 * Check that the assembly table is present in all necessary databases.
 */
public class CheckAssemblyTablesTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckAssemblyTables
   */
  public CheckAssemblyTablesTestCase() {
    addToGroup("pre_release");
    setDescription("Check that the assembly table  contains the same information for all databases with the same species.");
  }
  
  /**
   * Make sure that the assembly table has the same number of rows.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    String[] species = getListOfSpecies();
    
    for (int i = 0; i < species.length; i++) {
      
      String speciesRegexp = species[i] + CORE_DB_REGEXP_POSTFIX;
      
      boolean allMatch = checkSameSQLResult("SELECT COUNT(*) FROM assembly", speciesRegexp);
      if (!allMatch) {
        result = false;
        ReportManager.problem(this, species[i], "Differences in assembly table across species" );
      } else {
        ReportManager.correct(this, species[i],  "All assembly tables the same" );
      }
      
    } // foreach species
    
    
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckAssemblyTables
