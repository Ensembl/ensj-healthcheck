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

import java.lang.Integer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMLSSIdHomology extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMLSSIdHomology that applies to a specific set of databases.
     */
    public ForeignKeyMLSSIdHomology() {

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

        if (tableHasRows(con, "method_link_species_set")) {


            /* Check method_link_species_set <-> homology */
            /* All method_link for homologies must have an internal ID between 201 and 299 */
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "homology", "method_link_species_set_id", "method_link_id >= 201 and method_link_id < 300");
            result &= checkForOrphans(con, "homology", "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> family */
            /* All method_link for families must have an internal ID between 301 and 399 */
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "family", "method_link_species_set_id", "method_link_id >= 301 and method_link_id < 400");
            result &= checkForOrphans(con, "family", "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> gene_tree_root */
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "gene_tree_root", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "gene_tree_root", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')");
            result &= checkForOrphans(con, "gene_tree_root", "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> species_tree_root */
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "species_tree_root", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
            result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "species_tree_root", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')");


        } else {

            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMLSSIdHomology
