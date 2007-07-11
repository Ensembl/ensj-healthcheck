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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the start and end of genes and transcripts make sense.
 */
public class GeneTranscriptStartEnd extends SingleDatabaseTestCase {

    /**
     * Create a new GeneTranscriptStartEnd test case.
     */
    public GeneTranscriptStartEnd() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Checks that gene start/end agrees with transcript table");

    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);

    }
    
    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean startEndResult = true;
        boolean strandResult = true;

        // check that the lowest transcript start of a gene's transcripts is the same as the gene's
        // start
        // and that the highest transcript end of a gene's transcripts is the same as the gene's
        // end
        // the SQL below will return any where this is /not/ the case
        String sql = "SELECT g.gene_id, gsi.stable_id, g.seq_region_start AS gene_start, g.seq_region_end AS gene_end, MIN(tr.seq_region_start) AS min_transcript_start, MAX(tr.seq_region_end) AS max_transcript_end FROM gene g, transcript tr, gene_stable_id gsi WHERE tr.gene_id=g.gene_id AND gsi.gene_id = g.gene_id GROUP BY tr.gene_id HAVING (gene_start <> min_transcript_start OR gene_end <> max_transcript_end)";

        Connection con = dbre.getConnection();

        try {
	    
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
	    
	    rs.beforeFirst();
	    
            // gene GC32491 in drosophila is allowed to have all sorts of things wrong with it
            if (rs != null && !rs.isAfterLast() && rs.next() && dbre.getSpecies() != Species.DROSOPHILA_MELANOGASTER && rs.getString("stable_id") != null && !rs.getString("stable_id").equalsIgnoreCase("CG32491")) {
		
		ReportManager.problem(this, con, "Gene ID " + rs.getLong(1) + " has start/end that does not agree with transcript start/end");
		startEndResult = false;

                while (rs.next()) {
                    ReportManager.problem(this, con, "Gene ID " + rs.getLong(1)
                            + " has start/end that does not agree with transcript start/end");
                    startEndResult = false;
                }
                rs.close();

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
