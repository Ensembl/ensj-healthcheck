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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;



/**
 * An EnsEMBL Healthcheck test case that looks for correct analysis table structure
 */

public class AnalysisLogicName extends SingleDatabaseTestCase {

        /**
         * Create the analysis table.
         */
        public AnalysisLogicName() {

                addToGroup("post_genebuild");
                addToGroup("release");
                addToGroup("compara-ancestral");
                addToGroup("id_mapping");
                addToGroup("pre-compara-handover");
                addToGroup("post-compara-handover");

                setDescription("Check the analysis data is correct.");
                setSecondTeamResponsible(Team.GENEBUILD);

        }

       /**
         * Check the data in the analysis table.
         * 
         * @param dbre
         *          The database to use.
         * @return true if all data is there and in the correct format.
         */


        public boolean run(DatabaseRegistryEntry dbre) {

                boolean result = true;
                Connection con = dbre.getConnection();


                result &= checkdbVersion(con);
                
                result &= checkLowerCase(con);

                return result;
        }


        // ---------------------------------------------------------------------


       /**
         * Check that db_version is not empty if not a raw compute
         */


        private boolean checkdbVersion(Connection con) {

                boolean result = true;
                int rows = getRowCount(con, "SELECT COUNT(*) FROM analysis where isnull(db_version) " );
                if (rows > 0) {
                         result = false ;
                         ReportManager.problem(this, con,  rows + " Analyses are missing db_version");
                }

                return result;
        }



        // ---------------------------------------------------------------------



       /**
         * Check all logic names are lower case 
         */

        private boolean checkLowerCase(Connection con) {

                boolean result = true;

                String[] logicNames = getColumnValues(con, "SELECT logic_name FROM analysis where BINARY logic_name != lower(logic_name) ");
                if (logicNames.length > 0) {
                        ReportManager.problem(this, con, "The following logic_names are not lower case: " + Utils.arrayToString(logicNames,",") );
                } else {
                        ReportManager.correct(this, con, "All logic names are lower case");
                }

                return result;
        }



}


