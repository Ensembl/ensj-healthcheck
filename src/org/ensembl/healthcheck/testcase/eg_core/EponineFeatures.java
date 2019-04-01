/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * File: EponineFeatures.java
 * Created by: James Allen
 * Created on: Apr 22, 2014
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to check for Eponine features, not considered appropriate
 * for non-vertebrates due to the training data that was used.
 * @author jallen
 *
 */
public class EponineFeatures extends AbstractEgCoreTestCase {

	private final static String EPONINE_FEATS  = "SELECT COUNT(*) FROM simple_feature INNER JOIN analysis USING (analysis_id) WHERE logic_name = 'eponine';";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nEponine =  getTemplate(dbre).queryForDefaultObject(EPONINE_FEATS, Integer.class);
		if(nEponine>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nEponine+" Eponine features found: "+EPONINE_FEATS);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check for Eponine features, not considered appropriate for non-vertebrates due to the training data that was used.";
	}

}
