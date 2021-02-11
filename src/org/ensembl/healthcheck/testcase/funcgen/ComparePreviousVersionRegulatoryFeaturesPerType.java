/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.util.Map;
import java.sql.ResultSet;
import java.util.HashMap;
import java.sql.Statement;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;

/**
 * Compare the number of regulatory features per type (promoter, enhancer etc.)
 * in the current Regulatory Build to the one in the previous release
 */

public class ComparePreviousVersionRegulatoryFeaturesPerType extends
        RegulationComparePreviousVersion {

    public ComparePreviousVersionRegulatoryFeaturesPerType() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Compare the number of regulatory features per type " +
                "(promoter, enhancer etc.) in the current Regulatory Build to" +
                " the one in the previous release");
    }


    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {

        Map<String, Integer> counts = new HashMap<String, Integer>();

        Connection con = dbre.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT feature_type.name,count" +
                    "(*) FROM regulatory_feature JOIN " +
                    "feature_type USING(feature_type_id) JOIN " +
                    "regulatory_build USING(regulatory_build_id) WHERE " +
                    "regulatory_build.is_current=1 GROUP BY feature_type" +
                    ".feature_type_id");

            while (rs != null && rs.next()) {
                String featureTypeName = rs.getString(1);
                int numberOfRegulatoryFeatures = rs.getInt(2);
                counts.put(featureTypeName, numberOfRegulatoryFeatures);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    @Override
    protected String entityDescription() {
        return "Regulatory Features of type";
    }

    @Override
    protected double threshold() {
        return 0.95;
    }

    @Override
    protected double minimum() {
        return 0;
    }

    @Override
    protected boolean testUpperThreshold(){
        return true;
    }
}

