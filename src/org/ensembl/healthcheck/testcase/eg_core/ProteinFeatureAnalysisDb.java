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

package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to make sure that db column is populated for analysis types attached to
 * protein features. If this is missing the domain column will not be populated.
 * 
 * @author dstaines
 * 
 */
public class ProteinFeatureAnalysisDb extends AbstractEgCoreTestCase {

	private static final String SQL = "select logic_name from analysis a where (db='' or db is null) and analysis_id in (select distinct analysis_id from protein_feature pf)";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passed = true;
		List<String> logicNames = getTemplate(dbre).queryForDefaultObjectList(
				SQL, String.class);
		if (logicNames.size() > 0) {
			passed = false;
			ReportManager
					.problem(
							this,
							dbre.getConnection(),
							"The following analysis types attached to protein features do not have db set and will not display properly: "
									+ logicNames);
		}
		return passed;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to make sure that db column is populated for analysis types attached to"
		 +" protein features. If this is missing the domain column will not be populated.";
	}

}
