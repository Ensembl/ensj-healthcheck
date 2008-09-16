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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that there are Interpro descriptions, that each one has an xref, and that the xref has a description.
 */

public class InterproDescriptions extends SingleDatabaseTestCase {

    /**
     * Create a new InterproDescriptions testcase.
     */
    public InterproDescriptions() {

        addToGroup("post_genebuild");
        addToGroup("release");
    		addToGroup("core_xrefs");
        setDescription("Check that there are Interpro descriptions, that each one has an xref, and that the xref has a description.");

    }

    /**
     * This only really applies to core & vega databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.CDNA);
        
    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test passed.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        // check that the interpro table has some rows
        String sql = "SELECT COUNT(*) FROM interpro";

        int rows = getRowCount(con, sql);
        if (rows == 0) {

            ReportManager.problem(this, con, "Interpro table is empty (no xref checks done).");
            return false;

        } else {

            ReportManager.correct(this, con, "Interpro table not empty");
        }

        // check that there are no Interpro accessions without xrefs
        sql = "SELECT count(*) FROM interpro i LEFT JOIN xref x ON i.interpro_ac=x.dbprimary_acc WHERE x.dbprimary_acc IS NULL";

        rows = getRowCount(con, sql);
        if (rows > 0) {

            ReportManager.problem(this, con, "There are " + rows + " rows in the interpro table that have no associated xref");
            result = false;

        } else {

            ReportManager.correct(this, con, "All Interpro accessions have xrefs");
        }

        // check that the description field is populated for all of them
        sql = "SELECT COUNT(*) FROM interpro i, xref x WHERE i.interpro_ac=x.dbprimary_acc AND x.description IS NULL";

        rows = getRowCount(con, sql);
        if (rows > 0) {

            ReportManager.problem(this, con, "There are " + rows + " Interpro xrefs with missing descriptions");
            result = false;

        } else {

            ReportManager.correct(this, con, "All Interpro accessions have xref descriptions");
        }
        
        return result;

    } // run
    
} // InterproDescriptions
