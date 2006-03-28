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

public class ForeignKeyGenomeDbId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomeDbId that applies to a specific set of databases.
     */
    public ForeignKeyGenomeDbId() {

        addToGroup("compara_db_constraints");
        addToGroup("protein_db_constraints");
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

        Connection con = dbre.getConnection();

        if (tableHasRows(con, "genome_db")) {

            result &= checkForOrphans(con, "dnafrag", "genome_db_id", "genome_db", "genome_db_id");
            result &= checkForOrphansWithConstraint(con, "member", "genome_db_id", "genome_db", "genome_db_id", "genome_db_id != 0");
            result &= checkForOrphans(con, "species_set", "genome_db_id", "genome_db", "genome_db_id");
            result &= checkForOrphans(con, "genome_db", "genome_db_id", "species_set", "genome_db_id");

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genome_db table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomeDbId
