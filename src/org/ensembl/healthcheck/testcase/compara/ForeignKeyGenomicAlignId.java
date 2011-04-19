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

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyGenomicAlignId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomicAlignBlockId that applies to a specific set of databases.
     */
    public ForeignKeyGenomicAlignId() {

        addToGroup("compara_genomic");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible(Team.COMPARA);

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

        boolean result = true;

       Connection con = dbre.getConnection();

        if (tableHasRows(con, "genomic_align_group") &&
            tableHasRows(con, "genomic_align")) {

            //            result &= checkForOrphans(con, "genomic_align", "genomic_align_id", "genomic_align_group", "genomic_align_id");
            result &= checkForOrphans(con, "genomic_align_group", "genomic_align_id", "genomic_align", "genomic_align_id");

            // Check that all method_link_species_set_ids match the genomic_align_block table
            int mismatches = getRowCount(con, "SELECT COUNT(*) FROM genomic_align ga LEFT JOIN genomic_align_block gab" +
                " USING (genomic_align_block_id) WHERE ga.method_link_species_set_id != gab.method_link_species_set_id");
            if (mismatches > 0) {
                ReportManager.problem(this, con, mismatches + " entries in genomic_align table have a wrong" +
                    " method_link_species_set_id according to genomic_align_block table");
                result = false;
            } else {
                ReportManager.correct(this, con, "All entries in genomic_align table have a correct" +
                    " method_link_species_set_id according to genomic_align_block table");
            }
 
        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genomic_align_group or in genomic_align tables, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomicAlignId
