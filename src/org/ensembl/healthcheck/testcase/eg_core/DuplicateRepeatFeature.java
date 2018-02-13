/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/**
 * File: DuplicateRepeatFeatures.java
 * Created by: James Allen
 * Created on: Apr 22, 2014
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to find where repeat_features have been added twice
 * @author jallen
 *
 */
public class DuplicateRepeatFeature extends AbstractEgCoreTestCase {

	private final static String DUPLICATE_RF  = "SELECT COUNT(*) FROM (SELECT MAX(repeat_feature_id) FROM repeat_feature group by seq_region_id, seq_region_start, seq_region_end, seq_region_strand, repeat_start, repeat_end, repeat_consensus_id, analysis_id, score HAVING COUNT(*) > 1) nr;";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nDupRF =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_RF, Integer.class);
		if(nDupRF>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupRF+" duplicates found in repeat_feature: "+DUPLICATE_RF);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find where repeat_features have been added twice";
	}

}
