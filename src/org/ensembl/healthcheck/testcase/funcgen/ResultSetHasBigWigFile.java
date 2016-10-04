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
import org.ensembl.healthcheck.Team;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Check that every result_set which is linked to an annotated feature_set
 * has an associated BIGWIG file entry stored in dbfile_registry table. Check
 * that the file actually exists on the disk.
 * @author ilavidas
 */

public class ResultSetHasBigWigFile extends DBFileRegistryHasFile {

    public ResultSetHasBigWigFile() {
        setTeamResponsible(Team.FUNCGEN);
        setDescription("Check that every result_set which is linked to an " +
                "annotated feature_set has an associated BIGWIG file entry " +
                "stored in dbfile_registry table. Check that the file " +
                "actually exists on the disk.");
    }

    @Override
    protected FileType getFileType() {
        return FileType.BIGWIG;
    }

    @Override
    protected TableName getTableName() {
        return TableName.result_set;
    }

    @Override
    HashMap<Integer, String> getTableIDs(DatabaseRegistryEntry dbre) {
        HashMap<Integer, String> tableIDs = new HashMap<Integer, String>();

        Connection con = dbre.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT result_set_id, " +
                    "result_set.name FROM result_set JOIN supporting_set ON" +
                    "(result_set_id=supporting_set_id AND type='result') JOIN" +
                    " data_set USING(data_set_id) JOIN feature_set USING" +
                    "(feature_set_id) WHERE feature_set.type='annotated'");

            while(rs!=null && rs.next()){
                tableIDs.put(rs.getInt(1),rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableIDs;
    }

}
