/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.*;
import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check that the chromosome lengths stored in various places are consistent.
 */
public class ChromosomeLengths extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of CheckChromosomeLengthsTestCase
     */
    public ChromosomeLengths() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that the chromosome lengths from the seq_region table agree with both the assembly table and the karyotype table.");
    }

    /**
     * @return The test case result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        AssemblyNameInfo assembly = new AssemblyNameInfo(con);
        String defaultAssembly = assembly.getMetaTableAssemblyDefault();
        logger.finest("assembly.default from meta table: " + defaultAssembly);

        // ---------------------------------------------------
        // Find any seq_regions that have different lengths in seq_region & assembly, for the
        // default assembly.
        // NB seq_region length should always be equal to (or possibly greater than) the maximum
        // assembly length
        // The SQL returns failures
        // ----------------------------------------------------
        String sql = "SELECT sr.name AS name, sr.length, cs.name AS coord_system " + "FROM seq_region sr, assembly ass, coord_system cs "
                + "WHERE sr.coord_system_id=cs.coord_system_id " + "AND ass.asm_seq_region_id = sr.seq_region_id "
                + "GROUP BY ass.asm_seq_region_id " + "HAVING sr.length < MAX(ass.asm_end)";

        try {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int i = 0;
            if (rs.next() && i++ < 50) {
                result = false;
                String cs = rs.getString("coord_system");
                String sr = rs.getString("name");
                ReportManager.problem(this, con, cs + " " + sr + " is shorter in seq_region than in assembly");

            } else {
                ReportManager.correct(this, con,
                        "Sequence region lengths are equal or greater in the seq_region table compared to the assembly table");
            }
        } catch (SQLException e) {
            System.err.println("Error executing " + sql + ":");
            e.printStackTrace();
        }

        return result;

    } // run

} // ChromosomeLengths
