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
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the mitochondrial DNA, if present is marked as toplevel in seq_region_attrib.
 */

public class MTDNATopLevel extends SingleDatabaseTestCase {


    /**
     * Create a new MTDNATopLevel testcase.
     */
    public MTDNATopLevel() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that the mitochondrial DNA, if present is marked as toplevel in seq_region_attrib");

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

	// check for presence of MT 
	if (getRowCount(con, "SELECT COUNT(*) FROM seq_region WHERE NAME='MT'") > 0) {

	    String sql = "SELECT COUNT(*) FROM seq_region sr, seq_region_attrib sra, attrib_type at WHERE sr.name='Mt' AND sra.seq_region_id=sr.seq_region_id AND at.attrib_type_id=sra.attrib_type_id AND at.code='toplevel'";

	    int rows = getRowCount(con, sql);
            if (rows > 0) {

                ReportManager.correct(this, con, "MT DNA marked as toplevel in seq_region_attrib");

            } else if (rows == 0) {

                ReportManager.problem(this, con, "MT DNA not toplevel in seq_region_attrib");
                result = false;

            }

	} else {

	    ReportManager.info(this, con, "No MT in " + DBUtils.getShortDatabaseName(con) + ", skipping.");

	}

        return result;

    } // run

} // MTDNATopLevel
