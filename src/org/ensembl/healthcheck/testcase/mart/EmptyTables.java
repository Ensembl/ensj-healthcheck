/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * 
 * $Log$ Revision 1.1.2.1 2004/03/01 09:42:08 gp1 Moved
 * into mart subdirectory. Some tests renamed
 * 
 * Revision 1.2.2.1 2004/02/23 14:26:57 gp1 No longer depends on SchemaInfo etc
 * 
 * Revision 1.2 2004/01/12 11:19:50 gp1 Updated relevant dates (Copyright
 * notices etc) to 2004.
 * 
 * Revision 1.1 2003/11/04 12:09:52 dkeefe checks that all tables which should
 * contain data do contain data
 * 
 *  
 */

package org.ensembl.healthcheck.testcase.mart;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all tables which should be filled have data.
 */
public class EmptyTables extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of EmptyTablesTestCase
     */
    public EmptyTables() {

        addToGroup("post_ensmartbuild");
        setDescription("Checks that all tables which must be filled, have data");

    }

    // a small number of tables are allowed to be empty so mustBeFilled is
    // false
    private boolean mustBeFilled(final String table) {

        //if (table.equals("")){return false;}
        if (table.equals("hsapiens_expression_gnf_pathology_support")) {
            return false;
        }
        if (table.equals("hsapiens_expression_gnf_preparation_support")) {
            return false;
        }
        return true;

    } // mustBeFilled

    /**
     * For each schema, check that every table has more than 0 rows.
     * @param dbre The database to check.
     * @return True if the test passed.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        String[] tables = getTableNames(con);
        for (int i = 0; i < tables.length; i++) {

            String table = tables[i];
            logger.finest("Checking that " + table + " has rows");

            if (!tableHasRows(con, table) && mustBeFilled(table)) {

                ReportManager.problem(this, con, table + " has zero rows");
                result = false;

            }
        }

        return result;

    } // run

} // EmptyTables
