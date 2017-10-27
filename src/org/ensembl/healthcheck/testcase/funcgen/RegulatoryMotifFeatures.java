/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class RegulatoryMotifFeatures extends SingleDatabaseTestCase {


	/**
	 * Create a new instance of StableID.
	 */
	public RegulatoryMotifFeatures() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");

		//setHintLongRunning(true); // should be relatively fast
		setTeamResponsible(Team.FUNCGEN);

		setDescription("Checks if all motifs from peaks are associated to their respective regulatory features.");
		setPriority(Priority.AMBER);
		setEffect("Regulatory Features will seem to miss some motif features.");
		setFix("Re-project motif features or fix manually.");
	}


	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		//Do we really need these removes?
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);
	}


	/**
	 * Run the test.
	 * We will check if all the motif features in a regulatory feature contain all
	 *  the motif features associated to the peaks associated to the regulatory feature
	 *
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 *
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;
		Connection con = dbre.getConnection();


    // NOTE: SQL strings unbroken to allow easy copying into MySQL client

    // Restricts to current i.e. non-archive fset which have a v[0-9]+ suffix
    // This has to be a distnct count here as MF reg attrs are non-redundant across AFs within an RF
    // Hence the 2nd count would always be higher
    // Need change this to iterate over vell type build to capture differences within cell lines, which maybe currently masked
    // by the cell type wide distinct count.

		int regMFs = DBUtils.getRowCount(con, "SELECT count(distinct attribute_feature_id) from regulatory_attribute ra join regulatory_feature rf on ra.regulatory_feature_id=rf.regulatory_feature_id and attribute_feature_table='motif' join feature_set fs on rf.feature_set_id=fs.feature_set_id and fs.name not rlike '.*_v[0-9]+'");
    //"SELECT COUNT(distinct attribute_feature_id) from regulatory_attribute where attribute_feature_table='motif'"


		int fmaxLength = 2000;  // Accounts for potential out of bounds MFs from demoted TFs

		int regAMFs = DBUtils.getRowCount
        ( con, "select count(distinct amf.motif_feature_id) from associated_motif_feature amf, peak p, regulatory_attribute ra, regulatory_feature rf, feature_set fs where p.peak_id=amf.peak_id and ra.attribute_feature_id=p.peak_id and ra.attribute_feature_table='annotated' and (p.seq_region_end - p.seq_region_start +1) <= " + fmaxLength +" and ra.regulatory_feature_id=rf.regulatory_feature_id and rf.feature_set_id=fs.feature_set_id and fs.name not rlike '.*_v[0-9]+'");



		if(regMFs != regAMFs){
        // This incorporates mfs for afs < 2000bp where the af has been integrated into the rf, but the mf hasn't for some reason?
        // Or when  we have deleted an af with but not an associated mf that were both supporting an rf

        ReportManager.problem
				( this,  con, "The number of total non-distinct motif features associated to regulatory features (" + regMFs +
				  ") does not correspond to the number of distinct motif features within its associated peaks ("
				  + regAMFs + ") which are less than " + fmaxLength + " bp\n" +
				  "USEFUL SQL:\nALTER table regulatory_attribute add index `attribute_id_type`(attribute_feature_table, attribute_feature_id);\n" +
				  "insert ignore into regulatory_attribute select ra.regulatory_feature_id, amf.motif_feature_id, 'motif' from " +
				  "peak p, regulatory_attribute ra, associated_motif_feature amf left join " +
				  "regulatory_attribute ra1 on (amf.motif_feature_id=ra1.attribute_feature_id and ra1.attribute_feature_table='motif') " +
				  "where p.peak_id=amf.peak_id and ra.attribute_feature_id=p.peak_id and " +
				  "ra.attribute_feature_table='annotated' and (p.seq_region_end - p.seq_region_start +1) <= " +
				  fmaxLength + " and ra1.attribute_feature_id is NULL;\nALTER table regulatory_attribute drop index `attribute_id_type`;"
				  );


        result = false;
		}


		int outOfBoundMFs = DBUtils.getRowCount
        (con,
         "SELECT count(mf.motif_feature_id) FROM feature_set fs, regulatory_feature rf, regulatory_attribute ra, motif_feature mf WHERE fs.feature_set_id=rf.feature_set_id AND rf.regulatory_feature_id=ra.regulatory_feature_id AND ra.attribute_feature_table='motif' AND ra.attribute_feature_id=mf.motif_feature_id AND fs.name not rlike '.*_v[0-9]+' AND  ( (mf.seq_region_end < (rf.seq_region_start - rf.bound_start_length)) OR (mf.seq_region_start > (rf.seq_region_end + rf.bound_end_length)))");


    if(outOfBoundMFs != 0){

        ReportManager.problem
            ( this,  con,
              "Found " + outOfBoundMFs + " MotifFeatures which lie outside the core region. USEFUL SQL:\n" +
              "SELECT mf.motif_feature_id, mf.seq_region_start as 'mf start', mf.seq_region_end as 'mf end', rf.regulatory_feature_id as 'rf ID', rf.seq_region_start as 'rf seq start', rf.seq_region_end as 'rf seq end', rf.bound_start_length as 'bound start', rf.bound_end_length as 'bound end', rf.seq_region_start - rf.bound_start_length as 'real start', rf.seq_region_end + rf.bound_end_length as 'real end', lpad(rf.stable_id, 11, 0) FROM feature_set fs, regulatory_feature rf, regulatory_attribute ra, motif_feature mf WHERE fs.feature_set_id = rf.feature_set_id and rf.regulatory_feature_id = ra.regulatory_feature_id and ra.attribute_feature_table = 'motif' and ra.attribute_feature_id = mf.motif_feature_id and fs.name not rlike '.*_v[0-9]+' AND ( (mf.seq_region_end < (rf.seq_region_start - rf.bound_start_length)) OR (mf.seq_region_start > (rf.seq_region_end + rf.bound_end_length)))");

              result = false;
    }

		return result;
	}
}
