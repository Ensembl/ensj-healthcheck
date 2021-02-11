/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * Test to find where protein IDs have been added twice
 * @author dstaines
 *
 */
public class DuplicateProteinId extends AbstractEgCoreTestCase {

	private final static String QUERY = "select x.xref_id from xref x join external_db e using (external_db_id) where e.db_name='protein_id' and x.dbprimary_acc=x.display_label";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		for(String id: getTemplate(dbre).queryForDefaultObjectList(QUERY, String.class)) {
			passes= false;
			ReportManager.problem(this, dbre.getConnection(), "ProteinId Xref "+id+" has the same primary and display IDs");
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find where protein IDs have been added twice";
	}

}
