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
 * Check that all transcripts have an associated translation.
 */
public class CheckTranscriptsTranslateTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckTranscriptsTranslateTestCase
   */
  public CheckTranscriptsTranslateTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check that all transcripts have an associated translation");
  }
  
  /**
   * Find any transcripts that don't translate.
   * @todo use countOrphans()?
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rows = getRowCount(con, "select count(gene.gene_id) from gene left join transcript on gene.gene_id=transcript.gene_id where transcript.gene_id is NULL");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " transcripts have no associated gene");
        ReportManager.problem(this, con, rows + " transcripts have no associated gene");
      } else {
       ReportManager.correct(this, con, "All transcripts have associated genes"); 
      }
      
      rows = getRowCount(con, "select count(t.transcript_id) from transcript t left join translation tr on t.transcript_id = tr.translation_id where t.translation_id is NULL");
      if (rows > 0) {
        result = false;
        //logger.warning(rows + " transcripts have no associated translation");
        ReportManager.problem(this, con, rows + " transcripts have no associated translation");
      } else {
       ReportManager.correct(this, con, "All transcripts have translations"); 
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // CheckTranscriptsTranslateTestCase
