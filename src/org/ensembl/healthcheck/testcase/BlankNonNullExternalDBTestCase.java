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
 * EnsEMBL HealthCheck test case that looks for columns in the 
 * external_db table that are marked as NON NULL but contain null or blank values.
 */

public class BlankNonNullExternalDBTestCase extends EnsTestCase {
  
  /** 
   * Create a BlankNonNullExternalDBTestCase that matches a particular set of databases.
   */
  public BlankNonNullExternalDBTestCase() {
  }
  
  /**
   * Check for any columns in the external_db table that are marked as NON NULL but contain null or blank values.
   */
  public TestResult run() {
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    boolean result = true;
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int blanks = checkBlankNonNull(con, "external_db");
      if (blanks > 0) {
        result = false;
      }
    }
    
    return new TestResult(getShortTestName(), result, "");
    
  }
  
} // BlankNonNullExternalDBTestCase
