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

public class FamilyForeignKeyTestCaseFamilyId extends EnsTestCase {
    
    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public FamilyForeignKeyTestCaseFamilyId() {
	databaseRegexp = "^ensembl_family_.*";
	addToGroup("family_db_constraints");
	setDescription("Check for broken foreign-key relationships in ensembl_family databases.");
    }
    
    public TestResult run() {
	
	boolean result = true;
	
	DatabaseConnectionIterator it = getDatabaseConnectionIterator();
	int orphans = 0;
	
	while (it.hasNext()) {
	    
	    Connection con = (Connection)it.next();
	    // after stable_ids are loaded, there should be a one to one relationship
	    // Following four tests check stable_id fulfill that
	    
	    if( getRowCount( con, "select count(*) from family" ) > 0 ) {
		
		orphans = countOrphans(con, "family", "family_id", "family_members", "family_id", false );
		if (orphans == 0) {
		    ReportManager.correct(this, con, "family <-> family_members relationships PASSED");
		} else if (orphans > 0) {
		    ReportManager.problem(this, con, "family <-> family_members has unlinked entries FAILED");
		} else {
		    ReportManager.problem(this, con, "family <-> family_members TEST NOT COMPLETED, look at the StackTrace if any");
		}
	    } else {
		ReportManager.correct(this, con, "NO ENTRIES in family table, so nothing to test IGNORED");
	    }
	    
	    result &= (orphans == 0);
	    
	}
	
	return new TestResult(getShortTestName(), result);
	
    }
    
} // OrphanTestCase
