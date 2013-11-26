/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock() {

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

        if (tableHasRows(con, "method_link_species_set")) {

            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "genomic_align_block", "method_link_species_set_id",
                "method_link_id in (SELECT method_link_id FROM method_link WHERE class like 'GenomicAlign%')");
            result &= checkForOrphans(con,
                "genomic_align_block", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");
/*              This is now checked in the ForeignKeyGenomicAlignId healthcheck
              result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id",
                  "genomic_align", "method_link_species_set_id", "method_link_id < 100");
              result &= checkForOrphans(con, "genomic_align", "method_link_species_set_id", "method_link_species_set",
                  "method_link_species_set_id");
*/

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock
