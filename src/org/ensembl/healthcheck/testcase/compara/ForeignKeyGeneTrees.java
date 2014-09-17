/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyGeneTrees extends AbstractComparaTestCase {

    /**
     * Create an ForeignKeyMemberId that applies to a specific set of databases.
     */
    public ForeignKeyGeneTrees() {

        addToGroup("compara_homology");
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

        if (tableHasRows(con, "gene_tree_node")) {

            result &= checkForOrphansSameTable(con, "gene_tree_node", "root_id", "node_id", false);
            result &= checkForOrphansSameTable(con, "gene_tree_node", "parent_id", "node_id", true);
            result &= checkForOrphans(con, "gene_tree_node_tag", "node_id", "gene_tree_node", "node_id");
            result &= checkForOrphans(con, "gene_tree_node_attr", "node_id", "gene_tree_node", "node_id");
            result &= checkForOrphans(con, "gene_tree_root", "root_id", "gene_tree_node", "node_id");
            result &= checkForOrphansWithConstraint(con, "homology", "gene_tree_node_id", "gene_tree_node", "node_id", "description != 'alt_allele'");

            result &= checkForOrphansSameTable(con, "gene_tree_root", "ref_root_id", "root_id", true);
            result &= checkForOrphans(con, "gene_tree_root_tag", "root_id", "gene_tree_root", "root_id");
            result &= checkForOrphansWithConstraint(con, "homology", "gene_tree_root_id", "gene_tree_root", "root_id", "description != 'alt_allele'");

            result &= checkForOrphansWithConstraint(con, "gene_tree_root", "gene_align_id", "gene_align", "gene_align_id", "tree_type != 'clusterset'");
            result &= checkForOrphans(con, "gene_align_member", "gene_align_id", "gene_align", "gene_align_id");
            result &= checkForOrphansWithConstraint(con, "gene_tree_root_tag", "value", "gene_align", "gene_align_id", "tag = 'mcoffee_scores'");

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in gene_tree_node table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMemberId
