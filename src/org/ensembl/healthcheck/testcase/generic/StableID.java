/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks the *_stable_id tables to ensure they are populated, have no orphan
 * references, and have valid versions. Also prints some examples from the
 * table for checking by eye.
 * 
 * <p>
 * Group is <b>check_stable_ids </b>
 * </p>
 * 
 * <p>
 * To be run after the stable ids have been assigned.
 * </p>
 */
public class StableID extends SingleDatabaseTestCase {

    /**
     * Create a new instance of StableID.
     */
    public StableID() {
        addToGroup("id_mapping");
        addToGroup("release");
        setDescription("Checks *_stable_id tables are valid.");
    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);

    }
    
    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        result &= checkStableIDs(con, "exon");
        result &= checkStableIDs(con, "translation");
        result &= checkStableIDs(con, "transcript");
        result &= checkStableIDs(con, "gene");

        return result;
    }

    /**
     * Checks that the typeName_stable_id table is valid. The table is valid if
     * it has >0 rows, and there are no orphan references between typeName
     * table and typeName_stable_id. Also prints some example data from the
     * typeName_stable_id table via ReportManager.info().
     * 
     * @param con
     *          connection to run quries on.
     * @param typeName
     *          name of the type to check, e.g. "exon"
     * @return true if the table and references are valid, otherwise false.
     */
    public boolean checkStableIDs(Connection con, String typeName) {

        boolean result = true;

        String stableIDtable = typeName + "_stable_id";
        int nStableIDs = countRowsInTable(con, stableIDtable);
        ReportManager.info(this, con, "Num " + typeName + "s stable ids = " + nStableIDs);

        if (nStableIDs < 1) {
            ReportManager.problem(this, con, stableIDtable + " table is empty.");
            result = false;
        }

        // print a few rows so we can check by eye that the table looks ok
        //DBUtils.printRows(this, con, "select * from " + stableIDtable + " limit 10;");

        // look for orphans between type and type_stable_id tables
        int orphans = countOrphans(con, typeName, typeName + "_id", stableIDtable, typeName + "_id", false);
        if (orphans > 0) {
            ReportManager.problem(this, con, "Orphan references between " + typeName + " and " + typeName
                    + "_stable_id tables.");
            result = false;
        }

        int nInvalidVersions = getRowCount(con, "SELECT COUNT(*) AS " + typeName + "_with_invalid_version" + " FROM "
                + stableIDtable + " WHERE version < 1;");

        if (nInvalidVersions > 0) {
            ReportManager.problem(this, con, "Invalid " + typeName + " versions in " + stableIDtable);
            DBUtils.printRows(this, con, "SELECT DISTINCT(version) FROM " + stableIDtable);
            result = false;
        }

        // check for duplicate stable IDs (will be redundant when stable ID columns get a UNIQUE constraint)
        // to find which records are duplicated use
        // SELECT exon_id, stable_id, COUNT(*) FROM exon_stable_id GROUP BY stable_id HAVING COUNT(*) > 1;
        // this will give the internal IDs for *one* of each of the duplicates
        // if there are only a few then reassign the stable IDs of one of the duplicates
        int duplicates = getRowCount(con, "SELECT COUNT(stable_id)-COUNT(DISTINCT stable_id) FROM " + stableIDtable);
        if (duplicates > 0) {
            ReportManager.problem(this, con, stableIDtable + " has " + duplicates + " duplicate stable IDs (versions not checked)");
            result = false;
        } else {
            ReportManager.correct(this, con, "No duplicate stable IDs in " + stableIDtable);
        }
        
        return result;
    }

}
