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

public class MultipleGenomicAlignBlockIds extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomicAlignBlockId that applies to a specific set of databases.
     */
    public MultipleGenomicAlignBlockIds() {

        setDescription("Check that every genomic_align_block_id is linked to more than one single genomic_align_id.");
        setTeamResponsible("compara");

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        if (tableHasRows(con, "genomic_align")) {

            result &= checkForSingles(con, "genomic_align", "genomic_align_block_id");
 
        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genomic_align table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomicAlignBlockId
