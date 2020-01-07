/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
 * SimpleFeatureAnalysisTypes
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to check simple features for permitted types
 * 
 * @author dstaines
 * 
 */
public class SimpleFeatureAnalysisTypes extends AbstractEgCoreTestCase {

	private final static String LOGIC_NAME_SQL = "select distinct logic_name from simple_feature "
			+ "join analysis using (analysis_id) "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "where species_id=?";

	private final static Set<String> BLACKLISTED_ANALYSES = new HashSet<String>(
			Arrays.asList(new String[] { "gene", "mrna", "cds" }));

	public SimpleFeatureAnalysisTypes() {
		super();
		setDescription("Test to check simple features are not in the blacklist: "
				+ BLACKLISTED_ANALYSES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org
	 * .ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		// count number genes
		for (int speciesId : dbre.getSpeciesIds()) {
			for (String analysis : temp.queryForDefaultObjectList(
					LOGIC_NAME_SQL, String.class, speciesId)) {
				if (BLACKLISTED_ANALYSES.contains(analysis)) {
					ReportManager.problem(this, dbre.getConnection(),
							"Blacklisted simple_feature of analysis type "
									+ analysis + " found for species "
									+ speciesId);
					result = false;
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check simple features for permitted types";
	}
}
