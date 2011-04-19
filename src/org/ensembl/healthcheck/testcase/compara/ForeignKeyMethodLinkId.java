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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMethodLinkId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkId() {

        addToGroup("compara_genomic");
        addToGroup("compara_homology");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible(Team.COMPARA);

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

        if (!tableHasRows(con, "method_link")) {
            ReportManager.correct(this, con, "NO ENTRIES in method_link table, so nothing to test IGNORED");

        } else if (!tableHasRows(con, "method_link_species_set")) {
            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");

        } else {
            result &= checkForOrphans(con, "method_link_species_set", "method_link_id", "method_link", "method_link_id");
//             result &= checkForOrphans(con, "method_link", "method_link_id", "method_link_species_set", "method_link_id");
        }

        return result;

    }

} // ForeignKeyMethodLinkId
