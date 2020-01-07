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
 * Check that every epigenome which is part of the current Regulatory Build is
 * linked to a segmentation_file.
 *
 * @author ilavidas
 */

public class EpigenomeHasSegmentationFile extends SingleDatabaseTestCase {

    public EpigenomeHasSegmentationFile() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every epigenome which is part of the " +
                "current Regulatory Build is linked to a segmentation_file.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        boolean result = true;
        Connection con = dbre.getConnection();

        try {
            //fetch ID of the current Regulatory Build
            Statement stmt = con.createStatement();
            ResultSet currentRegBuild = stmt.executeQuery("SELECT " +
                    "regulatory_build_id FROM regulatory_build WHERE " +
                    "is_current=1");

            if (! currentRegBuild.next()){
                logger.warning("No current Regulatory Build found");
                return true;
            }

            currentRegBuild.first();
            int currentRegBuildID = currentRegBuild.getInt(1);

            //fetch all epigenomes that are part of the current Regulatory Build
            ResultSet epigenomes = stmt.executeQuery("SELECT rbe" +
                    ".epigenome_id, ep.display_label FROM " +
                    "regulatory_build_epigenome rbe JOIN epigenome ep USING" +
                    "(epigenome_id) WHERE rbe.regulatory_build_id=" +
                    currentRegBuildID);

            while (epigenomes != null && epigenomes.next()) {
                int epigenomeID = epigenomes.getInt(1);
                String epigenomeName = epigenomes.getString(2);

                //check that the segmentation_file table is populated
                Statement newStmt = con.createStatement();
                ResultSet rs = newStmt.executeQuery("SELECT " +
                        "segmentation_file_id FROM segmentation_file WHERE " +
                        "epigenome_id=" + epigenomeID + " AND " +
                        "regulatory_build_id=" + currentRegBuildID);

                if (!rs.next()) {
                    ReportManager.problem(this, con, "No entry found in the " +
                            "segmentation_file table for epigenome " +
                            epigenomeName + " with ID " + epigenomeID);
                    result = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
