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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key realtionships.
 */

public class CoreForeignKeyTestCase extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public CoreForeignKeyTestCase() {
    addToGroup("post_genebuild");
    setDescription("Check for broken foreign-key relationships.");
  }
  
  /**
   * Look for broken foreign-key realtionships.
   * @return Result.
   */
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    int orphans = 0;
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      // after stable_ids are loaded, there should be a one to one relationship
      // Following four tests check stable_id fulfill that
      
      if( getRowCount( con, "select count(*) from gene_stable_id" ) > 0 ) {
        orphans = countOrphans(con, "gene", "gene_id", "gene_stable_id", "gene_id", false );
        if( orphans > 0 ) {
          //warn( con, "gene <-> gene_stable_id has unlinked entries" );
          ReportManager.problem(this, con, "gene <-> gene_stable_id has unlinked entries");
        } else {
          ReportManager.correct(this, con, "All gene <-> gene_stable_id relationships OK");
        }
      }
      
      result &= (orphans == 0);
      
      if( getRowCount( con, "select count(*) from transcript_stable_id" ) > 0 ) {
        orphans = countOrphans(con, "transcript", "transcript_id", "transcript_stable_id", "transcript_id", false );
        if( orphans > 0 ) {
          //warn( con, "transcript <-> transcript_stable_id has unlinked entries" );
          ReportManager.problem(this, con, "transcript <-> transcript_stable_id has unlinked entries");
        } else {
          ReportManager.correct(this, con, "All transcript <-> transcript_stable_id relationships OK");
        }
      }
      result &= (orphans == 0);
      
      if( getRowCount( con, "select count(*) from translation_stable_id" ) > 0 ) {
        orphans = countOrphans(con, "translation", "translation_id", "translation_stable_id", "translation_id", false );
        if( orphans > 0 ) {
          //warn( con, "translation <-> translation_stable_id has unlinked entries" );
          ReportManager.problem(this, con, "translation <-> translation_stable_id has unlinked entries");
        } else {
          ReportManager.correct(this, con, "All translation <-> translation_stable_id relationships OK");
        }
      }
      result &= (orphans == 0);
      
      if( getRowCount( con, "select count(*) from exon_stable_id" ) > 0 ) {
        orphans = countOrphans(con, "exon", "exon_id", "exon_stable_id", "exon_id", false );
        if( orphans > 0 ) {
          //warn( con, "exon <-> exon_stable_id has unlinked entries" );
          ReportManager.problem(this, con, "exon <-> exon_stable_id has unlinked entries");
        } else {
          ReportManager.correct(this, con, "All exon <-> exon_stable_id relationships OK");
        }
      }
      result &= (orphans == 0);
      
      // following needs always to be true, no row cuont tests necessary
      orphans = countOrphans( con, "exon", "exon_id", "exon_transcript", "exon_id", false );
      if( orphans > 0 ) {
        //warn( con, "exon <-> exon_transcript has unlinked entries" );
        ReportManager.problem(this, con, "exon <-> exon_transcript has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All exon <-> exon_transcript relationships are OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "transcript", "transcript_id", "exon_transcript", "transcript_id", false );
      if( orphans > 0 ) {
        //warn( con, "transcript <-> exon_transcript has unlinked entries" );
        ReportManager.problem(this, con, "transcript <-> exon_transcript has unlinked entries" );
      } else {
        ReportManager.correct(this, con, "All transcript <-> exon_transcript relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "gene", "gene_id", "transcript", "gene_id", false );
      if( orphans > 0 ) {
        //warn( con, "gene <-> transcript has unlinked entries" );
        ReportManager.problem(this, con, "gene <-> transcript has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All gene <-> transcript relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "object_xref", "xref_id", "xref", "xref_id", true );
      if( orphans > 0 ) {
        //warn( con, "object_xref <-> xref has unlinked entries" );
        ReportManager.problem(this, con, "object_xref -> xref has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All object_xref -> xref relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "xref", "external_db_id", "external_db", "external_db_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "xref -> external_db has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All xref -> external_db relationships OK");
      }
      result &= (orphans == 0);
      


      orphans = countOrphans( con, "contig", "dna_id", "dna", "dna_id", false);
      if( orphans > 0) {
        ReportManager.problem(this, con, "contig <-> dna has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All contig <-> dna relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "assembly", "contig_id", "contig", "contig_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "assembly -> contig has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All assembly -> contig relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans(con, "marker_feature", "marker_id", 
			     "marker", "marker_id", true);
      if(orphans > 0) {
          ReportManager.problem(this, con, "marker_feature -> marker has "
                                + "unlinked entries");
      }	else {
          ReportManager.correct(this, con, "All marker_featre -> marker " 
                                + "relationships OK");
      }
      result &= (orphans == 0);

      /*
       * make sure that feature tables reference existing contigs
       */
      String[] featTabs = {"exon", "repeat_feature", "simple_feature", "dna_align_feature",
                           "protein_align_feature", "marker_feature", "prediction_transcript"};

      for(int i = 0; i < featTabs.length; i++) {
          String featTab = featTabs[i];
          orphans = countOrphans( con, featTab, "contig_id", "contig", "contig_id", true);
          if( orphans > 0) {
              ReportManager.problem(this, con, featTab + " -> contig has unlinked entries");
          } else {
              ReportManager.correct(this, con, "All " + featTab + " -> contig relationships OK");
          }
          result &= (orphans == 0);
      }
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
} // OrphanTestCase
