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
 * Check that all exons belong to a transcript.
 */
public class CheckAllExonsBelongToTranscriptTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckAllExonsBelongToTranscript
   */
  public CheckAllExonsBelongToTranscriptTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check that all exons belong to a transcript.");
  }
  
  /** 
   * Find any exons that aren't in a transcript.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rows = getRowCount(con, "select count(distinct e.exon_id) from exon e left join exon_transcript et on e.exon_id =et.exon_id where et.exon_id is null");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have no associated transcript.");
        ReportManager.problem(this, con, rows + " exons have no associated transcript");
      } else {
       ReportManager.correct(this, con, "All exons have associated transcripts"); 
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckAllExonsBelongToTranscript
