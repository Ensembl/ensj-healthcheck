/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * Check that every regulatory_feature of the current Regulatory Build has
 * a valid activity value in at least one epigenome.
 * Valid activity: 'INACTIVE', 'REPRESSED', 'POISED', 'ACTIVE'
 * Invalid activity: 'NA'
 *
 * @author ilavidas
 */


public class RegulatoryFeatureIsActive extends SingleDatabaseTestCase {

    private static final int MAX_ERRORS_REPORTED = 20;

    public RegulatoryFeatureIsActive() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every regulatory_feature of the current "
                + "Regulatory Build has a valid activity value in at least "
                + "one epigenome.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        Connection con = dbre.getConnection();
        int errorCount = 0;

        try {
            //fetch all regulatory_feature_ids of the current Regulatory Build
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT regulatory_feature_id " +
                    "FROM regulatory_feature JOIN regulatory_build USING" +
                    "(regulatory_build_id) WHERE regulatory_build" +
                    ".is_current=1");

            while (rs != null && rs.next()) {
                int regulatoryFeatureID = rs.getInt(1);

                //fetch activity for every regulatory_feature
                Statement newStmt = con.createStatement();
                ResultSet activity = newStmt.executeQuery("SELECT * FROM " +
                        "regulatory_activity WHERE activity!='NA' AND " +
                        "regulatory_feature_id=" + regulatoryFeatureID);

                if (!activity.next()) {
                    ReportManager.problem(this, con, "The regulatory_feature " +
                            "with id " + regulatoryFeatureID + " is not " +
                            "active in any epigenome.");
                    errorCount++;
                    result = false;
                }

                //if the number of errors is too high do not report all of
                // them, as this is usually slow. Print a helpful sql query
                // instead for manual inspection by the user
                if (errorCount > MAX_ERRORS_REPORTED) {

                    String helpfulQuery = "SELECT regulatory_feature" +
                            ".regulatory_feature_id FROM regulatory_build " +
                            "JOIN regulatory_feature USING " +
                            "(regulatory_build_id) LEFT JOIN " +
                            "regulatory_activity ON (regulatory_feature" +
                            ".regulatory_feature_id=regulatory_activity" +
                            ".regulatory_feature_id AND activity!='NA') " +
                            "WHERE regulatory_activity.regulatory_activity_id" +
                            " IS NULL AND regulatory_build.is_current=1";

                    ReportManager.info(this, con, "Too many errors found. The" +
                            " above list is NOT exhaustive! Execute this " +
                            "query to retrieve all regulatory_feature_ids " +
                            "that are not active in any epigenome:\n" +
                            helpfulQuery);

                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
