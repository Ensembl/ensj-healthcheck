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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;


/**
 * Checks the meta_coord table to make sure it is OK. Only one meta table at a time is done here; checks for the consistency of the meta_coord table across
 * species are done in MetaCrossSpecies.
 */
public class Meta_coord extends SingleDatabaseTestCase {

    /**
   * Creates a new instance of CheckMetaDataTableTestCase
   */
	public Meta_coord() {

		addToGroup("variation");
		addToGroup("release");
		setDescription("Check that the meta_coord table contains the right entries for the different variation species");
	}

    /**
     * Check various aspects of the meta_coord table.
     * 
     * @param dbre The database to check.
     * @return True if the test passed.
     */
	public boolean run(final DatabaseRegistryEntry dbre) {
	    boolean result = true;

	    Connection con = dbre.getConnection();
	    String[] tables = {"variation_feature","flanking_sequence","compressed_genotype_single_bp","transcript_variation","read_coverage","variation_group_feature"};
	    /* Will check the presence of the variation_feature, transcript_variation, compressed_genotype,
	       flanking_sequence, read_coverage and variation_group_feature entries in the meta_coord,
	       when data present in those tables
	     */
	    for (int i=0; i < tables.length; i++){
		int rows = getRowCount(con, "SELECT COUNT(*) FROM " + tables[i]); //count if table has data
		if (rows > 0){
		    // the meta_coord table should contain entry
		    result &= checkKeysPresent(con,tables[i]);
		}
	    }
	    if (! result ){
	    //if there were no problems, just inform for the interface to pick the HC
		ReportManager.info(this,con,"MetaCoord table healthcheck passed without any problem");
	    }
	    return result;
	} //run 

     // --------------------------------------------------------------

	private boolean checkKeysPresent(Connection con, String tableName) {

		boolean result = true;

		int rows = getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE table_name='" + tableName + "'");
		if (rows == 0) {
		    result = false;
		    ReportManager.problem(this, con, "No entry in meta_coord table for " + tableName);
		} else {
		    ReportManager.correct(this, con, tableName + " entry present");
		}

		return result;
	}

    // --------------------------------------
}
