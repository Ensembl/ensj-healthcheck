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
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
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
     * Verify foreign-key relations, and fills ReportManager with useful sql
     * if necessary.
     * 
     * @param con
     *          A connection to the database to be tested. Should already be
     *          open.
     * @param table1
     *          With col1, specifies the first key to check.
     * @param col1
     *          Column in table1 to check.
     * @param table2
     *          With col2, specifies the second key to check.
     * @param col2
     *          Column in table2 to check.
     * @return boolean
     *          true if everything is fine
     *          false otherwise
     */
    public boolean checkForOrphans(Connection con, String table1, String col1, String table2, String col2) {
        
        int orphans = 0;
        boolean result = true;

        orphans = countOrphans(con, table1, col1, table2, col2, true);

        String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2 + " iS NULL";

        if (orphans == 0) {
            ReportManager.correct(this, con, "PASSED " + table1 + " -> " + table2 + " using FK " + col1 + "("+col2+")" + " relationships");
        } else if (orphans > 0) {
            ReportManager.problem(this, con, "FAILED " + table1 + " -> " + table2 + " using FK " + col1 + "("+col2+")" + " relationships");
            ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table1 + " entries are not linked to " + table2);
            ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
            result = false;
        } else {
            ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1 + " -> " + table2 + " using FK " + col1 + ", look at the StackTrace if any");
            result = false;
        }
        
        return result;
    } //checkForOrphans

    // -------------------------------------------------------------------------
    /**
     * Verify foreign-key relations, and fills ReportManager with useful sql
     * if necessary.
     *
     * @param con
     *          A connection to the database to be tested. Should already be
     *          open.
     * @param table1
     *          With col1, specifies the first key to check.
     * @param col1
     *          Column in table1 to check.
     * @param table2
     *          With col2, specifies the second key to check.
     * @param col2
     *          Column in table2 to check.
     * @param constraint1
     *          additional constraint on a column in table1
     * @return boolean
     *          true if everything is fine
     *          false otherwise
     */
    public boolean checkForOrphansWithConstraint(Connection con, String table1, String col1, String table2, String col2, String constraint1) {

        int orphans = 0;
        boolean result = true;

        orphans = countOrphansWithConstraint(con, table1, col1, table2, col2, constraint1);

        String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2 + " iS NULL";

        if (! constraint1.equals("")) {
            useful_sql = useful_sql+ " AND " + table1 + "." + constraint1;
        }

        if (orphans == 0) {
            ReportManager.correct(this, con, "PASSED " + table1 + " -> " + table2 + " using FK " + col1 + "("+col2+")" + " relationships");
        } else if (orphans > 0) {
            ReportManager.problem(this, con, "FAILED " + table1 + " -> " + table2 + " using FK " + col1 + "("+col2+")" + " relationships");
            ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table1 + " entries are not linked to " + table2);
            ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
            result = false;
        } else {
            ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1 + " -> " + table2 + " using FK " + col1 + ", look at the StackTrace if any");
            result = false;
        }
        
        return result;
    } //checkForOrphansWithConstraint

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
        count = getRowCount(con, useful_sql);

        if (count == 0) {
            ReportManager.correct(this, con, "PASSED ");
        } else if (count > 0) {
            ReportManager.problem(this, con, "FAILED ");
            ReportManager.problem(this, con, "FAILURE DETAILS: " + count + " ENTRIES have " + constraint + " and should not");
            ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
            result = false;
        } else {
            ReportManager.problem(this, con, "TEST NOT COMPLETED, look at the StackTrace if any");
            result = false;
        }

        return result;

    } // checkColumnPattern

}
