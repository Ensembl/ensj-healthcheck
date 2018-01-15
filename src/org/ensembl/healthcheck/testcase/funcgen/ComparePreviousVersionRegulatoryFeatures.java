/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the
 * EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.util.Map;
import java.sql.ResultSet;
import java.util.HashMap;
import java.sql.Statement;
import java.sql.SQLException;

import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;

/**
 * Compare the number of regulatory features in the current Regulatory Build
 * to the one in the previous release
 */

public class ComparePreviousVersionRegulatoryFeatures extends
        ComparePreviousVersionBase {

    public ComparePreviousVersionRegulatoryFeatures() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Compare the number of regulatory features in the " +
                "current Regulatory Build to the one in the previous release");
    }


    @Override
    protected Map getCounts(DatabaseRegistryEntry dbre) {

        Map<String, Integer> counts = new HashMap<String, Integer>();

        Connection con = dbre.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " +
                    "regulatory_feature JOIN regulatory_build USING" +
                    "(regulatory_build_id) WHERE regulatory_build" +
                    ".is_current=1");

            while(rs != null && rs.next()){
                int numberOfRegulatoryFeatures = rs.getInt(1);
                counts.put("",numberOfRegulatoryFeatures);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    @Override
    protected String entityDescription() {
        return "Regulatory Features";
    }

    @Override
    protected double threshold() {
        return 0.90;
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

