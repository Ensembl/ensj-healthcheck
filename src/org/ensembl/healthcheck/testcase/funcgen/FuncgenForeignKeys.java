/*
 * Copyright [1999-2016] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;
import org.ensembl.healthcheck.Team;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class FuncgenForeignKeys extends CoreForeignKeys {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public FuncgenForeignKeys() {

		addToGroup("post_regbuild");
		addToGroup("funcgen-release");
		addToGroup("funcgen");
		setDescription("Check for broken foreign-key relationships.");
		setHintLongRunning(true);
		setTeamResponsible(Team.FUNCGEN);
		removeSecondTeamResponsible(); // Does not appear to be imported
	}

	/**
	 * Look for broken foreign key relationships.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		String[] featTabs = getFuncgenFeatureTables();
		//We need to write a new method here to handle denormalised link tables
		
		
		try{
		
		result &= checkForOrphans(con, "annotated_feature", "feature_set_id", "feature_set", "feature_set_id", true);
		
		result &= checkForOrphans(con, "array", "array_id", "array_chip", "array_id", false);
		
		
		// ----------------------------
		// Ensure that we have no  orphaned associate_feature_types
	
		result &= checkForOrphans(con, "associated_feature_type", "feature_type_id", "feature_type", "feature_type_id", true);
		
		// Get table_names from associated_feature_type

		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT distinct(table_name) from associated_feature_type");

			while (rs.next()){
				String tableName   = rs.getString(1); 
				result &= checkForOrphansWithConstraint(con, "associated_feature_type", "table_id", tableName, tableName + "_id", "table_name='" + tableName + "'");
			}
		
			rs.close();
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}


		//Need a constraint here where fs.cell_type_id is NOT NULL
		//result &= checkForOrphans(con, "feature_set", "cell_type_id", "cell_type", "cell_type_id", true);
		result &= checkForOrphansWithConstraint(con, "feature_set", "cell_type_id", "cell_type", "cell_type_id", "cell_type_id IS NOT NULL");
		
		result &= checkForOrphans(con, "experimental_chip", "cell_type_id", "cell_type", "cell_type_id", true);
		
		result &= checkForOrphans(con, "input_set", "cell_type_id", "cell_type", "cell_type_id", true);
		
		result &= checkForOrphans(con, "result_set", "cell_type_id", "cell_type", "cell_type_id", true);
		//This may fail as it's not necessary to have a cell_type_id in a result_set???
		
		result &= checkForOrphans(con, "channel", "experimental_chip_id", "experimental_chip", "experimental_chip_id", false);
						
		result &= checkForOrphans(con, "data_set", "data_set_id", "supporting_set", "data_set_id", false);
		
		result &= checkForOrphansWithConstraint(con, "data_set", "feature_set_id", "feature_set", "feature_set_id", "feature_set_id != 0");
		
		

		
		result &= checkForOrphans(con, "input_subset", "experiment_id", "experiment", "experiment_id", true); 
		
		result &= checkForOrphans(con, "experimental_chip", "experiment_id", "experiment", "experiment_id", true);
		
		result &= checkForOrphans(con, "experimental_chip", "feature_type_id", "feature_type", "feature_type_id", true);
		
		
		result &= checkForOrphans(con, "experiment", "experimental_group_id", "experimental_group", "experimental_group_id", true);
		result &= checkForOrphansWithConstraint(con, "experiment", "mage_xml_id", "mage_xml", "mage_xml_id", "mage_xml_id is NOT NULL");

		
		result &= checkForOrphans(con, "external_feature", "feature_set_id", "feature_set", "feature_set_id", true);
		
		result &= checkForOrphans(con, "feature_set", "analysis_id", "analysis", "analysis_id", true);
		
		result &= checkForOrphans(con, "feature_set", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "regulatory_feature", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "result_set", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "input_set", "feature_type_id", "feature_type", "feature_type_id", true);
	
	  result &= checkForOrphans(con, "input_set", "analysis_id", "analysis", "analysis_id", true);
		
		result &= checkForOrphans(con, "input_set", "input_set_id", "input_set_input_subset", "input_set_id", false);
		
		result &= checkForOrphans(con, "input_set_input_subset", "input_subset_id", "input_subset", "input_subset_id", true);
    
    result &= checkForOrphans(con, "input_subset", "feature_type_id", "feature_type", "feature_type_id", true);
    result &= checkForOrphans(con, "input_subset", "cell_type_id", "cell_type", "cell_type_id", true);

		//Need to check for input_sets which are nor present in supporting_set and result_set_input
		//reverse is already done, but we need this logical && test
		
		result &= checkForOrphans(con, "mage_xml", "mage_xml_id", "experiment", "mage_xml_id", true);
				
		result &= checkForOrphans(con, "probe", "array_chip_id", "array_chip", "array_chip_id", false);
		
		//result &= checkForOrphans(con, "probe", "probe_set_id", "probe_set", "probe_set_id", false);
		result &= checkForOrphansWithConstraint(con, "probe", "probe_set_id", "probe_set", "probe_set_id", "probe_set_id !=0");
		
		result &= checkForOrphans(con, "probe_set", "probe_set_id", "probe", "probe_set_id");
		
		//result &= checkForOrphans(con, "probe", "probe_id", "probe_feature", "probe_id", false);
		//Can have unmapped probes or arrays
		
		result &= checkForOrphans(con, "probe_feature", "probe_id", "probe", "probe_id", true);
		
		result &= checkForOrphans(con, "probe_feature", "analysis_id", "analysis", "analysis_id", true);
		
		result &= checkForOrphans(con, "regulatory_attribute", "regulatory_feature_id", "regulatory_feature", "regulatory_feature_id", true);
		

		

		for (int i = 0; i < featTabs.length; i++) {
			
			if(! featTabs[i].equals("regulatory_feature")){
			
				String type = featTabs[i].replaceAll("_feature", "");
				result &= checkForOrphansWithConstraint(con, "regulatory_attribute", "attribute_feature_id", featTabs[i], featTabs[i] + "_id","attribute_feature_table='" + type + "'");
			}		
		}
		
		result &= checkForOrphans(con, "regulatory_feature", "feature_set_id", "feature_set", "feature_set_id", true);
		
		result &= checkForOrphans(con, "result", "result_set_input_id", "result_set_input", "result_set_input_id", true);
		
		result &= checkForOrphans(con, "result", "probe_id", "probe", "probe_id", true);
		
		result &= checkForOrphans(con, "result_set", "analysis_id", "analysis", "analysis_id", true);

		String[] rsetInputTables = {"experimental_chip", "channel", "input_set"};
		
		//This only checks for table_ids which have been orphaned by the input table
		for (int i = 0; i < rsetInputTables.length; i++) {
			result &= checkForOrphansWithConstraint(con, "result_set_input", "table_id", rsetInputTables[i], rsetInputTables[i] + "_id", "table_name='" + rsetInputTables[i] + "'");
		}
		
		//Don't check for result_set_id in supporting set as this is nor mandatory
		
		//No valid enum'd table list for status, so just test what we have currently 
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT distinct(table_name) from status");
			
			while (rs.next()){
				String tableName   = rs.getString(1); 
				result &= checkForOrphansWithConstraint(con, "status", "table_id", tableName, tableName + "_id", "table_name='" + tableName + "'");
			}
			
			rs.close();
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
		
		
		result &= checkForOrphans(con, "status", "status_name_id", "status_name", "status_name_id", true);
		
			
		//This only checks for table_ids which have been orphaned by the input table
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT distinct(type) from supporting_set");
			
			while (rs.next()){
				String setType   = rs.getString(1); 
				result &= checkForOrphansWithConstraint(con, "supporting_set", "supporting_set_id", setType + "_set",  setType + "_set_id", "type='" + setType + "'");
			}
			
			rs.close();
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
	
			
		
		result &= checkForOrphans(con, "object_xref", "xref_id", "xref", "xref_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "xref", "external_db_id", "external_db", "external_db_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "external_synonym", "xref_id", "xref", "xref_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "identity_xref", "object_xref_id", "object_xref", "object_xref_id", true);//shouldn't this be false?


		// ----------------------------
		// Check object xrefs point to existing objects
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT distinct(ensembl_object_type) from object_xref");
			
			while (rs.next()){
				String objType   = rs.getString(1); 
				result &= checkKeysByEnsemblObjectType(con, "object_xref", objType);
			}
			
			rs.close();
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
			
	

		// ----------------------------
		// Ensure that feature tables reference existing seq_regions
	
		for (int i = 0; i < featTabs.length; i++) {
			String featTab = featTabs[i];
			// skip large tables as this test takes an inordinately long time
			// if (featTab.equals("protein_align_feature") || featTab.equals("dna_align_feature") || featTab.equals("repeat_feature")) {
			// continue;
			// }
			result &= checkForOrphans(con, featTab, "seq_region_id", "seq_region", "seq_region_id", true);
		}

		result &= checkForOrphans(con, "analysis_description", "analysis_id", "analysis", "analysis_id", true);//shouldn't this be false?


		result &= checkForOrphans(con, "unmapped_object", "unmapped_reason_id", "unmapped_reason", "unmapped_reason_id", true);
		result &= checkForOrphans(con, "unmapped_object", "analysis_id", "analysis", "analysis_id", true);

		result &= checkOptionalRelation(con, "unmapped_object", "external_db_id", "external_db", "external_db_id");

		// ----------------------------
		// Check tables which reference the analysis table
		String[] analysisTabs = getFuncgenTablesWithAnalysisID();

		for (int i = 0; i < analysisTabs.length; i++) {
			String analysisTab = analysisTabs[i];
			// skip large tables as this test takes an inordinately long time
			//if (analysisTab.equals("protein_align_feature") || analysisTab.equals("dna_align_feature") || analysisTab.equals("repeat_feature")) {
			//	continue;
			//}

			//Isn't this jsut checkForOrphansWithConstraint?
			//Or is this just allowing the analysisTab to contain NULLs
			
			if (countOrphansWithConstraint(con, analysisTab, "analysis_id", "analysis", "analysis_id", "analysis_id IS NOT NULL") > 0) {
				ReportManager.problem(this, con, "FAILED object_xref -> analysis using FK analysis_id relationships");
				result = false;
			}

		}
		}
		catch(Exception e){ //Catch all possible exceptions
		  ReportManager   
      .problem(
                      this,
                      con,
                      "HealthCheck generated an exception:\n\t"
                                      + e.getMessage());
		  result = false;
		}

	
		return result;

	}
	
	
	// -------------------------------------------------------------------------

} // FuncgenForeignKeys
