/*
 * Created on 15-Mar-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the start and end of genes and transcripts make sense.
 */
public class GeneTranscriptStartEnd extends SingleDatabaseTestCase {

    public GeneTranscriptStartEnd() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Checks that gene start/end agrees with transcript table");

    }

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean startEndResult = true;
        boolean strandResult = true;

        // check that the lowest transcript start of a gene's transcripts is the same as the gene's
        // start
        // and that the highest transcript end of a gene's transcripts is the same as the gene's
        // end
        // the SQL below will return any where this is /not/ the case
        String sql = "SELECT g.gene_id, g.seq_region_start AS gene_start, g.seq_region_end AS gene_end, MIN(tr.seq_region_start) AS min_transcript_start, MAX(tr.seq_region_end) AS max_transcript_end FROM gene g, transcript tr where tr.gene_id=g.gene_id GROUP BY tr.gene_id HAVING (gene_start <> min_transcript_start OR gene_end <> max_transcript_end)";

        Connection con = dbre.getConnection();

        try {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ReportManager.problem(this, con, "Gene ID " + rs.getLong(1)
                        + " has start/end that does not agree with transcript start/end");
                startEndResult = false;
            }

            if (startEndResult) {
                ReportManager.correct(this, con, "All gene/transcript start/end agree");
            }

            // also check that all gene's transcripts have the same strand as the gene
            sql = "SELECT g.gene_id FROM gene g, transcript tr WHERE tr.gene_id=g.gene_id AND tr.seq_region_strand != g.seq_region_strand";
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ReportManager.problem(this, con, "Gene ID " + rs.getLong(1)
                        + " has strand that does not agree with transcript strand");
                strandResult = false;
            }

            if (strandResult) {
                ReportManager.correct(this, con, "All gene/transcript strands agree");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startEndResult && strandResult;

    }

}
