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

import java.sql.*;
import java.util.*;
import org.ensembl.healthcheck.util.*;

/**
 * <p>Title: BlankEnumDBNameExternalDBTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 21, 2003, 8:58 AM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */

public class BlankNonNullExternalDBTestCase extends EnsTestCase {
  
  /** Creates a new instance of BlankEnumDBNameExternalDBTestCase */
  public BlankNonNullExternalDBTestCase() {
    databaseRegexp = ".*_core_\\d.*";
  }
  
  /**
   * Check for any columns in the external_db table that are marked as NON NULL but contain null or blank values.
   */
  TestResult run() {
    DatabaseConnectionIterator it = testRunner.getDatabaseConnectionIterator(getAffectedDatabases(databaseRegexp));
    
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
