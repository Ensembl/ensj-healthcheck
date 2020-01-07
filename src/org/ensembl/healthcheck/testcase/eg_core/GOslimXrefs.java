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
 * File: GOslimXrefs.java
 * Created by: James Allen
 * Created on: Sep 17, 2014
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to check for GOslim xrefs, which are unnecessary.
 * (The web display gets the information from the ontology database.)
 * @author jallen
 *
 */
public class GOslimXrefs extends AbstractEgCoreTestCase {

	private final static String GOSLIM_XREFS  = "SELECT COUNT(*) FROM xref x INNER JOIN object_xref ox USING (xref_id) INNER JOIN external_db USING (external_db_id) WHERE db_name LIKE 'goslim%';";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nXrefs =  getTemplate(dbre).queryForDefaultObject(GOSLIM_XREFS, Integer.class);
		if(nXrefs>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nXrefs+" GOslim xrefs found: "+GOSLIM_XREFS);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check for GOslim xrefs, which are unnecessary. (The web display gets the information from the ontology database.)";
	}

}
