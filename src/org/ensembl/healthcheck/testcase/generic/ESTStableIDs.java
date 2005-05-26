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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the gene_stable_id table is populated in EST databases. 
 */

public class ESTStableIDs extends SingleDatabaseTestCase {


    /**
     * Create a new ESTStableIDs testcase.
     */
    public ESTStableIDs() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check the gene_stable_id table is populated in EST databases.");

    }

    /**
     * This only applies to EST databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.CORE);
        removeAppliesToType(DatabaseType.VEGA);

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
	
	int rows = getRowCount(con, "SELECT COUNT(*) FROM gene_stable_id");

	if (rows == 0) {
	    ReportManager.problem(this, con, "gene_stable_id is empty");
	    return false;
	} else {
	    ReportManager.correct(this, con, "gene_stable_id has " + rows + " rows");
	}

        return result;

    } // run

} // ESTStableIDs
