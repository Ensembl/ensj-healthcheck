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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the seq_region names are in the right format.
 */

public class SeqRegionName extends SingleDatabaseTestCase {

    /**
     * Create a new SeqRegionName testcase.
     */
    public SeqRegionName() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that seq_region names are in the right format.");

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

	result &= seqRegionNameCheck(con, "clone",  "^[a-zA-Z]+[0-9]+\\.[0-9]+$");
	result &= seqRegionNameCheck(con, "contig", "^[a-zA-Z]+[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$");

        return result;

    } // run


    // ----------------------------------------------------------------------
    /** 
     * Check that seq regions of a particualar coordinate system are named 
     * appropriately.
     * @return True if all seq_region names match the regexp.
     */

    private boolean seqRegionNameCheck(Connection con, String coordinateSystem, String regexp) {

	boolean result = true;

	int rows = getRowCount(con, "SELECT COUNT(*) FROM seq_region sr, coord_system cs WHERE sr.coord_system_id=cs.coord_system_id AND cs.name='" + coordinateSystem + "' AND sr.name NOT REGEXP '" + regexp + "'");

	if (rows > 0) {

	    ReportManager.problem(this, con, rows + " seq_regions in coordinate system " + coordinateSystem + " have names that are not of the correct format");
	    result = false;

	} else {

	    ReportManager.correct(this, con, "All seq_regions in coordinate system " + coordinateSystem + " have names in the correct format");
	    result = true;

	}

	return result;

    }

    // ----------------------------------------------------------------------

} // SeqRegionName
