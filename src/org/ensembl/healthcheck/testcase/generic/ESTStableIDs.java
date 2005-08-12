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
 * Check that the all genes have stable IDs in EST databases. 
 */

public class ESTStableIDs extends SingleDatabaseTestCase {


    /**
     * Create a new ESTStableIDs testcase.
     */
    public ESTStableIDs() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that the all genes have stable IDs in EST databases");

    }

    /**
     * This only applies to EST databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.CORE);
        removeAppliesToType(DatabaseType.VEGA);
        removeAppliesToType(DatabaseType.CDNA);
        
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
	
	int rows = getRowCount(con, "SELECT COUNT(*) FROM gene g LEFT JOIN gene_stable_id gs ON g.gene_id = gs.gene_id WHERE gs.gene_id IS NULL HAVING COUNT(g.gene_id) > 0");

	if (rows > 0) {
	    ReportManager.problem(this, con, rows + " genes have no stable IDs");
	    return false;
	} else {
	    ReportManager.correct(this, con, "All genes have stable IDs");
	}

        return result;

    } // run

} // ESTStableIDs
