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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case
 */

public class StableIDNotNull extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of
     * databases.
     */
    public StableIDNotNull() {
        
        // TODO: addToGroup?
        //addToGroup("compara_genomic");
        //addToGroup("compara_homology");
        setDescription("Check that every GeneTree has a NOT NULL stable_id. Note, we only test stable_ids from the protein GeneTrees in the gene_tree_root table where the tree_type = 'tree'");
        setTeamResponsible(Team.COMPARA);
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

        Connection con = dbre.getConnection();

        // check genome_db table has > 0 rows
        if (DBUtils.countRowsInTable(con, "gene_tree_root") == 0) {
            result = false;
            ReportManager.problem(this, con, "gene_tree_root table is empty");
        }
        else {
            ReportManager.correct(this, con, "gene_tree_root table has data");
        }

        String sql = "SELECT * FROM gene_tree_root " +
            "WHERE member_type = 'protein' AND tree_type = 'tree' " +
            "AND stable_id IS NULL";

        String[] query = DBUtils.getColumnValues(con, sql);

        if (query.length > 0) {
            result = false;
            ReportManager.problem(this, con,
                    "Cases where GeneTree stable_id is NULL (and tree type = 'tree'");
            for (int i = 0; i < query.length; i++) {
                ReportManager.problem(this, con, " root_id " + query[i]
                        + " have NULL stable_ids");
            }
        }
        else {
            ReportManager.correct(this, con, "PASSED all GeneTrees have not NULL stable_ids (where tree type = 'tree')");
        }

        return result;
    }
}
