/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case which checks all exon of a gene are on the
 * same strand and in the correct order in their transcript.
 */

public class ExonStrandOrder extends SingleDatabaseTestCase {

    private static final int TRANSCRIPT_WARN_LENGTH = 2000000;

    /**
     * Constructor.
     */
    public ExonStrandOrder() {

        addToGroup("post_genebuild");

        setHintLongRunning(true);
        setTeamResponsible(Team.GENEBUILD);

    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    /**
     * Check strand order of exons.
     * 
     * @param dbre
     *            The database to use.
     * @return Result.
     */

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        int singleExonTranscripts = 0, transcriptCount = 0;

        String sql = "SELECT g.gene_id, g.seq_region_start, g.seq_region_end, "
            + "g.seq_region_strand, tr.transcript_id, tr.seq_region_start, "
            + "tr.seq_region_end, tr.seq_region_strand, e.exon_id, "
            + "e.seq_region_start, e.seq_region_end, e.seq_region_strand, "
            + "et.rank, g.stable_id, tr.stable_id "
            + "FROM   gene g, transcript tr, exon_transcript et, exon e "
            + "WHERE  e.exon_id = et.exon_id "
            + "AND    et.transcript_id = tr.transcript_id "
            + "AND    tr.gene_id = g.gene_id "
            + "AND    tr.transcript_id NOT IN "
            + "(SELECT transcript_id FROM transcript_attrib "
                + "INNER JOIN attrib_type USING (attrib_type_id) "
                + "WHERE code='trans_spliced') "
            + "ORDER BY et.transcript_id, et.rank";
        System.out.println(sql);
        Connection con = dbre.getConnection();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                     java.sql.ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            rs = st.executeQuery(sql);

            long lastTranscriptID   = -1;
            long lastExonStart      = -1;
            long lastExonEnd        = -1;
            long lastExonStrand     = -2;
            long lastExonID         = -1;
            int lastExonRank        = 0;

            // ResultSet is ordered by transcript ID and rank, so we can loop
            // through
            // and look at the grouped exons for each transcript
            while (rs.next()) {

                long geneID = rs.getLong(1);
                // long geneStart = rs.getLong(2);
                // long geneEnd = rs.getLong(3);
                // int geneStrand = rs.getInt(4);
                long transcriptID = rs.getLong(5);
                long transcriptStart = rs.getLong(6);
                long transcriptEnd = rs.getLong(7);
                int transcriptStrand = rs.getInt(8);
                long exonID = rs.getLong(9);
                long exonStart = rs.getLong(10);
                long exonEnd = rs.getLong(11);
                int exonStrand = rs.getInt(12);
                int exonRank = rs.getInt(13);
                String geneStableID = rs.getString(14);
                String transcriptStableID = rs.getString(15);

                if (transcriptID == lastTranscriptID) {

                    if (lastExonStrand < -1) {
                        // first exon in "new" transcript
                        lastExonStrand = exonStrand;
                        lastExonStart = exonStart;
                        lastExonEnd = exonEnd;
                        lastExonID = exonID;
                        lastExonRank = exonRank;

                    } else {

                        // check all exons in a transcript have the same strand
                        if (exonStrand != lastExonStrand) {
                            ReportManager.problem(this, con,
                                "Exons in transcript " + transcriptID
                                + " have different strands");
                            result = false;
                        }

                        // check all exons have the same strand as their
                        // transcript
                        if (exonStrand != transcriptStrand) {
                            ReportManager.problem(this, con, "Exon " + exonID
                                + " in transcript " + transcriptID
                                + " has strand " + exonStrand
                                + " but transcript's strand is "
                                + transcriptStrand);
                            result = false;
                        }

                        // check that exon start/ends make sense
                        if (exonStrand == 1) {
                            if (lastExonEnd > exonStart) {
                                ReportManager.problem(this, con,
                                    "Exons " + lastExonID
                                    + " (end " + lastExonEnd + ") and " + exonID
                                    + " (start " + exonStart + ") "
                                    + "in transcript " + transcriptID
                                    + " appear to overlap (positive strand)");
                                result = false;
                            }
                        } else if (exonStrand == -1) {
                            if (lastExonStart < exonEnd) {
                                ReportManager.problem(this, con,
                                    "Exons " + lastExonID
                                    + " (start " + lastExonStart + ") and " + exonID
                                    + " (end " + exonEnd + ") "
                                    + "in transcript " + transcriptID
                                    + " appear to overlap (negative strand)");
                                result = false;
                            }
                        }

                        // check for rank jumping
                        if (exonRank - lastExonRank > 1) {
                            ReportManager.problem(this, con,
                                "Exon rank jump in exon " + exonID
                                + " transcript: " + transcriptID
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

                    transcriptCount++;

                }

            } // while rs

            if ((double) singleExonTranscripts / transcriptCount > 0.2) {
                ReportManager.warning(this, con,
                    "High single exon transcript count. ("
                    + singleExonTranscripts + "/" + transcriptCount + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeQuietly(rs);
            DBUtils.closeQuietly(st);
        }
        ReportManager.correct(this, con, "Exon strand order seems OK");

        return result;

    }

} // ExonStrandOrder
