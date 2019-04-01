/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;

import java.sql.Connection;

/**
 * Subclass of EnsTestCase for tests that apply to a <em>single</em> database. Such tests should
 * subclass <em>this</em> class and implement the <code>run</code> method.
 */
public abstract class SingleDatabaseTestCase extends EnsTestCase {

    /**
     * This method should be overridden by subclasses.
     * 
     * @param dbre
     *          The database to run on.
     * @return True if the test passed.
     */

    public abstract boolean run(DatabaseRegistryEntry dbre);

    // -------------------------------------------------------------------------
    /**
     * Check that the count in a table for a given constraint is 0.
     * 
     * @param con
     *          The database connection to use.
     * @param table
     *          The name of the table to examine.
     * @param constraint
     *          constraint
     * @return boolean
     *          true if everything is fine
     *          false otherwise
     */
    public boolean checkCountIsZero(Connection con, String table, String constraint) {

        int count = 0;
        boolean result = true;

        // cheat by looking for any rows that DO NOT match the pattern
        String useful_sql = "SELECT COUNT(*) FROM " + table;
        
        if (! constraint.equals("")) {
            useful_sql = useful_sql + " WHERE " + constraint;
        }

        logger.fine(useful_sql);
        count = DBUtils.getRowCount(con, useful_sql);

        if (count == 0) {
            ReportManager.correct(this, con, "PASSED ");
        } else if (count > 0) {
            ReportManager.problem(this, con, "FAILED ");
            ReportManager.problem(this, con, "FAILURE DETAILS: " + count + " ENTRIES found in " + table  + " matching '" + constraint + "' instead of 0");
            ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
            result = false;
        } else {
            ReportManager.problem(this, con, "TEST NOT COMPLETED, look at the StackTrace if any");
            result = false;
        }

        return result;

    } // checkCountIsZero

    /**
     * Check that the count in a table for a given constraint is higher
	 * than 0.
     *
     * @param con
     *          The database connection to use.
     * @param table
     *          The name of the table to examine.
     * @param constraint
     *          constraint
     * @return boolean
     *          true if everything is fine
     *          false otherwise
     */
    public boolean checkCountIsNonZero(Connection con, String table, String constraint) {

        int count = 0;
        boolean result = true;

        // cheat by looking for any rows that DO NOT match the pattern
        String useful_sql = "SELECT COUNT(*) FROM " + table;

        if (! constraint.equals("")) {
            useful_sql = useful_sql + " WHERE " + constraint;
        }

        logger.fine(useful_sql);
        count = DBUtils.getRowCount(con, useful_sql);

        if (count > 0) {
            ReportManager.correct(this, con, "PASSED ");
        } else if (count == 0) {
            ReportManager.problem(this, con, "FAILED ");
            ReportManager.problem(this, con, "FAILURE DETAILS: NO ENTRIES found in " + table  + " matching '" + constraint + "'");
            ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
            result = false;
        } else {
            ReportManager.problem(this, con, "TEST NOT COMPLETED, look at the StackTrace if any");
            result = false;
        }

        return result;

    } // checkCountIsNonZero




}
