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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class ComparaForeignKeyTestCaseGenomeDbId extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public ComparaForeignKeyTestCaseGenomeDbId() {
      databaseRegexp = "^ensembl_compara_.*";
      addToGroup("compara_db_constraints");
      setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
  }
  
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    int orphans = 0;
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      // 5 tests to check genome_db_id used as foreign key
      
      if( getRowCount( con, "select count(*) from genome_db" ) > 0 ) {
        orphans = countOrphans(con, "dnafrag", "genome_db_id", "genome_db", "genome_db_id", true );
        if( orphans > 0 ) {
          ReportManager.problem(this, con, "dnafrag has unlinked entries in genome_db");
        } else {
          ReportManager.correct(this, con, "dnafrag -> genome_db relationships OK");
        }
  
        orphans = countOrphans(con, "genomic_align_genome", "consensus_genome_db_id", "genome_db", "genome_db_id", true );
        if( orphans > 0 ) {
          ReportManager.problem(this, con, "consensus_genome_db_id in genomic_align_genome has unlinked entries to genome_db");
        } else {
          ReportManager.correct(this, con, "consensus_genome_db_id in genomic_align_genome -> genome_db relationships OK");
        }
  
        orphans = countOrphans(con, "genomic_align_genome", "query_genome_db_id", "genome_db", "genome_db_id", true );
        if( orphans > 0 ) {
          ReportManager.problem(this, con, "query_genome_db_id in genomic_align_genome has unlinked entries to genome_db");
        } else {
          ReportManager.correct(this, con, "query_genome_db_id in genomic_align_genome -> genome_db relationships OK");
        }
  
        orphans = countOrphans(con, "gene_relationship_member", "genome_db_id", "genome_db", "genome_db_id", true );
        if( orphans > 0 ) {
          ReportManager.problem(this, con, "gene_relationship_member has unlinked entries to genome_db");
        } else {
          ReportManager.correct(this, con, "gene_relationship_member -> genome_db relationships OK");
        }
  
        orphans = countOrphans(con, "method_link_species", "genome_db_id", "genome_db", "genome_db_id", false );
        if( orphans > 0 ) {
          ReportManager.problem(this, con, "method_link_species has unlinked entries in genome_db");
        } else {
          ReportManager.correct(this, con, "method_link_species <-> genome_db relationships OK");
        }
      }
      
      result &= (orphans == 0);
     
    }
    
    return new TestResult(getShortTestName(), result);
    
  }
  
} // OrphanTestCase
