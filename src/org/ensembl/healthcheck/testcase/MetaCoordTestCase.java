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
 * Healthcheck for the meta_coord table.
 */

public class MetaCoordTestCase extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public MetaCoordTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check meta_coord table");
  }
  
  /**
   * Check the coord systems in each feature table.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    String[] featureTables = {"exon", "repeat_feature", "simple_feature", "dna_align_feature",
    "protein_align_feature", "marker_feature", "prediction_transcript", "prediction_exon",
    "gene", "qtl_feature", "transcript", "karyotype" };
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      for (int i = 0; i < featureTables.length; i++) {
        
        String featureTable = featureTables[i];
        logger.fine("Checking " +  featureTable);
        
        String sql = "SELECT DISTINCT cs.name FROM " + featureTable + "  f, seq_region sr, coord_system cs " +
        "WHERE f.seq_region_id=sr.seq_region_id AND sr.coord_system_id=cs.coord_system_id;";
        
        // warn if features in a feature table are stored in > 1 coordinate system
        String[] cs = getColumnValues(con, sql);
        
        if (cs.length == 0) {
          
          result = false;          
          ReportManager.problem(this, con, featureTable + " does not appear to have any associated coordinate systems");
          
        } else if (cs.length > 1) {
          
          result = false;
          String problemCoordinateSystems = Utils.arrayToString(cs, ",");
          ReportManager.problem(this, con, featureTable + " has more than one associated coordinate system: " + problemCoordinateSystems);
          
        } else {
         
          ReportManager.correct(this, con, featureTable + " coordinates OK");
          
        }
        
      }
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
}