/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
 * Check that every distinct experiment - epigenome - feature_type
 * combination is linked to a feature_set.
 *
 * @author ilavidas
 */


public class ExperimentHasFeatureSet extends SingleDatabaseTestCase {

    public ExperimentHasFeatureSet() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every distinct experiment - epigenome - "
                + "feature_type combination is linked to a feature_set.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        Connection con = dbre.getConnection();

        try {
            //fetch all distinct experiment - epigenome - feature_type
            // combinations from experiment table. Ignore control files
            Statement stmt = con.createStatement();
            ResultSet combos = stmt.executeQuery("SELECT DISTINCT ex" +
                    ".experiment_id, ex.epigenome_id, ex.feature_type_id, ep" +
                    ".display_label, ft.name FROM experiment ex JOIN " +
                    "feature_type ft USING(feature_type_id) JOIN epigenome ep" +
                    " USING(epigenome_id) WHERE ft.name!='WCE'");

            while (combos != null && combos.next()) {
                int experimentID = combos.getInt(1);
                int epigenomeID = combos.getInt(2);
                int featureTypeID = combos.getInt(3);
                String epigenomeName = combos.getString(4);
                String featureTypeName = combos.getString(5);

                //fetch peak_calling for every combination
                Statement newStmt = con.createStatement();
                ResultSet featureSets = newStmt.executeQuery("SELECT " +
                        "peak_calling_id FROM peak_calling WHERE " +
                        " epigenome_id="
                        + epigenomeID + " AND feature_type_id=" +featureTypeID);


                if (!featureSets.next()) {
                    ReportManager.problem(this, con, "There is no peak_calling" +
                            " for this combination: experiment_id = " +
                            experimentID + ", epigenome_id = " + epigenomeID
                            + " (" + epigenomeName + "), feature_type_id = "
                            + featureTypeID + " (" + featureTypeName + ")");

                    result = false;
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}

