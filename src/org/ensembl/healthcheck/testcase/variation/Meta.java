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
 * Checks the metadata table to make sure it is OK. Only one meta table at a time is done here; checks for the consistency of the meta table across
 * species are done in MetaCrossSpecies.
 */
public class Meta extends SingleDatabaseTestCase {

    /**
   * Creates a new instance of CheckMetaDataTableTestCase
   */
	public Meta() {

		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Check that the meta table contains the right entries for the human and mouse");
	}

    /**
     * Check various aspects of the meta table.
     * 
     * @param dbre The database to check.
     * @return True if the test passed.
     */
	public boolean run(final DatabaseRegistryEntry dbre) {
	    boolean result = true;

	    Connection con = dbre.getConnection();
	    String metaKey;

	    result &= checkSchemaVersionDBName(dbre);

	    //check the Meta table in Human: should contain the entry for the pairwise_ld
	    if (dbre.getSpecies() == Species.HOMO_SAPIENS){
		//find out if there is an entry for the default LD Population
		metaKey = "pairwise_ld.default_population";

		result &= checkKeysPresent(con,metaKey);
		
		result &= checkForOrphansWithConstraint(con,"meta","meta_value","sample","sample_id","meta_key = '" + metaKey + "'");

	    }
	    if (dbre.getSpecies() == Species.MUS_MUSCULUS || dbre.getSpecies() == Species.RATTUS_NORVEGICUS || dbre.getSpecies() == Species.HOMO_SAPIENS){
		//find out if the entries in the Meta point to the strain information
	//	String[] metaKeys = {"read_coverage.coverage_level","individual.default_strain","source.default_source"};
	    String[] metaKeys = {"read_coverage.coverage_level","individual.default_strain","individual.display_strain","individual.reference_strain"};
		for (int i = 0; i < metaKeys.length; i++){
		    metaKey = metaKeys[i];
		    
		    result &= checkKeysPresent(con,metaKey);
		    if (metaKey == "read_coverage.coverage_level"){
		    	result &= checkForOrphansWithConstraint(con,"meta","meta_value","read_coverage","level","meta_key = '" + metaKey + "'");
		    }
		    else if ((metaKey == "individual.default_strain") || (metaKey == "individual.display_strain")){
		    	result &= checkForOrphansWithConstraint(con,"meta","meta_value","sample","name COLLATE latin1_general_cs","meta_key = '" + metaKey + "'");			
		    }
		    else if(metaKey == "individual.reference_strain"){
		    	result &= checkKeysPresent(con,metaKey);
		    	
		    }
		 /*   else if (metaKey == "source.default_source"){
			result &= checkForOrphansWithConstraint(con,"meta","meta_value","source","name","meta_key = '" + metaKey + "'");
		    }
		    */
		    
		}
	    }
	    if (dbre.getSpecies() == Species.CANIS_FAMILIARIS){
		//find out if the entries in the Meta point to the strain information
		String[] metaKeys = {"individual.default_strain"};
		for (int i = 0; i < metaKeys.length; i++){
		    metaKey = metaKeys[i];
		    
		    result &= checkKeysPresent(con,metaKey);
		    if (metaKey == "individual.default_strain"){
			result &= checkForOrphansWithConstraint(con,"meta","meta_value","sample","name COLLATE latin1_general_cs","meta_key = '" + metaKey + "'");
		    }
		}
	    }
	    if ( result ){
	    //if there were no problems, just inform for the interface to pick the HC
		ReportManager.correct(this,con,"Meta test passed without any problem");
	    }
	    return result;
	} // run

    // --------------------------------------------------------------

	private boolean checkKeysPresent(Connection con, String metaKey) {

		boolean result = true;

		int rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='" + metaKey + "'");
		if (rows == 0) {
		    result = false;
		    ReportManager.problem(this, con, "No entry in meta table for " + metaKey);
		} else {
		    ReportManager.correct(this, con, metaKey + " entry present");
		}

		return result;
	}

	// ---------------------------------------------------------------------
	/**
	 * Check that the schema_version in the meta table is present and matches the
	 * database name.
	 */
	private boolean checkSchemaVersionDBName(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// get version from database name
		String dbNameVersion = dbre.getSchemaVersion();
		logger.finest("Schema version from database name: " + dbNameVersion);

		// get version from meta table
		Connection con = dbre.getConnection();

		if (dbNameVersion == null) {
			ReportManager.warning(this, con, "Can't deduce schema version from database name.");
			return false;
		}

		String schemaVersion = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key='schema_version'");
		logger.finest("schema_version from meta table: " + schemaVersion);

		if (schemaVersion == null || schemaVersion.length() == 0) {

			ReportManager.problem(this, con, "No schema_version entry in meta table");
			return false;

		} else if (!schemaVersion.matches("[0-9]+")) {

			ReportManager.problem(this, con, "Meta schema_version " + schemaVersion + " is not numeric");
			return false;

		} else if (!dbNameVersion.equals(schemaVersion)) {

			ReportManager.problem(this, con, "Meta schema_version " + schemaVersion
					+ " does not match version inferred from database name (" + dbNameVersion + ")");
			return false;

		} else {

			ReportManager.correct(this, con, "schema_version " + schemaVersion + " matches database name version " + dbNameVersion);

		}
		return result;

	}
    // ---------------------------------------------------------
}
