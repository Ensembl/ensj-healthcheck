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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class ComparaForeignKeyTestCaseMemberId extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public ComparaForeignKeyTestCaseMemberId() {
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
      // 1 test to check member_id used as foreign key
      
      if( getRowCount( con, "select count(*) from member" ) > 0 ) {
 
        orphans = countOrphans(con, "family_member", "member_id", "member", "member_id", false );
        if( orphans == 0 ) {
	    ReportManager.correct(this, con, "family_member <-> member relationships PASSED");
	} else if( orphans > 0 ) {
	    ReportManager.problem(this, con, "family_member has unlinked entries in member FAILED");
        } else {
	    ReportManager.problem(this, con, "family_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
        }

        orphans = countOrphans(con, "homology_member", "member_id", "member", "member_id", false );
        if( orphans == 0 ) {
	    ReportManager.correct(this, con, "homology_member <-> member relationships PASSED");
	} else if( orphans > 0 ) {
	    ReportManager.problem(this, con, "homology_member has unlinked entries in member FAILED");
        } else {
	    ReportManager.problem(this, con, "homology_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
        }

        orphans = countOrphans(con, "domain_member", "member_id", "member", "member_id", false );
        if( orphans == 0 ) {
	    ReportManager.correct(this, con, "domain_member <-> member relationships PASSED");
	} else if( orphans > 0 ) {
	    ReportManager.problem(this, con, "domain_member has unlinked entries in member FAILED");
        } else {
	    ReportManager.problem(this, con, "domain_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
        }

      } else {
	  ReportManager.correct(this, con, "NO ENTRIES in member table, so nothing to test IGNORED");
      }

      result &= (orphans == 0);

    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
} // OrphanTestCase
