/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Healthcheck for the dna table. Should be empty in an est database
 */

public class DNAEmpty extends SingleDatabaseTestCase {

    /**
     * Check the assembly_exception table.
     */
    public DNAEmpty() {
        setDescription("Check that dna table is empty");
        setTeamResponsible(Team.GENEBUILD);
    }

    /**
     * This applies to all core schema databases apart from 'core', 'presite' and 'sangervega'
     */
    public void types() {

        removeAppliesToType(DatabaseType.CORE);
        removeAppliesToType(DatabaseType.PRE_SITE);
        removeAppliesToType(DatabaseType.SANGER_VEGA);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM dna");
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, "dna table contains " + rows
                + " rows - it should be empty unless it's a core database ");
        } else {
            ReportManager.correct(this, con, "dna table is empty");
        }

        return result;

    }

} // DNAEmpty
