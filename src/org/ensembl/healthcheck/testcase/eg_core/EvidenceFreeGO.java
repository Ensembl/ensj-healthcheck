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
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to see if we have GO terms where there is no evidence
 * @author dstaines
 *
 */
public class EvidenceFreeGO extends AbstractEgCoreTestCase {

	private final static String EVIDENCE_FREE_GO = "select count(*) from object_xref ox "
			+ "join xref x using (xref_id) "
			+ "join external_db e using (external_db_id) "
			+ "left join ontology_xref oox using (object_xref_id) "
			+ "where e.db_name='GO' and oox.object_xref_id is null;";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int n =  getTemplate(dbre).queryForDefaultObject(EVIDENCE_FREE_GO, Integer.class);
		if(n>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), n+" GO terms found with no evidence: "+EVIDENCE_FREE_GO);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if we have GO terms where there is no evidence";
	}

}
