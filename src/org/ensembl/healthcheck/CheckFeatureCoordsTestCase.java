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
 * Check that featuer co-ords make sense.
 */
public class CheckFeatureCoordsTestCase extends EnsTestCase {
  
  /**
   * Creates a new instance of CheckFeatureCoordsTestCase
   */
  public CheckFeatureCoordsTestCase() {
    databaseRegexp = ".*_core_\\d.*";
    addToGroup("post_genebuild");
  }
  
  /**
   * Iterate over each affected database and perform various checks.
   */
  TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      logger.info("Checking DNA align features for " + DBUtils.getShortDatabaseName(con) + " ...");
      int rows = getRowCount(con, "select count(*) from dna_align_feature where contig_start > contig_end");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have DNA align features where contig_start > contig_end");
      }
      
      logger.info(".");
      rows = getRowCount(con, "select count(*) from dna_align_feature where contig_start < 1");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have DNA align features where contig_start < 1");
      }

      logger.info(".");
      rows = getRowCount(con, "select count(dna_align_feature_id) from dna_align_feature f, contig c where f.contig_id = c.contig_id and f.contig_end > c.length");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have DNA align features where contig_length > contig_end");
      }
      
      logger.info("Checking protein align features for " + DBUtils.getShortDatabaseName(con) + " ...");
      rows = getRowCount(con, "select count(*) from protein_align_feature where contig_start > contig_end");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have protein align features where contig_start > contig_end");
      }

      logger.info(".");
      rows = getRowCount(con, "select count(*) from protein_align_feature where contig_start < 1");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have protein align features where contig_start < 1");
      }
      
      logger.info(".");
      rows = getRowCount(con, "select count(protein_align_feature_id) from protein_align_feature f, contig c where f.contig_id = c.contig_id and f.contig_end > c.length");
      if (rows > 0) {
        result = false;
        logger.warning(rows + " in " + DBUtils.getShortDatabaseName(con) + " have protein align features where contig_length > contig_end");
      }
      
    } // while connection
    
    return new TestResult(getShortTestName(), result, "");
    
  } // run
  
} // CheckFeatureCoordsTestCase
