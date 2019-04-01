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

package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

public class NoRepeatFeatures extends AbstractEgCoreTestCase {

	@Override
	protected String getEgDescription() {
		return "Check that some kind of repeat masker has been run on the species.";
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		List<Integer> numRepeatFeaturesList = getTemplate(dbre).queryForDefaultObjectList(
				"select count(*) from repeat_feature;", Integer.class
		);
		
		Integer numRepeatFeatures = numRepeatFeaturesList.get(0);
		
		if (numRepeatFeatures==0) {
			
			ReportManager.problem(
				this, 
				dbre.getConnection(), 
				"No repeat features found!"
			);
			
			return false;
		}
		if (numRepeatFeatures<100) {
			
			ReportManager.problem(
				this, 
				dbre.getConnection(), 
				"Number of repeat features ("+numRepeatFeatures+") is suspiciously low!"
			);
			
			return false;
		}
		
		return true;
	}

}
