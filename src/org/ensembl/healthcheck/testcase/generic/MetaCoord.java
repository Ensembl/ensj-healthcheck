/*
 Copyright (C) 2004 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that meta_coord table contains entries for all the coordinate systems that all the
 * features are stored in.
 */
public class MetaCoord extends SingleDatabaseTestCase {

    String[] featureTables = { "gene", "exon", "dna_align_feature", "protein_align_feature", "repeat_feature",
            "prediction_transcript", "prediction_exon", "simple_feature", "marker_feature", "misc_feature", "qtl_feature",
            "karyotype", "transcript", "density_feature"};

    public MetaCoord() {

        addToGroup("release");
        addToGroup("post_genebuild");
        setDescription("Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in");

    }

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        // coordSystems is a hash of lists of coordinate systems that each feature table contains
        Map coordSystems = new HashMap();

        try {

            Statement stmt = con.createStatement();

            // build up a list of all the coordinate systems that are in the various feature tables
            for (int tableIndex = 0; tableIndex < featureTables.length; tableIndex++) {

                String tableName = featureTables[tableIndex];
                // note straight join used for performance reasons here
                String sql = "SELECT STRAIGHT_JOIN DISTINCT(sr.coord_system_id) FROM seq_region sr, " + tableName
                        + " f WHERE sr.seq_region_id = f.seq_region_id";

                logger.finest("Getting feature coordinate systems for " + tableName);
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String coordSystemID = rs.getString(1);
                    logger.finest("Added feature coordinate system for " + tableName + ": " + coordSystemID);
                    // check that the meta_coord table has an entry corresponding to this
                    int mc = getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE coord_system_id=" + coordSystemID
                            + " AND table_name='" + tableName + "'");
                    if (mc == 0) {
                        ReportManager.problem(this, con, "No entry for coordinate system with ID " + coordSystemID + " for "
                                + tableName + " in meta_coord");
                        result = false;
                    } else if (mc > 1) {
                        ReportManager.problem(this, con, "Coordinate system with ID " + coordSystemID + " duplicated for "
                                + tableName + " in meta_coord");
                        result = false;
                    } else {
                        ReportManager.correct(this, con, "Coordinate system with ID " + coordSystemID + " for table " + tableName
                                + " has an entry in meta_coord");
                    }
                    
                    // store in coordSystems map - create List if necessary
                    List csList = (ArrayList) coordSystems.get(tableName);
                    if (csList == null) {
                        csList = new ArrayList();
                    }
                    csList.add(coordSystemID);
                    coordSystems.put(tableName, csList);
                }

                rs.close();

            }

            // check that every meta_coord table entry refers to a coordinate system
            // that is used in a feature
            // if this isn't true it's not fatal but should be flagged
            String sql = "SELECT * FROM meta_coord";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                String csID = rs.getString("coord_system_id");
                logger.finest("Checking for coord_system_id " + csID + " in " + tableName);
                List featureCSs = (ArrayList) coordSystems.get(tableName);
                if (featureCSs != null && !featureCSs.contains(csID)) {
                    ReportManager.problem(this, con, "meta_coord has entry for coord_system ID " + csID + " in " + tableName
                            + " but this coordinate system is not actually used in " + tableName);
                    result = false;
                }

            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;

    }

}
