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
 * An EnsEMBL Healthcheck test case which checks all exon of a gene are on the same strand
 * and in the correct order in their transcript..
 */

public class DuplicateExons extends EnsTestCase {
  
  /**
   * Create an OrphanTestCase that applies to a specific set of databases.
   */
  public DuplicateExons() {
    databaseRegexp = "^.*_core_\\d.*";
    addToGroup("db_constraints");
  }
  
  /**
   * Check strand order of exons.
   * @return Result.
   */
  
  public TestResult run() {
    
    boolean result = true;
    
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    
    String sql =
    "SELECT e.exon_id, e.phase, " +
    "MIN( IF     (a.contig_ori=1,(e.contig_start+a.chr_start-a.contig_start)," +
    "                       (a.chr_start+a.contig_end-e.contig_end ))) as start, " +
    "MAX( IF     (a.contig_ori=1,(e.contig_end+a.chr_start-a.contig_start), " +
    "                       (a.chr_start+a.contig_end-e.contig_start)))  as end, " +
    "       a.contig_ori*e.contig_strand as strand, " +
    "       a.chromosome_id " +
    "FROM   exon e, assembly a " +
    "WHERE  e.contig_id = a.contig_id " +
    "GROUP  BY e.exon_id " +
    "ORDER BY chromosome_id, strand, start, end, phase";
	// + " LIMIT  100";

    // System.out.println( sql );

    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      try {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        int exonStart,  exonEnd, exonPhase, exonStrand, exonChromosome, exonId;
	int lastExonStart = -1;
	int lastExonEnd = -1;
	int lastExonPhase = -1;
	int lastExonStrand = -1;
	int lastExonChromosome = -1;
	int duplicateExon = 0;

	boolean first = true;

        while (rs.next()) {
          
          // load the vars
          exonId = rs.getInt(1);
          exonPhase = rs.getInt(2);
          exonStart = rs.getInt(3);
          exonEnd = rs.getInt(4);
          exonStrand = rs.getInt(5);
          exonChromosome = rs.getInt(6);
          
          if( !first ) {
	      if( lastExonChromosome == exonChromosome &&
		  lastExonStart == exonStart &&
		  lastExonEnd == exonEnd &&
		  lastExonPhase == exonPhase &&
		  lastExonStrand == exonStrand ) {
		  duplicateExon++;
		  ReportManager.info( this, con, "Exon " + exonId + " is duplicated." );
	      }
	  } else {
	      first = false ;
	  }
	  
	  lastExonStart = exonStart;
	  lastExonEnd = exonEnd;
	  lastExonChromosome = exonChromosome;
	  lastExonStrand = exonStrand;
	  lastExonPhase = exonPhase;
	}

	if( duplicateExon > 0 ) {
	    ReportManager.problem( this, con, duplicateExon + " duplicated Exons." );
	    result = false;
	}
	rs.close();
	stmt.close();
	
      } catch (Exception e) {
	  result = false;
	  e.printStackTrace();
      }
    } // while rs
    
    return new TestResult(getShortTestName(), result);
    
}
  
  
} // ExonStrandOrder TestCase
