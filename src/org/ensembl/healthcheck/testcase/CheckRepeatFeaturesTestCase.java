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
 * Check that repeat features start<=end.
 */
public class CheckRepeatFeaturesTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckExonCoordsTestCase
   */
  public CheckRepeatFeaturesTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check that repeat features start<=end.");
  }
  
  // -------------------------------------------------------------------------
  
 	public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      String q = "SELECT COUNT(*) FROM repeat_feature WHERE repeat_start>repeat_end";
      int rows = getRowCount(con, q);
      if (rows > 0) {
        result = false;
        ReportManager.problem(this, con, rows + " repeat_features with start> end.");
      } 
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  } 
  
} 