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
import java.util.*;
import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * An EnsEMBL Healthcheck test case which checks that the protein_feature table
 * agrees with the translation table.
 */

public class ProteinFeatureTranslationTestCase extends EnsTestCase {
  
  /**
   * Create an ProteinFeatureTranslationTestCase that applies to a specific set of databases.
   */
  public ProteinFeatureTranslationTestCase() {
    addToGroup("db_constraints");
  }
  
  /**
   *
   * @return Result.
   */
  
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    // get list of transcripts
    String sql =
    "SELECT t.transcript_id, e.exon_id, tr.start_exon_id, tr.translation_id, tr.end_exon_id, tr.seq_start, tr.seq_end, e.contig_start, e.contig_end, e.sticky_rank " +
    "FROM   transcript t, exon_transcript et, exon e, translation tr " +
    "WHERE  t.transcript_id = et.transcript_id AND et.exon_id = e.exon_id AND t.translation_id = tr.translation_id " +
    "ORDER  BY t.transcript_id, et.rank, e.sticky_rank DESC";
    
    while (it.hasNext()) {
      
      try {
        
        Connection con = (Connection)it.next();
        Statement stmt = con.createStatement();
        
        // first get max translation ID so we can dimension the array
        ResultSet rs = stmt.executeQuery("SELECT MAX(translation_id) FROM translation");
        rs.first();
        int maxTranslationID = rs.getInt(1);
        logger.fine("Max translation ID = " + maxTranslationID);
        int[] translationLengths = new int[maxTranslationID + 1]; // better to use a list if the array is going to be sparse
        
        // now calculate and store the translation lengths
        rs = stmt.executeQuery(sql);
        
        boolean inCodingRegion = false;
        
        while(rs.next()) {

          int currentTranslationID = rs.getInt("translation_id");
          
          if (!inCodingRegion) {
            if (rs.getInt("start_exon_id") == rs.getInt("exon_id")) {
              // single-exon-translations
              if (rs.getInt("start_exon_id") == rs.getInt("end_exon_id")) {
                translationLengths[currentTranslationID] = (rs.getInt("seq_end") - rs.getInt("seq_start")) + 1;
                continue;
              }
              inCodingRegion = true;
              translationLengths[currentTranslationID] -= rs.getInt("seq_start");
            }
          } // if !inCoding
          
          if (inCodingRegion) {
            if (rs.getInt("exon_id") == rs.getInt("end_exon_id")) {
              translationLengths[currentTranslationID] += rs.getInt("seq_end");
              inCodingRegion = false;
            } else {
              translationLengths[currentTranslationID] += (rs.getInt("contig_end") - rs.getInt("contig_start")) + 1;
            }
          } // if inCoding
          
        } // while rs
        
        rs.close();
        
        // find protein features where seq_end is > than the length of the translation
        rs = stmt.executeQuery("SELECT protein_feature_id, translation_id, seq_end FROM protein_feature");
        while (rs.next()) {
          int minTranslationLength = (translationLengths[rs.getInt("translation_id")] + 2) / 3; // some codons can only be 2 bp
          if (rs.getInt("seq_end")  > minTranslationLength) {
            ReportManager.problem(this, con, "Protein feature " + rs.getInt("protein_feature_id") + " claims to have length " + rs.getInt("seq_end") + " but translation is of length " + minTranslationLength);
            result = false;
          }
        }
        
        stmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      
    } // while it
    
    return new TestResult(getShortTestName(), result);

  } // run
  
} // ProteinFeatureTranslationTestCase
