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
   * Constructor.
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
      
      // check that the tables listed in the meta_coord table actually exist
      // typos in the table names could lead to all sorts of confusion
      
      String[] tableNames = getColumnValues(con, "SELECT table_name FROM meta_coord");
      for (int j = 0; j < tableNames.length; j++) {
        if (checkTableExists(con, tableNames[j])) {
          ReportManager.correct(this, con, tableNames[j] + " listed in meta_coord exists");
        } else {
          ReportManager.problem(this, con, tableNames[j] + " listed in meta_coord does not exist!");
        }
      }
      
      // check for "top_level" and "sequence_level" attribs in the coord_system table
      // there must be one and only one sequence_level co-ordinate system
      // there can be more than one co-ordinate system labelled as top_level but in
      // that case they must have different defaults and versions
      
      // first check for only one sequence_level
      int seqLevelRows = getRowCount(con, "SELECT COUNT(*) FROM coord_system WHERE attrib LIKE '%sequence_level%'");
      if (seqLevelRows == 0) {
        ReportManager.problem(this, con, "No co-ordinate system defined with a sequence_level attribute in coord_system");
      } else if (seqLevelRows == 1) {
        ReportManager.correct(this, con, "coord_system table has one co-ordinate system defined as sequence_level");
      } else if (seqLevelRows > 1) {
        ReportManager.problem(this, con, "coord_system table has " + seqLevelRows + " rows defined with sequence_level attributes - there should be only one");
      }
      
      // check that there is at least one top_level co-ordinate system
      int topLevelRows = getRowCount(con, "SELECT COUNT(*) FROM coord_system WHERE attrib LIKE '%top_level%'");
      if (topLevelRows == 0) {
        ReportManager.problem(this, con, "No co-ordinate system defined with a top_level attribute in coord_system");
      } else if (topLevelRows == 1) {
        ReportManager.correct(this, con, "coord_system table has one co-ordinate system defined as top_level");
      } else if (topLevelRows > 1) {
        // this situation may be acceptable if the versions are different
        int distinctDefaults = getRowCount(con, "SELECT DISTINCT version FROM coord_system WHERE attrib LIKE '%top_level%'");
        if (distinctDefaults == topLevelRows) {
          ReportManager.correct(this, con, "coord_system table has " + topLevelRows + " co-ordinate systems defined as top_level, and all have different versions");
        } else {
          ReportManager.correct(this, con, "coord_system table has " + topLevelRows + " co-ordinate systems defined as top_level, but not all have different versions");
        }
      }
      
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
}