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
 * <p>Title: OrphanTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 13, 2003, 4:25 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version
 */

package org.ensembl.healthcheck;

import java.sql.*;

import org.ensembl.healthcheck.util.*;

public class OrphanTestCase extends EnsTestCase {
  
  public OrphanTestCase() {
    databaseRegexp = "^homo_sapiens_core_12.*";
    addToGroup("group2");
  }
  
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = testRunner.getDatabaseConnectionIterator(getAffectedDatabases(databaseRegexp, preFilterRegexp));
        
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int orphans = super.countOrphans(con, "gene", "gene_id", "gene_stable_id", "gene_id", false);
      result &= (orphans == 0);
      
    }
    
    return new TestResult(getShortTestName(), result, "");
    
  }
  
} // OrphanTestCase
