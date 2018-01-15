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
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to see if Uniprot misspelling is present in descriptions
 * 
 * @author dstaines
 * 
 */
public class DbDisplayNameUniProt extends AbstractEgCoreTestCase {

	private final static String QUERY = "select db_display_name from external_db where db_display_name like binary ?";
	private final static String[] TERMS = { "%SPTREMBL%", "%SWISSPROT%",
			"%Uniprot%", "%UniProt/%" };

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String term : TERMS) {
			for (String entry : template.queryForDefaultObjectList(QUERY,
					String.class, term)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"An external_db entry has the display name " + entry
								+ " which is incorrect");
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if Uniprot (case!) misspelling is present in descriptions";
	}
}
