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

public class MultipleGenomicAlignBlockIds extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomicAlignBlockId that applies to a specific set of databases.
     */
    public MultipleGenomicAlignBlockIds() {

        setDescription("Check that every genomic_align_block_id is linked to more than one single genomic_align_id.");
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

        if (tableHasRows(con, "genomic_align")) {

            result &= checkForSingles(con, "genomic_align", "genomic_align_block_id");
 
        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genomic_align table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomicAlignBlockId
