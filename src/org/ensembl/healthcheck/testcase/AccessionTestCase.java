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
 * Check for presence and format of PFAM etc hits. Also checks for protein features
 * with not hit_id
 */

public class AccessionTestCase extends EnsTestCase {
  
  private HashMap logicNames = new HashMap();
  private HashMap format = new HashMap();
  
  
  /**
   * Constructor.
   */
  public AccessionTestCase() {
    
    addToGroup("post_genebuild");
    setDescription("Check for presence and format of PFAM etc hits");
    
    // add to these hashes to check for other types and formats
    logicNames.put("pfam", "pfam");
    format.put("pfam", "PF_____");
    
  }
  
  /**
   * Check each type of hit.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      Set keys = logicNames.keySet();
      Iterator it2 = keys.iterator();
      
      while (it2.hasNext()) {
        
        String key = (String)it2.next();
        logger.fine("Checking for logic name " + logicNames.get(key) + " with hits of format " + format.get(key));
        
        // check that there is at least one hit
        int hits = getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='" + logicNames.get(key) + "' AND a.analysis_id=pf.analysis_id");
        if (hits < 1) {
          ReportManager.problem(this, con, "No proteins with " + logicNames.get(key) + " hits; this is only a problem for *_core_* databases");
        } else {
          ReportManager.correct(this, con, hits  + " proteins with " + logicNames.get(key) + "  hits");
        }
        
        // check format of hits
        int badFormat = getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='" + logicNames.get(key) + "' AND a.analysis_id=pf.analysis_id AND pf.hit_id NOT LIKE '" + format.get(key) + "'");
        if (badFormat > 0) {
          ReportManager.problem(this, con, badFormat + " " + logicNames.get(key) + " hit IDs are not in the correct format");
        } else {
          ReportManager.correct(this, con, "All " + logicNames.get(key) + " hits are in the correct format");
        }
        
      }
      
      // check for protein features with no hit_id
      int nullHitIDs = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_id IS NULL OR hit_id=''");
      if (nullHitIDs > 0) {
        ReportManager.problem(this, con, nullHitIDs + " protein features have null or blank hit_ids");
      } else {
        ReportManager.correct(this, con, "No protein features have null or blank hit_ids");
      }
      
    }    
    
    return new TestResult(getShortTestName(), result);
    
  }
  
}