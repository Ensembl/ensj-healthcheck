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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case which checks all exon of a gene are on the
 * same strand and in the correct order in their transcript..
 */

public class DuplicateExons extends SingleDatabaseTestCase {

    private static final int MAX_WARNINGS = 10;

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public DuplicateExons() {

        addToGroup("post_genebuild");
        addToGroup("release");

    }
    
    /**
     * This test only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.CDNA);
        
    }
    
    /**
     * Check for duplicate exons.
     * 
     * @param dbre
     *          The database to check.
     * @return True if the test passes.
     */

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        String sql = "SELECT e.exon_id, e.phase, e.seq_region_start AS start, e.seq_region_end AS end, e.seq_region_id AS chromosome_id, e.end_phase, e.seq_region_strand AS strand "
                + "             FROM exon e ORDER BY chromosome_id, strand, start, end, phase, end_phase";

        Connection con = dbre.getConnection();
        try {
            
            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
            ResultSet rs = stmt.executeQuery(sql);

            int exonStart, exonEnd, exonPhase, exonChromosome, exonId, exonEndPhase, exonStrand;
            int lastExonStart = -1;
            int lastExonEnd = -1;
            int lastExonPhase = -1;
            int lastExonChromosome = -1;
            int lastExonEndPhase = -1;
            int lastExonStrand = -1;
            int duplicateExon = 0;

            boolean first = true;

            while (rs.next()) {

                // load the vars
                exonId = rs.getInt(1);
                exonPhase = rs.getInt(2);
                exonStart = rs.getInt(3);
                exonEnd = rs.getInt(4);
                exonChromosome = rs.getInt(5);
                exonEndPhase = rs.getInt(6);
                exonStrand = rs.getInt(7);
                	
                if (!first) {
                    if (lastExonChromosome == exonChromosome && lastExonStart == exonStart && lastExonEnd == exonEnd
                            && lastExonPhase == exonPhase && lastExonStrand == exonStrand
                            && lastExonEndPhase == exonEndPhase) {
                        duplicateExon++;
                        if (duplicateExon <= MAX_WARNINGS) {
                            ReportManager.warning(this, con, "Exon " + exonId + " is duplicated.");
                        } 
                    }
                } else {
                    first = false;
                }

                lastExonStart = exonStart;
                lastExonEnd = exonEnd;
                lastExonChromosome = exonChromosome;
                lastExonPhase = exonPhase;
                lastExonEndPhase = exonEndPhase;
                lastExonStrand = exonStrand;
                
            } // while rs

            if (duplicateExon > 0) {
                ReportManager.problem(this, con, "Has at least " + duplicateExon + " duplicated exons.");
                result = false;
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }

        return result;

    }

} // DuplicateExons
