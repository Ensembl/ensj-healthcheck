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

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMemberId extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public ForeignKeyMemberId() {

        addToGroup("compara_db_constraints");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");

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

        int orphans = 0;

        Connection con = dbre.getConnection();
        // 1 test to check member_id used as foreign key

        if (tableHasRows(con, "member")) {

            orphans = countOrphans(con, "family_member", "member_id", "member", "member_id", false);
            if (orphans == 0) {
                ReportManager.correct(this, con, "PASSED family_member <-> member relationships");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "FAILED family_member <-> member relationships: " + orphans + " family_member has unlinked entries in member");
            } else {
                ReportManager.problem(this, con,
                        "TEST NOT COMPLETED family_member <-> member, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "homology_member", "member_id", "member", "member_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con, "homology_member <-> member relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "homology_member has unlinked entries in member FAILED");
            } else {
                ReportManager.problem(this, con,
                        "homology_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "homology_member", "peptide_member_id", "member", "member_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con,
                        "homology_member <-> member via peptide_member_id relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con,
                        "homology_member has unlinked entries via peptide_member_id in member FAILED");
            } else {
                ReportManager.problem(this, con,
                        "homology_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
            }

            orphans = countOrphans(con, "domain_member", "member_id", "member", "member_id", true);
            if (orphans == 0) {
                ReportManager.correct(this, con, "domain_member <-> member relationships PASSED");
            } else if (orphans > 0) {
                ReportManager.problem(this, con, "domain_member has unlinked entries in member FAILED");
            } else {
                ReportManager.problem(this, con,
                        "domain_member <-> member TEST NOT COMPLETED, look at the StackTrace if any");
            }

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in member table, so nothing to test IGNORED");
        }

        result &= (orphans == 0);

        return result;

    }

} // OrphanTestCase
