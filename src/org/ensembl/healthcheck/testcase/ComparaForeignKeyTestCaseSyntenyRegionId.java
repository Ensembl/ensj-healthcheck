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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class ComparaForeignKeyTestCaseSyntenyRegionId extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public ComparaForeignKeyTestCaseSyntenyRegionId() {
      databaseRegexp = "^ensembl_compara_.*";
      addToGroup("compara_db_constraints");
      setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
  }
  
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    int orphans = 0;
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      // 1 test to check synteny_region_id used as foreign key
      
      if( getRowCount( con, "select count(*) from synteny_region" ) > 0 ) {
        orphans = countOrphans(con, "dnafrag_region", "synteny_region_id", "synteny_region", "synteny_region_id", false );
        if( orphans == 0 ) {
	    ReportManager.correct(this, con, "dnafrag_region <-> synteny_region relationships PASSED");
        } else if( orphans > 0 ) {
	    ReportManager.problem(this, con, "dnafrag_region has unlinked entries in synteny_region FAILED");
        } else {
	    ReportManager.problem(this, con, "dnafrag_region <-> synteny_region TEST NOT COMPLETED, look at the StackTrace if any");
        }
      } else {
	  ReportManager.correct(this, con, "NO ENTRIES in synteny_region table, so nothing to test IGNORED");
      }

      result &= (orphans == 0);
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
} // OrphanTestCase
