/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that some display_xrefs are set.
 */

public class DisplayXref extends SingleDatabaseTestCase {

    /**
     * Create a new DisplayXref testcase.
     */
    public DisplayXref() {

        addToGroup("post_genebuild");
        addToGroup("release");
	addToGroup("xrefs");
        setDescription("Check that display_xrefs are set OK");

    }

    /**
     * This only applies to core and Vega databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);

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

        Connection con = dbre.getConnection();

        // check that some (not necessarily all) genes and transcripts have valid display_xref_ids
        // TODO - is checking that there is at least 1 OK or should there be a minimum of say 100?
        String[] types = {"gene", "transcript"};
        for (int i = 0; i < types.length; i++) {

            if (getRowCount(con, "SELECT COUNT(*) FROM " + types[i] + " WHERE display_xref_id IS NOT NULL AND display_xref_id > 0") == 0) {

                ReportManager.problem(this, con, "No " + types[i] + "s with valid display_xref_id");
                result = false;

            }
        }

        if (result) {
        
            ReportManager.correct(this, con, "All display_xrefs OK");
            
        }
        
        return result;

    } // run

} // DisplayXref
