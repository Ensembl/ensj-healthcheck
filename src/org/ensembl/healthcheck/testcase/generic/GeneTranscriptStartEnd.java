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

/**
 * Check that the start and end of genes and transcripts make sense.
 */
public class GeneTranscriptStartEnd extends SingleDatabaseTestCase {

    /**
     * Create a new GeneTranscriptStartEnd test case.
     */
    public GeneTranscriptStartEnd() {

        setDescription("Checks that gene start/end agrees with transcript table");
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
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     * 
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean startEndResult = true;
        boolean strandResult = true;

        // check that the lowest transcript start and end coordinates matches
        // the start and end of the gene itself.
        // the SQL below will return any where this is /not/ the case
        String sql = "SELECT g.gene_id, g.stable_id, "
            + "g.seq_region_start AS gene_start, "
            + "g.seq_region_end AS gene_end, "
            + "MIN(tr.seq_region_start) AS min_transcript_start, "
            + "MAX(tr.seq_region_end) AS max_transcript_end "
            + "FROM gene g, transcript tr "
            + "WHERE tr.gene_id=g.gene_id "
            + "GROUP BY tr.gene_id "
            + "HAVING (gene_start <> min_transcript_start "
                + "OR gene_end <> max_transcript_end)";

        Connection con = dbre.getConnection();

        try {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            rs.beforeFirst();

            // gene GC32491 in drosophila is allowed to have all sorts of
            // things wrong with it
            //TODO this is very, very wrong indeed
            if (rs != null && !rs.isAfterLast() && rs.next()
                && !dbre.getSpecies().equals(DatabaseRegistryEntry.DROSOPHILA_MELANOGASTER)
                && rs.getString("stable_id") != null
                && !rs.getString("stable_id").equalsIgnoreCase("CG32491")) {

                ReportManager.problem(this, con, "Gene ID "
                    + rs.getLong(1)
                    + " has start/end that does not agree with transcript start/end");
                startEndResult = false;

                while (rs.next()) {
                    ReportManager.problem(this, con, "Gene ID "
                        + rs.getLong(1)
                        + " has start/end that does not agree with transcript start/end");
                    startEndResult = false;
                }
                rs.close();

                if (startEndResult) {
                    ReportManager.correct(this, con, "All gene/transcript start/end agree");
                }

                // also check that all gene's transcripts have the same strand as the
                // gene
                sql = "SELECT g.gene_id "
                    + "FROM gene g, transcript tr "
                    + "WHERE tr.gene_id=g.gene_id "
                    + "AND tr.seq_region_strand != g.seq_region_strand";
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    ReportManager.problem(this, con, "Gene ID "
                        + rs.getLong(1)
                        + " has strand that does not agree with transcript strand");
                    strandResult = false;
                }
                rs.close();
                stmt.close();

                if (strandResult) {
                    ReportManager.correct(this, con, "All gene/transcript strands agree");
                }

            } // if drosophila gene

        } catch (Exception e) {
            e.printStackTrace();
        }

        return startEndResult && strandResult;

    }

}
