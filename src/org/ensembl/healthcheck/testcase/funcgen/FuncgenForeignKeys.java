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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.generic.CoreForeignKeys;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class FuncgenForeignKeys extends CoreForeignKeys {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public FuncgenForeignKeys() {

		//addToGroup("post_regbuild");
		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("funcgen");
		setDescription("Check for broken foreign-key relationships.");
		setHintLongRunning(true);
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
		
		result &= checkForOrphans(con, "annotated_feature", "feature_set_id", "feature_set", "feature_set_id", true);
		
		result &= checkForOrphans(con, "array", "array_id", "array_chip", "array_id", false);
		
		
		// ----------------------------
		// Ensure that we have no  orphaned associate_feature_types
	
		result &= checkForOrphans(con, "associated_feature_type", "feature_type_id", "feature_type", "feature_type_id", true);
		
		for (int i = 0; i < featTabs.length; i++) {
			String type = featTabs[i].replaceAll("_feature", "");
			result &= checkForOrphansWithConstraint(con, "associated_feature_type", "feature_id", featTabs[i], featTabs[i] + "_id", "feature_table='" + type + "'");
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
		
		
		//design_type?
		
		result &= checkForOrphans(con, "input_set", "experiment_id", "experiment", "experiment_id", true);
		
		result &= checkForOrphans(con, "experimental_chip", "experiment_id", "experiment", "experiment_id", true);
		
		result &= checkForOrphans(con, "experimental_chip", "feature_type_id", "feature_type", "feature_type_id", true);
		
		//experimental design?
		
		result &= checkForOrphans(con, "experiment", "experimental_group_id", "experimental_group", "experimental_group_id", true);
		result &= checkForOrphansWithConstraint(con, "experiment", "mage_xml_id", "mage_xml", "mage_xml_id", "mage_xml_id is NOT NULL");
		//experimental variable?
		
		result &= checkForOrphans(con, "external_feature", "feature_set_id", "feature_set", "feature_set_id", true);
		
		result &= checkForOrphans(con, "feature_set", "analysis_id", "analysis", "analysis_id", true);
		
		result &= checkForOrphans(con, "feature_set", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "regulatory_feature", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "result_set", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "input_set", "feature_type_id", "feature_type", "feature_type_id", true);
		
		result &= checkForOrphans(con, "input_set", "input_set_id", "input_subset", "input_set_id", false);
		
		result &= checkForOrphans(con, "mage_xml", "mage_xml_id", "experiment", "mage_xml_id", true);
				
		result &= checkForOrphans(con, "probe", "array_chip_id", "array_chip", "array_chip_id", false);
		
		//result &= checkForOrphans(con, "probe", "probe_set_id", "probe_set", "probe_set_id", false);
		result &= checkForOrphansWithConstraint(con, "probe", "probe_set_id", "probe_set", "probe_set_id", "probe_set_id is NOT NULL");
		
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
		String[] statusTables = {"array", "array_chip", "experimental_chip", "channel", "input_set", "input_subset", "result_set", "data_set", "feature_set"};
		
		for (int i = 0; i < statusTables.length; i++) {
			result &= checkForOrphansWithConstraint(con, "status", "table_id", statusTables[i], statusTables[i] + "_id", "table_name='" + statusTables[i] + "'");
		}
		
		result &= checkForOrphans(con, "status", "status_name_id", "status_name", "status_name_id", true);
		
		String[] supportingTypes = {"result", "feature", "input"};
		
		//This only checks for table_ids which have been orphaned by the input table
	
		for (int i = 0; i < supportingTypes.length; i++) {
			result &= checkForOrphansWithConstraint(con, "supporting_set", "supporting_set_id", supportingTypes[i] + "_set",  supportingTypes[i] + "_set_id", "type='" + supportingTypes[i] + "'");
		}

		
		
		
		result &= checkForOrphans(con, "object_xref", "xref_id", "xref", "xref_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "xref", "external_db_id", "external_db", "external_db_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "external_synonym", "xref_id", "xref", "xref_id", true);//shouldn't this be false?

		result &= checkForOrphans(con, "identity_xref", "object_xref_id", "object_xref", "object_xref_id", true);//shouldn't this be false?


		// ----------------------------
		// Check object xrefs point to existing objects
		String[] types = { "Probe", "ProbeFeature", "ProbeSet" };
		//This will fail as we need to insert an _
		for (int i = 0; i < types.length; i++) {
			result &= checkKeysByEnsemblObjectType(con, "object_xref", types[i]);
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

	
		return result;

	}
	
	
	// -------------------------------------------------------------------------

} // FuncgenForeignKeys
