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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class SpeciesNameConsistency extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public SpeciesNameConsistency() {

        addToGroup("compara_db_constraints");
        setDescription("Check for species name inconsistancies in ensembl_compara databases.");

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

        // check genome_db table has > 0 rows
        if (countRowsInTable(con, "genome_db") == 0) {
            result = false;
            ReportManager.problem(this, con, "genome_db table is empty");
        } else {
            ReportManager.correct(this, con, "genome_db table has data");
        }

        // check taxon table has > 0 rows
        if (countRowsInTable(con, "taxon") == 0) {
            result = false;
            ReportManager.problem(this, con, "taxon table is empty");
        } else {
            ReportManager.correct(this, con, "taxon table has data");
        }

        String sql = "select gdb.taxon_id from taxon tx, genome_db gdb where tx.taxon_id=gdb.taxon_id and gdb.name != concat(tx.genus,\" \",tx.species)";

        String[] taxonIDs = getColumnValues(con, sql);

        if (taxonIDs.length > 0) {
            result = false;
            ReportManager.problem(this, con,
                    "Cases where genome_db and taxon table do not shared the same species names");
            for (int i = 0; i < taxonIDs.length; i++) {
                ReportManager.problem(this, con, " taxon_id " + taxonIDs[i]
                        + " have different species name between genome_db and taxon tables");
            }
        } else {
            ReportManager.correct(this, con, "genome_db and taxon table share the same species names");
        }

        return result;

    }

} // SpeciesNameConsistency
