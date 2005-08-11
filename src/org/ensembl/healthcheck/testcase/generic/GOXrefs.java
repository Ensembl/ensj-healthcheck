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
 * Check that GO xrefs exist for certain species.
 */

public class GOXrefs extends SingleDatabaseTestCase {

    /**
     * Create a new GOXrefs testcase.
     */
    public GOXrefs() {

        addToGroup("post_genebuild");
        addToGroup("release");
	addToGroup("core_xrefs");
        setDescription("Check that GO xrefs exist for certain species (human, mouse, rat, drosophila)");
	
    }

    /**
     * This only really applies to core databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.VEGA);
        removeAppliesToType(DatabaseType.CDNA);
        
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

	// only check for GO xrefs for human, mouse, rat & drosophila
	//	if (dbre.getSpecies().equals(Species.HOMO_SAPIENS) || dbre.getSpecies().equals(Species.MUS_MUSCULUS) || dbre.getSpecies().equals(Species.RATTUS_NORVEGICUS) || dbre.getSpecies().equals(Species.DROSOPHILA_MELANOGASTER)) {

	if (true) {
	    Connection con = dbre.getConnection();
	    
	    String sql = "SELECT COUNT(*) FROM external_db edb, xref x WHERE edb.db_name= 'go' AND edb.external_db_id = x.external_db_id";
	    
	    int rows = getRowCount(con, sql);
	    if (rows == 0) {
		
		ReportManager.problem(this, con, "No GO xrefs found.");
		result = false;
		
	    } else {
		
		ReportManager.correct(this, con, "Found " + rows + " GO xrefs");
		
	    }
	    
	} else {

	    logger.info("Not checking for GO xrefs in " + dbre.getSpecies());
            return true;
	    
	}	
	
	return result;
	    
    } // run
    
} // GOXrefs
