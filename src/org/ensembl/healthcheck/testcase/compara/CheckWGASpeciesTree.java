/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;


/**
 * An EnsEMBL Healthcheck test case that looks for WGA entries missing a
 * species tree.
 */

public class CheckWGASpeciesTree extends SingleDatabaseTestCase {

    public CheckWGASpeciesTree() {
        setDescription("Check for WGAs missing a species tree.");
        setTeamResponsible(Team.COMPARA);
    }

    public boolean run(DatabaseRegistryEntry dbre) {
        Connection con = dbre.getConnection();

        if (!tableHasRows(con, "method_link_species_set")) {
            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
		}

        boolean result = true;
		result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", "species_tree_root", "method_link_species_set_id", "method_link_id IN (SELECT method_link_id FROM method_link WHERE (class LIKE 'GenomicAlignTree%' OR class LIKE '%multiple_alignment' OR class LIKE '%tree_node'))");
        return result;
    }

} // CheckWGASpeciesTree
