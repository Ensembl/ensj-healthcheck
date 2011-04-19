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
            result &= checkCountIsZero(con,"homology","threshold_on_ds=0.0");
            result &= checkCountIsZero(con,"homology","threshold_on_ds>2.0");
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
        int numRows = getRowCount(con, sql_count);
        if (numRows > 0) {
            ReportManager.problem(this, con, "FAILED homology contains redundant entries");
            ReportManager.problem(this, con, "FAILURE DETAILS: There are " + numRows + " redundant homology relationships in the DB");
            ReportManager.problem(this, con, "USEFUL SQL: " + sql_summary);
            result = false;
        }

        return result;

    }

} // CheckHomology
