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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class CheckHomology extends SingleDatabaseTestCase {

    /**
     * Create an CheckHomology that applies to a specific set of databases.
     */
    public CheckHomology() {

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

        if (tableHasRows(con, "homology") && tableHasRows(con, "homology")) {
            result &= checkCountIsZero(con,"method_link_species_set_tag","tag='threshold_on_ds' AND value NOT IN (1,2)");
        } else {
            ReportManager.problem(this, con, "NO ENTRIES in homology or homology_member tables");
        }
        
        String sql_main = "SELECT hm1.member_id member_id1, hm2.member_id member_id2, COUNT(*) num, GROUP_CONCAT(h1.description order by h1.description) descs" +
             " FROM homology h1 CROSS JOIN homology_member hm1 USING (homology_id)" +
             " CROSS JOIN homology_member hm2 USING (homology_id)" +
             " WHERE hm1.member_id < hm2.member_id" +
             " GROUP BY hm1.member_id, hm2.member_id HAVING COUNT(*) > 1";
        String sql_count = sql_main;
        String sql_summary = "SELECT descs, num, count(*) FROM (" + sql_main + ") tt1 GROUP BY descs, num";
        int numRows = DBUtils.getRowCount(con, sql_count);
        if (numRows > 0) {
            ReportManager.problem(this, con, "FAILED homology contains redundant entries");
            ReportManager.problem(this, con, "FAILURE DETAILS: There are " + numRows + " redundant homology relationships in the DB");
            ReportManager.problem(this, con, "USEFUL SQL: " + sql_summary);
            result = false;
        }

        return result;

    }

} // CheckHomology
