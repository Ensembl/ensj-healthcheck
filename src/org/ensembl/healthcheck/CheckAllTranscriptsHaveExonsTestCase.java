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
 * Check that all transcripts have an associated exon.
 */
public class CheckAllTranscriptsHaveExonsTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckAllTranscriptsHaveExonsTestCase
   */
  public CheckAllTranscriptsHaveExonsTestCase() {
    databaseRegexp = ".*_core_\\d.*";
    addToGroup("post_genebuild");
  }
  
  /**
   * Find any transcripts that are not associated with an exon.
   */
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rows = getRowCount(con, "SELECT COUNT(transcript.transcript_id) FROM transcript LEFT JOIN exon_transcript on transcript.transcript_id=exon_transcript.transcript_id WHERE exon_transcript.transcript_id IS NULL");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " transcripts in " + DBUtils.getShortDatabaseName(con) + " are not associated with an exon.");
      }
    }
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CheckAllTranscriptsHaveExonsTestCase
