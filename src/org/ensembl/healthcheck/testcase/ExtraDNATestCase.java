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
 * Healthcheck to look for any "extra" DNA, i.e. DNA where there is no associated contig.
 */
public class ExtraDNATestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of ExtraDNATestCase
   */
  public ExtraDNATestCase() {
    
    String[] cols = { "dna_id" };
    addCondition(new HasTableColumnsCondition("dna", cols));
    addCondition(new HasTableColumnsCondition("contig", cols));    
    addToGroup("post_genebuild");
    setDescription("Checks that all DNA has an associated contig.");
    
  }
  
  /**
   * Look for any DNA that has no associated contig.
   */
  public TestResult run() {
    
    boolean result = true;
    
    String sql = "SELECT COUNT(*) FROM dna d LEFT JOIN contig c ON d.dna_id = c.dna_id WHERE c.dna_id is null";
    
    DatabaseConnectionIterator it = getMatchingSchemaIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      int extraRows = getRowCount(con, sql);
      if (extraRows > 0) {
        ReportManager.problem(this, con, extraRows + " rows in DNA table that are not associated with a contig");
      } else {
        ReportManager.correct(this, con, "All DNA has an associated contig.");
      }
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // ExtraDNATestCase
