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
 * Check for duplicate exons.
 */
public class CheckDuplicateExonsTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckDuplicateExonsTestCase
   */
  public CheckDuplicateExonsTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check for duplicate exons.");
  }
  
  /** 
   * Find any exons that are duplicated.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      // note join needs to include contig_id as it is indexed
      int rows = getRowCount(con, "select count(*) from exon e1, exon e2 where e1.contig_start = e2.contig_start and e1.contig_end=e2.contig_end and e1.contig_strand=e2.contig_strand and e1.phase=e2.phase and e1.end_phase=e2.end_phase and e1.exon_id != e2.exon_id and e1.contig_id = e2.contig_id");    
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " seem to be duplicated.");
        ReportManager.problem(this, con, rows + " exons seem to be duplicated");
      } else {
        ReportManager.correct(this, con, "No duplicate exons"); 
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckDuplicateExonsTestCase

