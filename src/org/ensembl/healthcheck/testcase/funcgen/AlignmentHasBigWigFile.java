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

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.MissingMetaKeyException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.io.File;

/**
 * Check that every alignment which has been used for peak calling
 * has an associated BIGWIG file entry stored in data_file table. Check
 * that the file actually exists on the disk.
 * @author jcmarca
 */

public class AlignmentHasBigWigFile extends AbstractExternalFileUsingTestcase {

    public AlignmentHasBigWigFile() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every alignment which has been used for " +
                "peak calling has an associated BIGWIG file entry stored in " +
                "data_file table. Check that the file actually exists on the " +
                "disk.");
    }

    @Override
    public boolean run(DatabaseRegistryEntry dbre){
        boolean result = true;
        Connection con = dbre.getConnection();

        try {
            //fetch file path for every table_id
            Statement stmt = con.createStatement();
            ResultSet alignment_set = stmt.executeQuery("SELECT bigwig_file_id, name FROM alignment where bigwig_file_id is not null");

            while (alignment_set.next()) {
                String sql_query = "select path from data_file where data_file_id = " + alignment_set.getInt("bigwig_file_id") + " and file_type = 'BIGWIG'";
                Statement stmt_data_file = con.createStatement();
                ResultSet data_file_resultset = stmt_data_file.executeQuery(sql_query);

                if (!data_file_resultset.next()) {
                    ReportManager.problem(this, con, "No BIGWIG file entry found in data_file table for alignment " + 
                                                alignment_set.getString("name") + 
                                                " with id " + alignment_set.getInt("bigwig_file_id"));
                    result = false;
                } else {
                    //check that the file actually exists on the disk
                    String parentFuncgenDir;
                    try {
                        
                        parentFuncgenDir = getSpeciesAssemblyDataFileBasePath(dbre);
                        
                    } catch (CoreDbNotFoundException e) {
                        
                        ReportManager.problem(this, dbre.getConnection(), e.getMessage());
                        result = false;
                        continue;
                        
                    } catch (MissingMetaKeyException e) {
                        
                        ReportManager.problem(this, dbre.getConnection(), e.getMessage());
                        result = false;
                        continue;
                        
                    }
                    File file = new File(parentFuncgenDir + data_file_resultset.getString("path"));

                    if (!file.exists()) {
                        ReportManager.problem(this, con, " File " + file
                                .getPath() + " does not exist on the disk.");
                        result = false;
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;


    }
}



