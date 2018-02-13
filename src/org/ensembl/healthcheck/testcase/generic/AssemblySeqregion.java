/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the assembly table and seq_region table are consistent.
 */
public class AssemblySeqregion extends SingleDatabaseTestCase {

    /**
     * Create a new AssemlySeqregion test case.
     */
    public AssemblySeqregion() {

        setDescription("Check that the chromosome lengths from the seq_region table "
            + "agree with both the assembly table and the karyotype table.");
        setTeamResponsible(Team.GENEBUILD);
    }

    /**
     * Data is only tested in core database, as the tables are in sync
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.RNASEQ);
        removeAppliesToType(DatabaseType.CDNA);

    }

    /**
     * @param dbre
     *            The database to use.
     * @return The test case result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        // Check that the assembly table is populated
        int cs = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM coord_system");
        if (cs > 1) {
            int rows = DBUtils
                    .getRowCount(con, "SELECT COUNT(*) FROM assembly");
            if (rows == 0) {
                ReportManager.problem(this, con, rows
                        + " rows found in assembly table");
            } else {
                ReportManager.correct(this, con, "Assembly table is populated");

                // -------------------------------------------------------
                // check various other things about the assembly table
                // Check for mismatched lengths of assembled and component
                // sides.
                // ie where (asm_end - asm_start + 1) != (cmp_end - cmp_start +
                // 1)
                rows = DBUtils.getRowCount(con,
                    "SELECT COUNT(*) FROM assembly "
                    + "WHERE (asm_end - asm_start + 1) != (cmp_end - cmp_start + 1)");
                if (rows > 0) {
                    ReportManager.problem(this, con, rows
                        + " rows in assembly table have mismatched lengths of "
                        + "assembled and component sides");
                } else {
                    ReportManager.correct(this, con,
                        "All rows in assembly table have matching lengths of "
                        + "assembled and component sides");
                }

                // check for start/end < 1
                rows = DBUtils.getRowCount(con,
                    "SELECT COUNT(*) FROM assembly "
                    + "WHERE asm_start < 1 OR asm_end < 1 OR cmp_start < 1 OR cmp_end < 1");
                if (rows > 0) {
                    ReportManager.problem(this, con,
                        rows + " rows in assembly table have start or end coords < 1");
                } else {
                    ReportManager.correct(this, con,
                        "All rows in assembly table have start and end coords > 0");
                }

                // check for end < start
                rows = DBUtils.getRowCount(con,
                    "SELECT COUNT(*) FROM assembly "
                    + "WHERE asm_end < asm_start OR cmp_end < cmp_start");
                if (rows > 0) {
                    ReportManager.problem(this, con, rows
                        + " rows in assembly table have start or end coords < 1");
                } else {
                    ReportManager.correct(this, con,
                        "All rows in assembly table have end coords > start coords");
                }
            }
        }

        // ---------------------------------------------------
        // Find any seq_regions that have different lengths in seq_region &
        // assembly.
        // NB seq_region length should always be equal to (or possibly greater
        // than) the maximum assembly length. The SQL returns failures
        // ----------------------------------------------------
        String sql = "SELECT sr.name AS name, sr.length, cs.name AS coord_system "
            + "FROM seq_region sr, assembly ass, coord_system cs "
            + "WHERE sr.coord_system_id = cs.coord_system_id "
            + "AND ass.asm_seq_region_id = sr.seq_region_id "
            + "GROUP BY ass.asm_seq_region_id "
            + "HAVING sr.length < MAX(ass.asm_end)";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int i = 0;

            // Report only the first 50 seq_regions that are shorter
            while (rs.next() && i++ < 50) {
                result = false;
                String cos = rs.getString("coord_system");
                String sr = rs.getString("name");
                ReportManager.problem(this, con, cos + " " + sr
                    + " is shorter in seq_region than in assembly");
            }
            if (i == 0) {
                ReportManager.correct(this, con,
                    "Sequence region lengths are equal or greater "
                    + "in the seq_region table compared to the assembly table");
            }
        } catch (SQLException e) {
            System.err.println("Error executing " + sql + ":");
            e.printStackTrace();
        }
        return result;

    } // run

} // ChromosomeLengths
