/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUsql
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase;

import java.sql.*;

import org.ensembl.healthcheck.util.*;

/**
 * Check the DNA tables in a range of databases.
 */
public class CheckDNATables extends EnsTestCase {
    
  /**
   * Creates a new instance of CheckDNATables
   */
  public CheckDNATables() {
    addToGroup("pre_mart");
  }
  
  /**
   * Make sure that the assembly table has the same number of rows
   */
  TestResult run() {
    
    boolean result = true;
    
    String[] species = getListOfSpecies();
    
    for (int i = 0; i < species.length; i++) {
      
      String speciesRegexp = species[i] + CORE_DB_REGEXP;
      logger.info("Checking dna tables in "+ speciesRegexp);
      boolean allMatch = checkSameSQLResult("SELECT COUNT(*) FROM dna", speciesRegexp);
      if (!allMatch) {
        result = false;
      }
      
    } // foreach species
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CheckDNATables
