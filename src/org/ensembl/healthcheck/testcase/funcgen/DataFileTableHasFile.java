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

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.MissingMetaKeyException;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Given a list of ids (alignment_id, segmentation_file_id), check that
 * every id has an associated file entry stored in data_file table.
 * Check that the file actually exists on the disk.
 *
 * @author ilavidas
 */

abstract class DataFileTableHasFile extends AbstractExternalFileUsingTestcase {

    DataFileTableHasFile() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Given a list of ids (alignment_id, " +
                "segmentation_file_id), check that every id has an associated" +
                " file entry stored in data_file table. Check that the file " +
                "actually exists on the disk.");
    }

    protected abstract FileType getFileType();

    protected abstract TableName getTableName();

    abstract HashMap<Integer, String> getTableIDs(DatabaseRegistryEntry dbre);

    @Override
    public boolean run(DatabaseRegistryEntry dbre) {
        boolean result = true;
        Connection con = dbre.getConnection();

        FileType fileType = getFileType();
        TableName tableName = getTableName();
        HashMap<Integer, String> tableIDs = getTableIDs(dbre);

        try {
            for (Object o : tableIDs.entrySet()) {
                Map.Entry<Integer, String> pair = (Map.Entry<Integer,
                        String>) o;
                int tableID = pair.getKey();
                String name = pair.getValue();

                //fetch file path for every table_id
                Statement stmt = con.createStatement();
                ResultSet filePath = stmt.executeQuery("SELECT path FROM " +
                        "data_file WHERE table_name='" + tableName.toString() +
                        "' AND file_type='" + fileType.toString() +
                        "' AND table_id=" + tableID);

                if (!filePath.next()) {
                    ReportManager.problem(this, con, "No " +
                            fileType.toString() + " file entry found in " +
                            "data_file table for " + tableName.toString() +
                            " " + name + " with id " + tableID);
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
                    File file = new File(parentFuncgenDir + filePath
                            .getString(1));

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

    enum FileType {
        BAM, BIGWIG, BIGBED
    }

    enum TableName {
        alignment, segmentation_file, external_feature_file
    }
}
