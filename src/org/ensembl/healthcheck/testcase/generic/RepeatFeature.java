/*
 Copyright (C) 2003 EBI, GRL

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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for repeat features that have repeat_start > repeat_end, or start or end < 1. Note that seq_region_start/end are checked in FeatureCoords.
 */

public class RepeatFeature extends SingleDatabaseTestCase {

    /**
     * Create a new RepeatFeature testcase.
     */
    public RepeatFeature() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that repeat_start and repeat_end in repeat_feature make sense.");

    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        
        // check that start < end
        String sql = "SELECT COUNT(*) FROM repeat_feature WHERE repeat_start > repeat_end";
        Connection con = dbre.getConnection();
        int rows = getRowCount(con, sql);
        if (rows == 0) {
            ReportManager.correct(this, con, "All repeat_feature_start < repeat_feature end");
        } else if (rows > 0) {
            ReportManager.problem(this, con, rows + " rows in repeat_feature have repeat_start > repeat_end");
            result = false;
        }

        // check start and end not < 1
        sql = "SELECT COUNT(*) FROM repeat_feature WHERE repeat_start < 1 OR repeat_end < 1";
        rows = getRowCount(con, sql);
        if (rows == 0) {
            ReportManager.correct(this, con, "All repeat_feature repeat_start and repeat_end < 1");
        } else if (rows > 0) {
            ReportManager.problem(this, con, rows + " rows in repeat_feature have repeat_start or repeat_end < 1");
            result = false;
        }
        return result;

    } // run

} // RepeatFeature
