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
 * Check that exon co-ordinates are sensible.
 */
public class CheckExonCoordsTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckExonCoordsTestCase
   */
  public CheckExonCoordsTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check that exon co-ordinates are sensible.");
  }
  
  // -------------------------------------------------------------------------
  
  /** 
   * Check if there are any exons with invalid co-ordinates.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      logger.fine("Checking for start < 1 ...");
      int rows = getRowCount(con, "SELECT COUNT(*) FROM exon WHERE seq_region_start < 1");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_start values < 1.");
        ReportManager.problem(this, con, rows + " exons with seq_region_start values < 1");
      } else {
        ReportManager.correct(this, con, "All exons have seq_region start values >= 1");
      } 
      
      logger.fine("Checking for seq_region start > seq_region_end...");
      rows = getRowCount(con, "SELECT COUNT(*) FROM exon WHERE seq_region_start > seq_region_end;");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_start > contig_end.");
        ReportManager.problem(this, con, rows + " exons with seq_region_start > seq_region_end");
      } else {
        ReportManager.correct(this, con, "All exons have seq_region_start < seq_region_end");
      }
      
      logger.fine("Checking for seq_region_end beyond end of exon ...");
      rows = getRowCount(con, "SELECT COUNT(EXON_ID) FROM exon, seq_region WHERE exon.seq_region_id=seq_region.seq_region_id AND exon.seq_region_end > seq_region.length");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_end values beyond the end of the contig.");
        ReportManager.problem(this, con, rows + " exons with seq_region_end beyond end of contig");
      } else {
        ReportManager.correct(this, con, "No exons have seq_region_end beyond end of contig"); 
      }
      
    } // DBIterator
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
  // -------------------------------------------------------------------------
  
} // CheckExonCoordsTestCase
