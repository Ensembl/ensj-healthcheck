/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUsql
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import java.sql.*;
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
  }
  
  // -------------------------------------------------------------------------
  
  /**
   * Check if there are any exons with invalide co-ordinates.
   *
   */
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      logger.fine("Checking for start < 1 ...");
      int rows = getRowCount(con, "SELECT COUNT(*) FROM exon WHERE contig_start < 1");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_start values < 1.");
      }
      
      logger.fine("Checking for contig start > contig_end...");
      rows = getRowCount(con, "SELECT COUNT(*) FROM exon WHERE contig_start > contig_end;");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_start > contig_end.");
      }
      
      logger.fine("Checking for contig_end beyond end of exon ...");
      rows = getRowCount(con, "SELECT COUNT(EXON_ID) FROM exon, contig WHERE exon.contig_id =contig.contig_id AND exon.contig_end > contig.length");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " exons in " + DBUtils.getShortDatabaseName(con) + " have contig_end values beyond the end of the contig.");
      }
      
    } // DBIterator
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
  // -------------------------------------------------------------------------
  
} // CheckExonCoordsTestCase
