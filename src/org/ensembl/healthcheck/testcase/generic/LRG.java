/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for mistakes relating to LRGs
 */
public class LRG extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of LRG healthcheck
     */
    public LRG() {
        addToGroup("release");
        addToGroup("lrg");
        setDescription("Healthcheck for LRGs");
    }

    /**
     * Check that all seq_regions on the lrg coordinate system have gene and transcripts associated with them
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {
        boolean result = true;
    
        Connection con = dbre.getConnection();
	
	// Get all seq_region_ids on the lrg coordinate system
	String stmt = new String("SELECT sr.seq_region_id FROM seq_region sr JOIN coord_system cs ON sr.coord_system_id = cs.coord_system_id WHERE cs.name LIKE 'lrg' ORDER BY sr.seq_region_id ASC");
	String[] seq_regions = getColumnValues(con,stmt);
	String idList = new String();
	for (int i=0; i<seq_regions.length; i++) {
		idList += seq_regions[i] + ",";
	}
	idList = idList.substring(0,idList.length()-1);
	
	// Check that gene annotations exist	
	stmt = new String("SELECT g.seq_region_id, COUNT(*) FROM gene g WHERE g.seq_region_id IN (" + idList + ") GROUP BY g.seq_region_id");
	int count = (seq_regions.length - getRowCount(con,stmt));
	if (count != 0) {
		ReportManager.problem(this, con, String.valueOf(count) + " LRG seq_regions do not have any gene annotations");
		result = false;
	}
	
	// Check that transcript annotations exist
	stmt = new String("SELECT t.seq_region_id, COUNT(*) FROM transcript t WHERE t.seq_region_id IN (" + idList + ") GROUP BY t.seq_region_id");
	count = (seq_regions.length - getRowCount(con,stmt));
	if (count != 0) {
		ReportManager.problem(this, con, String.valueOf(count) + " LRG seq_regions do not have any transcript annotations");
		result = false;
	}
	
	if ( result ){
	    ReportManager.correct(this,con,"LRG healthcheck passed without any problem");
	}
        return result;

    } // run

} // LRG
