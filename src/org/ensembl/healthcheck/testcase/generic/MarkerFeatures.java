/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that marker features exist if markers exist, and that map_wieghts are set to non-zero
 * values
 */
public class MarkerFeatures extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of CheckMarkerFeatures
     */
    public MarkerFeatures() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Checks that marker_features exist and that they have" + " non-zero map_weights");
    }

    /**
     * Verify marker features exist if markers exist, and that map weights are non-zero.
     * 
     * @param dbre The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
        boolean markersExist = getRowCount(con, "select count(*) from marker") > 0;

        /*
         * assume this species has no markers, dangling refs test case will catch problem if
         * marker_features exist without markers
         */
        if (!markersExist) {
            return true;
        }

        int rowCount = getRowCount(con, "SELECT COUNT(*) FROM marker_feature");

        if (rowCount == 0) {
            ReportManager.problem(this, con, "No marker features in database even though markers are present");
            result = false;
        }

        int badWeightCount = getRowCount(con, "SELECT marker_id, COUNT(*) AS correct, map_weight FROM marker_feature GROUP BY marker_id HAVING map_weight != correct");

        if (badWeightCount > 0) {
            ReportManager.problem(this, con, badWeightCount + " marker features have not been assigned correct map weights");
            result = false;
        }

        if (result) {
            ReportManager.correct(this, con, "Marker features appear to be ok");
        }

        return result;

    } // run

} // MarkerFeatures
