/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
 * Check that every read_file is linked to an alignment. Check that the
 * alignment exists.
 *
 * @author ilavidas
 */

public class ReadFileHasBeenAligned extends SingleDatabaseTestCase {

    private static final int MAX_ERRORS_REPORTED = 20;

    public ReadFileHasBeenAligned() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every read_file is linked to an alignment." +
                " Check that the alignment exists.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        Connection con = dbre.getConnection();
//        int noLinkErrorCount = 0;
//        int noResultSetErrorCount = 0;

        try {
            //fetch all read_files
            Statement stmt = con.createStatement();
            ResultSet readFiles = stmt.executeQuery("SELECT " +
                    "read_file_id,name FROM read_file");

            while (readFiles != null && readFiles.next()) {
                int rfID = readFiles.getInt(1);
                String rfName = readFiles.getString(2);

                //fetch alignments for every read_file
                Statement newStmt = con.createStatement();
                ResultSet alignmentLinks = newStmt.executeQuery("SELECT " +
                        "alignment_id FROM alignment_read_file WHERE " +
                        "read_file_id=" + rfID);

                // Check that the read_file has link(s) to alignments
                if (!alignmentLinks.next()) {
                    /*
                    ReportManager.problem(this, con, "Read_file " + rfName +
                            " with read_file_id " + rfID + " is not linked to" +
                            " any alignment");
//                    noLinkErrorCount++;
                    result = false;
                    */
                    ReportManager.warning(this, con, "Read_file " + rfName +
                            " with read_file_id " + rfID + " is not linked to" +
                            " any alignment");
//                    noLinkErrorCount++;
                    result = true;

                } else {
                    alignmentLinks.first();
                    while (alignmentLinks.next()) {
                        int alignmentID = alignmentLinks.getInt(1);
                        Statement statement = con.createStatement();
                        ResultSet alignments = statement.executeQuery("SELECT" +
                                " * FROM alignment WHERE alignment_id=" +
                                alignmentID);

                        if (!alignments.next()) {
                            ReportManager.problem(this, con, "Read_file " +
                                    rfName + " with read_file_id " + rfID + "" +
                                    " appears to be linked to alignment_id "
                                    + alignmentID + " but such id does NOT " +
                                    "exist in the alignment table.");
                            result = false;
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
