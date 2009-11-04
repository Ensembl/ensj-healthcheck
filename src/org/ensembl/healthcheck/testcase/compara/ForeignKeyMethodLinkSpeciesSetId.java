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

public class ForeignKeyMethodLinkSpeciesSetId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkSpeciesSetId() {

        addToGroup("compara_db_constraints");
        addToGroup("protein_db_constraints");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible("compara");

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

            /* Check method_link_species_set <-> species_set */
            result &= checkForOrphans(con,
                "method_link_species_set", "species_set_id",
                "species_set", "species_set_id");
            result &= checkForOrphansWithConstraint(con, "species_set", "species_set_id",
                "method_link_species_set", "species_set_id",
                "species_set_id not in (SELECT distinct species_set_id from species_set_tag)");

            /* Check method_link_species_set <-> synteny_region */
            /* All method_link for syntenies must have an internal ID between 101 and 199 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "synteny_region", "method_link_species_set_id",
                "method_link_id >= 101 and method_link_id < 200");
            result &= checkForOrphans(con,
                "synteny_region", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> homology */
            /* All method_link for homologies must have an internal ID between 201 and 299 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "homology", "method_link_species_set_id",
                "method_link_id >= 201 and method_link_id < 300");
            result &= checkForOrphans(con,
                "homology", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> family */
            /* All method_link for families must have an internal ID between 301 and 399 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "family", "method_link_species_set_id",
                "method_link_id >= 301 and method_link_id < 400");
            result &= checkForOrphans(con,
                "family", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

            /* Check method_link_species_set <-> protein_tree_member */
            /* All method_link for protein trees must have an internal ID between 401 and 499 */
            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "protein_tree_member", "method_link_species_set_id",
                "method_link_id >= 401 and method_link_id < 500");
            result &= checkForOrphans(con,
                "protein_tree_member", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMethodLinkSpeciesSetId
