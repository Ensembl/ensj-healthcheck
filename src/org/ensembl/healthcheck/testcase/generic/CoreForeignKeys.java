/*
  Copyright (C) 2004 EBI, GRL
 
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.*;

import org.ensembl.healthcheck.testcase.*;

import org.ensembl.healthcheck.*;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key realtionships.
 */

public class CoreForeignKeys extends SingleDatabaseTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public CoreForeignKeys() {
    addToGroup("post_genebuild");
    setDescription("Check for broken foreign-key relationships.");
  }
  
  /**
   * Look for broken foreign-key realtionships.
   * @return Result.
   */
  public boolean run(DatabaseRegistryEntry dbre) {
    
    boolean result = true;
    
    int orphans = 0;
    
    
      
      Connection con = dbre.getConnection();
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
      
      orphans = countOrphans( con, "dna", "seq_region_id", "seq_region", "seq_region_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "dna -> seq_region has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All dna <-> seq_region relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "seq_region", "coord_system_id", "coord_system", "coord_system_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "seq_region -> coord_system has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All seq_region <-> coord_system relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "assembly", "cmp_seq_region_id", "seq_region", "seq_region_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "assembly -> seq_region has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All assembly -> seq_region relationships OK");
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
      "protein_align_feature", "marker_feature", "prediction_transcript", "prediction_exon", 
      "gene", "qtl_feature", "transcript", "karyotype" };
      
      for(int i = 0; i < featTabs.length; i++) {
        String featTab = featTabs[i];
        orphans = countOrphans( con, featTab, "seq_region_id", "seq_region", "seq_region_id", true);
        if( orphans > 0) {
          ReportManager.problem(this, con, featTab + " -> seq_region has unlinked entries");
        } else {
          ReportManager.correct(this, con, "All " + featTab + " -> seq_region relationships OK");
        }
        result &= (orphans == 0);
      }
      
      orphans = countOrphans( con, "seq_region_attrib", "seq_region_id", "seq_region", "seq_region_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "seq_region_attrib -> seq_region has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All seq_region_attrib -> seq_region relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "seq_region_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "seq_region_attrib -> attrib_type has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All seq_region_attrib -> attrib_type relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "misc_feature_misc_set", "misc_feature_id", "misc_feature", "misc_feature_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "misc_feature_misc_set -> misc_feature has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All misc_feature_misc_set -> misc_feature relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "misc_feature_misc_set", "misc_set_id", "misc_set", "misc_set_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "misc_feature_misc_set -> misc_set has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All misc_feature_misc_set -> misc_set relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "misc_feature", "misc_feature_id", "misc_attrib", "misc_feature_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "misc_feature -> misc_attrib has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All misc_feature -> misc_attrib relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "misc_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "misc_feature -> attrib_type has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All misc_feature -> attrib_type relationships OK");
      }
      result &= (orphans == 0);
      
      orphans = countOrphans( con, "assembly_exception", "seq_region_id", "seq_region", "seq_region_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "assembly_exception -> seq_region_id has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All assembly_exception -> seq_region_id relationships OK");
      }
      result &= (orphans == 0);

      orphans = countOrphans( con, "assembly_exception", "exc_seq_region_id", "seq_region", "seq_region_id", true);
      if( orphans > 0) {
        ReportManager.problem(this, con, "assembly_exception (exc)-> seq_region_id has unlinked entries");
      } else {
        ReportManager.correct(this, con, "All assembly_exception (exc)-> seq_region_id relationships OK");
      }
      result &= (orphans == 0);

    return result;
    
  }
  
} // CoreForeignKeys
