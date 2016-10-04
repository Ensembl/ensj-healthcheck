/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.funcgen;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Check that every input_subset is linked to a result_set. Check that the
 * result_set exists.
 *
 * @author ilavidas
 */

public class InputSubsetHasResultSet extends SingleDatabaseTestCase {

    private static final int MAX_ERRORS_REPORTED = 20;

    public InputSubsetHasResultSet() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every input_subset is linked to a " +
                "result_set. Check that the result_set exists.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        Connection con = dbre.getConnection();
//        int noLinkErrorCount = 0;
//        int noResultSetErrorCount = 0;

        try {
            //fetch all input_subsets
            Statement stmt = con.createStatement();
            ResultSet inputSubsets = stmt.executeQuery("SELECT " +
                    "input_subset_id,name FROM input_subset");

            while (inputSubsets != null && inputSubsets.next()) {
                int issID = inputSubsets.getInt(1);
                String issName = inputSubsets.getString(2);

                //fetch result_set links for every input_subset
                Statement newStmt = con.createStatement();
                ResultSet resultSetLinks = newStmt.executeQuery("SELECT " +
                        "result_set_id FROM result_set_input WHERE " +
                        "table_id=" + issID);

                // Check that the input_subset has link(s) to result_set
                if (!resultSetLinks.next()) {
                    ReportManager.problem(this, con, "Input_subset " +
                            issName + " with input_subset_id " + issID + " is" +
                            " not linked to any result_set");
//                    noLinkErrorCount++;
                    result = false;
                } else {
                    resultSetLinks.first();
                    while(resultSetLinks.next()){
                        int resultSetID = resultSetLinks.getInt(1);
                        Statement statement = con.createStatement();
                        ResultSet resultSets = statement.executeQuery("SELECT * " +
                                "FROM result_set WHERE result_set_id=" +
                                resultSetID);

                        if(!resultSets.next()){
                            ReportManager.problem(this,con,"Input_subset " +
                                    issName + " with input_subset_id " + issID +
                                    " appears to be linked to result_set_id " +
                                    resultSetID + " but such id does NOT exist in" +
                                    " the result_set table.");
                            result=false;
                        }
                    }
                }

                // If the number of errors is too high do not report all of
                // them, as this is usually slow. Print a helpful sql query
                // instead for manual inspection by the user
//                if (noLinkErrorCount > MAX_ERRORS_REPORTED) {
//
//                    String helpfulQuery = "SELECT input_subset" + "" +
//                            ".input_subset_id, input_subset.name FROM " +
//                            "input_subset LEFT JOIN result_set_input ON " +
//                            "(input_subset_id=table_id AND " +
//                            "table_name='input_subset') WHERE result_set_id " +
//                            "IS NULL";
//
//                    ReportManager.info(this, con, "Too many errors found. The" +
//                            " above list is NOT exhaustive! Execute this " +
//                            "query to retrieve all input_subsets that are not" +
//                            " linked to a result_set:\n" + helpfulQuery);
//
//                    break;
//                }

                // Check that the result_set links found in the
                // result_set_input table exist in the result_set table
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
