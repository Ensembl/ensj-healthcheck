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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyGenomeDbId extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public ForeignKeyGenomeDbId() {

        addToGroup("compara_db_constraints");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");

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

        boolean result = true;

        int orphans = 0;

        Connection con = dbre.getConnection();
        // 5 tests to check genome_db_id used as foreign key

        if (tableHasRows(con, "genome_db")) {
            orphans = countOrphans(con, "dnafrag", "genome_db_id", "genome_db", "genome_db_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con, "dnafrag -> genome_db relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "dnafrag has unlinked entries in genome_db FAILED");
            } else {
                ReportManager.problem(this, con,
                        "dnafrag ->  genome_db TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "genomic_align_genome", "consensus_genome_db_id", "genome_db", "genome_db_id",
                    true);
            if (orphans == 0) {
                ReportManager.correct(this, con,
                        "consensus_genome_db_id in genomic_align_genome -> genome_db relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con,
                        "consensus_genome_db_id in genomic_align_genome has unlinked entries to genome_db FAILED");
            } else {
                ReportManager.problem(this, con,
                        "genomic_align_genome -> genome_db TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "genomic_align_genome", "query_genome_db_id", "genome_db", "genome_db_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con,
                        "query_genome_db_id in genomic_align_genome -> genome_db relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con,
                        "query_genome_db_id in genomic_align_genome has unlinked entries to genome_db FAILED");
            } else {
                ReportManager
                        .problem(this, con,
                                "genomic_align_genome -> genome_db relationships TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "member", "genome_db_id", "genome_db", "genome_db_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con, "member -> genome_db relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "member has unlinked entries to genome_db FAILED");
            } else {
                ReportManager.problem(this, con,
                        "member -> genome_db TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "method_link_species", "genome_db_id", "genome_db", "genome_db_id", false);
            if (orphans == 0) {
                ReportManager.correct(this, con, "method_link_species <-> genome_db relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "method_link_species has unlinked entries in genome_db FAILED");
            } else {
                ReportManager.problem(this, con,
                        "method_link_species <-> genome_db TEST NOT COMPLETED, look at the StackTrace if any");
            }
        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genome_db table, so nothing to test IGNORED");
        }

        result &= (orphans == 0);

        return result;

    }

} // OrphanTestCase
