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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the seq_region_strand is +1 or -1 for several tables.
 */

public class Strand extends SingleDatabaseTestCase {

    private String[] tables = {"prediction_transcript", "prediction_exon", "transcript", "gene", "exon"};

    /**
     * Create a new Strand testcase.
     */
    public Strand() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that seq_region_strand is +/-1 in several tables.");

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

        for (int i = 0; i < tables.length; i++) {

            String table = tables[i];
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE seq_region_strand NOT IN (1,-1)";
            Connection con = dbre.getConnection();
            int rows = getRowCount(con, sql);
            if (rows == 0) {
                ReportManager.correct(this, con, "All seq_region_strand in " + table + " are 1 or -1");
            } else {
                ReportManager.problem(this, con, rows + " rows in " + table
                        + " have seq_region_strand not equal to 1 or -1");
            }
        }

        return true;

    } // run

} // Strand
