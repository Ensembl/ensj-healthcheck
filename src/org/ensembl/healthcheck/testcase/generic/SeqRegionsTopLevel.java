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
 * Check that all seq_regions comprising genes are marked as toplevel in seq_region_attrib.
 */

public class SeqRegionsTopLevel extends SingleDatabaseTestCase {


    /**
     * Create a new SeqRegionsTopLevel testcase.
     */
    public SeqRegionsTopLevel() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that all seq_regions comprising genes are marked as toplevel in seq_region_attrib");

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
	
	// can't do this all in one SQL statement, so get all gene seq_regions
	// and check each one in turn
	// pre-fetch correct attrib_type_id to save a join
	String val = getRowColumnValue(con, "SELECT attrib_type_id FROM attrib_type WHERE code=\'toplevel\'");
	if (val == null || val.equals("")) {
	    ReportManager.problem(this, con, "Can't find a seq_region attrib_type with code 'toplevel', exiting");
	    return false;
	}
	int topLevelAttribType = Integer.parseInt(val);
    
	logger.info("attrib_type_id for toplevel: " + topLevelAttribType);

	// now loop over each gene seq_region
	String[] geneSeqRegionIDs = getColumnValues(con, "SELECT DISTINCT seq_region_id FROM gene");

	for (int i = 0; i < geneSeqRegionIDs.length; i++) {

	    int numTopLevel = getRowCount(con, "SELECT COUNT(*) FROM seq_region_attrib WHERE attrib_type_id = " + topLevelAttribType + " AND seq_region_id=" + geneSeqRegionIDs[i]);
	    if (numTopLevel == 0) {

		ReportManager.problem(this, con, "No top_level attribute set for seq_region_id " + geneSeqRegionIDs[i]);
		result = false;

	    } 

	}

	if (result == true) {
	    ReportManager.correct(this, con, "All " + geneSeqRegionIDs.length + " gene seq_regions have top_level attribute set");
	}
	
        return result;

    } // run

} // SeqRegionsTopLevel
