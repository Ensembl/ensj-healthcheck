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

import org.ensembl.healthcheck.*;

/**
 * EnsEMBL Healthcheck test case that ensures that the results of the SQL
 * query <code>DESCRIBE external_db</code> are the same for a set of databases.
 */

public class ExternalDBDescribeTestCase extends EnsTestCase {

  /** Creates a new instance of ExternalDBDescriptionTestCase */
  public ExternalDBDescribeTestCase() {
    addToGroup("pre_release");
    setDescription("Check that the external_db table is the same in all databases.");
  }
  
  /**
   * Check that the external_db tables are the same for each matched database.
   * @return Result.
   */
  public TestResult run() {
    
    //boolean result = super.checkSameSQLResult("DESCRIBE external_db");
    boolean result = super.checkSameSQLResult("SELECT * FROM external_db ORDER BY external_db_id");
    
    // XXX update when ReportManager can handle non database/test-specific reports
    if (result) {
      ReportManager.correct(this, "", "external_db table is the same in all databases");
    } else {
       ReportManager.problem(this, "", "external_db table is NOT the same in all databases");
    } 
    return new TestResult(getShortTestName(), result);
  }
  
}
