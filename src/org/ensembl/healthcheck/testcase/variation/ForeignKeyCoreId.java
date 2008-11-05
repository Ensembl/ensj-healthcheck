/*
 Copyright (C) 2004 EBI, GRL
 
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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.ReportManager;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships between core and variation database.
 */

public class ForeignKeyCoreId extends MultiDatabaseTestCase {

     /**
     * Create an ForeignKeyCoreId that applies to a specific set of databases.
     */
    public ForeignKeyCoreId() {

        addToGroup("variation");
	addToGroup("variation-release");
        setDescription("Check for broken foreign-key relationships between variation and core databases.");
	setHintLongRunning(true);

    }

    /**
     * Run the test.
     * 
     * @param databases
     *          The databases to check, in order core->variation
     * @return true if same transcripts and seq_regions in core and variation are the same.
     *  
     */
    public boolean run(DatabaseRegistry dbr) {


        boolean result = true;

	DatabaseRegistryEntry[] variationDBs = dbr.getAll(DatabaseType.VARIATION);
	
	for (int i = 0; i < variationDBs.length; i++){

	    DatabaseRegistryEntry dbrvar = variationDBs[i];
	    Species species = dbrvar.getSpecies();

	    String variationName = dbrvar.getName();
	    String coreName = variationName.replaceAll("variation","core");
	    DatabaseRegistryEntry dbrcore = new DatabaseRegistryEntry(coreName,species,DatabaseType.CORE,true);
	    if (dbrcore == null){
		result = false;
		logger.severe("Incorrect core database " + coreName + " for " + variationName);
		return result;
	    }

	    Connection con = dbrvar.getConnection();
	    
	    System.out.println("Using " + dbrcore.getName() + " as core database and " + dbrvar.getName() + " as variation database");
	    
	    result &= checkForOrphans(con, dbrvar.getName() + ".transcript_variation", "transcript_id", dbrcore.getName() + ".transcript", "transcript_id");
	    
	    result &= checkForOrphansWithConstraint(con, dbrvar.getName() + ".flanking_sequence", "seq_region_id", dbrcore.getName() + ".seq_region", "seq_region_id","seq_region_id IS NOT NULL");
	    
	    result &= checkForOrphansWithConstraint(con, dbrvar.getName() + ".variation_feature", "seq_region_id", dbrcore.getName() + ".seq_region", "seq_region_id","seq_region_id IS NOT NULL");

	    int rows = getRowCount(con,"SELECT COUNT(*) FROM " + dbrvar.getName() + ".seq_region srv ," + dbrcore.getName() + ".seq_region src," + dbrcore.getName() + ".coord_system cs WHERE cs.attrib = 'default_version' AND cs.coord_system_id = src.coord_system_id AND src.name=srv.name AND src.seq_region_id != srv.seq_region_id");
	    if (rows > 0) {
		ReportManager.problem(this, con, rows + " rows seq_region in core has same name, but different seq_region_id comparing with seq_region in variation database");
		result =  false;
	    }

	    if (! result ){
	    //if there were no problems, just inform for the interface to pick the HC
	    ReportManager.correct(this,con,"ForeignKeyCoreId test passed without any problem");
	}
	}
        return result;

    }

   /**
     * This only applies to variation databases.
     */
     public void types() {

	 removeAppliesToType(DatabaseType.OTHERFEATURES);
	 removeAppliesToType(DatabaseType.CDNA);
	 removeAppliesToType(DatabaseType.CORE);
	 removeAppliesToType(DatabaseType.VEGA);

     }

} // ForeignKeyCoreId
