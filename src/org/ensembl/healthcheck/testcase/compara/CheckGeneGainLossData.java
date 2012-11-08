/*
 * Copyright (C) 2012 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check compara Gene Gain/Loss tables.
 */

public class CheckGeneGainLossData extends SingleDatabaseTestCase {

        public CheckGeneGainLossData() {

                addToGroup("compara_homology");
                setDescription("Check that we have data coming from ncRNA and protein gain/loss trees");
                setTeamResponsible(Team.COMPARA);
        }

       /**                                                                                                                                                                                   
         * Run the test.
         *
         * @param dbre
         *          The database to use.
         * @return true if the test passed.
         *
         **/

        public boolean run(DatabaseRegistryEntry dbre) {
 
                boolean result = true;

                Connection con = dbre.getConnection();

                String sql_main = "SELECT member_type, count(*)" + 
                        " FROM gene_tree_root gtr JOIN CAFE_gene_family cgf ON(gtr.root_id=cgf.gene_tree_root_id)" +
                        " WHERE gtr.tree_type = 'tree' GROUP BY gtr.member_type";

                int numRows = DBUtils.getRowCount(con, sql_main);
                if (numRows > 2)  {
                        ReportManager.problem(this, con, "FAILED Gene Gain/Loss Data test. Either ncRNA or protein trees don't have gene Gain/Loss trees.");
                        ReportManager.problem(this, con, "FAILURE DETAILS: There are less than 2 member_types [protein/ncRNA] having gene Gain/Loss trees.");
                        ReportManager.problem(this, con, "USEFUL SQL: " + sql_main);
                        return false;
                                              
                }
                return true;
        }

}
