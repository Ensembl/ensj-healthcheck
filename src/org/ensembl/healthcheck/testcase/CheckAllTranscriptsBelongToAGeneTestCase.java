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

package org.ensembl.healthcheck.testcase;

import java.sql.*;

import org.ensembl.healthcheck.*;

import org.ensembl.healthcheck.util.*;

/**
 * Check that all transcripts belong to a gene.
 */
public class CheckAllTranscriptsBelongToAGeneTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckAllTranscriptsBelongToAGeneTestCase
   */
  public CheckAllTranscriptsBelongToAGeneTestCase() {
    addToGroup("post_genebuild");
  }
  
  /**
   * Find how many transcripts don't belong to a gene.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rows = getRowCount(con, "select count(distinct t.transcript_id) from transcript t left join gene g on g.gene_id =t.gene_id where g.gene_id is null");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " transcripts in " + DBUtils.getShortDatabaseName(con) + " have no associated gene");
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CheckAllTranscriptsBelongToAGeneTestCase
