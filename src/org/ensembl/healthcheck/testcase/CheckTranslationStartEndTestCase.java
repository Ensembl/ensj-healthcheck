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
 * Check that if the start and end of translation is on the same exon, that start < end.
 */
public class CheckTranslationStartEndTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckTranslationStartEndTestCase
   */
  public CheckTranslationStartEndTestCase() {
    addToGroup("post_genebuild");
  }
  
  /**
   * Find any matching databases that have start > end
   */
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      int rows = getRowCount(con, "select count(translation_id) from translation where start_exon_id = end_exon_id and seq_start > seq_end");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " translations in " + DBUtils.getShortDatabaseName(con) + " have start > end");
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CheckTranslationStartEndTestCase
