/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case which checks all exon of a gene are on the same strand and in
 * the correct order in their transcript.
 */

public class ExonStrandOrder extends SingleDatabaseTestCase {

    public static final int TRANSCRIPT_WARN_LENGTH = 2000000;

    /**
     * Constructor.
     */
    public ExonStrandOrder() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setHintLongRunning(true);

    }

    /**
     * Check strand order of exons.
     * 
     * @return Result.
     */

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        int singleExonTranscripts = 0;
        int transcriptCount = 0;

        String sql = "SELECT g.gene_id, g.seq_region_start, g.seq_region_end, g.seq_region_strand, tr.transcript_id, tr.seq_region_start, tr.seq_region_end, tr.seq_region_strand,e.exon_id, e.seq_region_start, e.seq_region_end, e.seq_region_strand, et.rank "
                + "FROM   gene g, transcript tr, exon_transcript et, exon e "
                + "WHERE  e.exon_id = et.exon_id "
                + "AND    et.transcript_id = tr.transcript_id "
                + "AND    tr.gene_id = g.gene_id "
                + "ORDER BY et.transcript_id, et.rank"; 

        Connection con = dbre.getConnection();
        try {
            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt.executeQuery(sql);

            long lastTranscriptID = -1;
            long lastExonStart = -1;
            long lastExonEnd = -1;
            long lastExonStrand = -2;
            long lastExonID = -1;
            int lastExonRank = 0;

            // ResultSet is ordered by transcript ID and rank, so we can loop through
            // and look at the grouped exons for each transcript

            while (rs.next()) {

                long geneID = rs.getLong(1);
                long geneStart = rs.getLong(2);
                long geneEnd = rs.getLong(3);
                int geneStrand = rs.getInt(4);
                long transcriptID = rs.getLong(5);
                long transcriptStart = rs.getLong(6);
                long transcriptEnd = rs.getLong(7);
                int transcriptStrand = rs.getInt(8);
                long exonID = rs.getLong(9);
                long exonStart = rs.getLong(10);
                long exonEnd = rs.getLong(11);
                int exonStrand = rs.getInt(12);
                int exonRank = rs.getInt(13);

                if (transcriptID == lastTranscriptID) {

                    if (lastExonStrand < -1) {// first exon in "new" transcript

                        lastExonStrand = exonStrand;
                        lastExonStart = exonStart;
                        lastExonEnd = exonEnd;
                        lastExonID = exonID;
                        lastExonRank = exonRank;

                    } else {

                        //System.out.println("Checking tr " + transcriptID + " strand " +
                        // transcriptStrand + " exon " + exonID + " strand " + exonStrand);

                        // check all exons in a transcript have the same strand
                        if (exonStrand != lastExonStrand) {
                            ReportManager.problem(this, con, "Exons in transcript " + transcriptID + " have different strands");
                            result = false;
                        }

                        // check all exons have the same strand as their transcript
                        if (exonStrand != transcriptStrand) {
                            ReportManager.problem(this, con, "Exon " + exonID + " in transcript " + transcriptID + " has strand "
                                    + exonStrand + " but transcript's strand is " + transcriptStrand);
                            result = false;
                        }

                        // check that exon start/ends make sense
                        if (exonStrand == 1) {
                            if (lastExonEnd > exonStart) {
                                ReportManager.problem(this, con, "Exons " + lastExonID + " and " + exonID + " in transcript "
                                        + transcriptID + " appear to overlap (positive strand)");
                                result = false;
                            }
                        } else if (exonStrand == -1) {
                            if (lastExonStart < exonEnd) {
                                ReportManager.problem(this, con, "Exons " + lastExonID + " and " + exonID + " in transcript "
                                        + transcriptID + " appear to overlap (negative strand)");
                                result = false;
                            }
                        }

                        // check for rank jumping
                        if (exonRank - lastExonRank > 1) {
                            ReportManager.problem(this, con, "Exon rank jump in exon " + exonID + " transcript: " + transcriptID
                                    + " gene: " + geneID);
                            result = false;
                        }

                        // get ready for next exon
                        lastExonStrand = exonStrand;
                        lastExonStrand = exonStrand;
                        lastExonStart = exonStart;
                        lastExonEnd = exonEnd;
                        lastExonID = exonID;
                        lastExonRank = exonRank;

                    } // if first exon

                } else {

                    // check for single-exon transcripts (highest rank = 1)
                    if (lastExonRank == 1) {
                        singleExonTranscripts++;
                    }

                    // next
                    lastTranscriptID = transcriptID;
                    lastExonStrand = -2;
                    lastExonStart = -1;
                    lastExonEnd = -1;
                    lastExonID = -1;
                    lastExonRank = 0;

                    // check for overlong transcripts
                    if (transcriptEnd - transcriptStart > TRANSCRIPT_WARN_LENGTH) {
                        ReportManager.warning(this, con, "Long transcript " + lastTranscriptID + " length "
                                + (transcriptEnd - transcriptStart));
                    }

                    transcriptCount++;

                }

            } // while rs

            rs.close();
            stmt.close();
            if ((double) singleExonTranscripts / transcriptCount > 0.2) {
                ReportManager.warning(this, con, "High single exon transcript count. (" + singleExonTranscripts + "/"
                        + transcriptCount + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ReportManager.correct(this, con, "Exon strand order seems OK");

        return result;

    }

} // ExonStrandOrder
