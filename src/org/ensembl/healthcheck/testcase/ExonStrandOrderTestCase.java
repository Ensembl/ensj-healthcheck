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

public class ExonStrandOrderTestCase extends EnsTestCase {
  
    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public ExonStrandOrderTestCase() {
	databaseRegexp = "^.*_core_\\d.*";
	addToGroup("db_constraints");
    }
    
    public TestResult run() {
	
	boolean result = true;
    
	DatabaseConnectionIterator it = getDatabaseConnectionIterator();
	int singleExonTranscripts = 0;
	int transcriptCount = 0;
    
	String sql = 
	    "SELECT t.gene_id, t.transcript_id, e.exon_id, " +
	    "IF     (a.contig_ori=1,(e.contig_start+a.chr_start-a.contig_start)," +
	    "                       (a.chr_start+a.contig_end-e.contig_end )) as start, " +  
	    "IF     (a.contig_ori=1,(e.contig_end+a.chr_start-a.contig_start), " +
	    "                       (a.chr_start+a.contig_end-e.contig_start)) as end, " +
	    "       a.contig_ori*e.contig_strand, " +
	    "       a.chromosome_id, " +
	    "       et.rank " +
	    "FROM   transcript t, exon_transcript et ,exon e, assembly a " +
	    "WHERE  t.transcript_id = et.transcript_id " +
	    "AND    et.exon_id = e.exon_id " +
	    "AND    e.contig_id = a.contig_id " +
	    "ORDER  BY t.gene_id, t.transcript_id, et.rank, e.sticky_rank ";
	// +
	//				"LIMIT  100";
	// + "GROUP BY et.transcript_id, e.exon_id";
          
        
	while (it.hasNext()) {
	
	    Connection con = (Connection)it.next();
	    try {	
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		int transcriptStart, geneStart, lastStart;
		int lastTranscriptId = -1;
		int lastGeneId = -1;
		int lastRank = -1;
	    
		int geneId, transcriptId, exonId, start, end, strand, 
		    chromosomeId, exonRank, currentStrand, currentChromosomeId;
		geneStart = -1;
		currentStrand = 0;
		currentChromosomeId = -1;
		lastStart = -1;
	    
		while (rs.next()) {
		
		    // load the vars
		    geneId = rs.getInt(1);
		    transcriptId = rs.getInt(2);
		    exonId = rs.getInt(3);
		    start = rs.getInt(4);
		    end = rs.getInt(5);
		    strand = rs.getInt(6);
		    chromosomeId = rs.getInt(7);
		    exonRank = rs.getInt(8);

		
		    if( transcriptId != lastTranscriptId ) {
			lastTranscriptId = transcriptId;
			if( lastRank == 1 ) {
			    singleExonTranscripts++;
			}
			if( strand == 1 ) {
			    transcriptStart = start;
			} else {
			    transcriptStart = end;
			}
		    
			if( lastGeneId != geneId ) {
			    geneStart = transcriptStart;
			    currentStrand = strand;
			    currentChromosomeId = chromosomeId;
			
			} else {
			    if( strand == 1 ) {
				geneStart = transcriptStart < geneStart ? 
				    transcriptStart : geneStart;
			    } else {
				geneStart = transcriptStart > geneStart ? 
				    transcriptStart : geneStart;
			    }
			}
			lastRank = exonRank;
			lastStart = start;
			transcriptCount++;
			continue;
		    }
								
		    // strand or chromosome jumping in Gene
		    if( strand != currentStrand ||
			chromosomeId != currentChromosomeId ) {
			warn( con, "Jumping strand or chromosome Exon " + exonId +
			      " Transcript: " + transcriptId + 
			      " Gene: " + geneId );
		    }

		    // order of exons right test
		    if( strand == 1 ) {
			if( lastStart > start ) {
			    // test fails 
			    warn( con, "Order wrong in Exon " + exonId +
				  " Transcript: " + transcriptId + 
				  " Gene: " + geneId );
			    result = false;
			}
		    } else {
			if( lastStart < start ) {
			    // test fails 
			    warn( con, "Order wrong in Exon " + exonId +
				  " Transcript: " + transcriptId + 
				  " Gene: " + geneId );
			    result = false;
			}
		    }												
								
		    if( exonRank - lastRank > 1 ) {
			warn( con, "Exon rank jump in Exon " + exonId +
			      " Transcript: " + transcriptId + 
			      " Gene: " + geneId );
			result = false;
		    }

		    lastRank = exonRank;
		} // while rs
		rs.close();
		stmt.close();
		if( (double)singleExonTranscripts / transcriptCount > 0.2 ) {
		    warn( con, "High single exon transcript count. (" +
			  singleExonTranscripts + "/" +
			  transcriptCount + ")" );
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}
    
	return new TestResult(getShortTestName(), result, "");
    
    }

  
} // ExonStrandOrder TestCase
