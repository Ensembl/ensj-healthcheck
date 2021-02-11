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
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test to see if expected identity xrefs are set
 * @author dstaines
 *
 */
public class IdentityXref extends AbstractEgCoreTestCase {

	private final static String QUERY = "select distinct(e.db_name) from external_db e "
			+ "join xref x using (external_db_id) "
			+ "join object_xref ox using (xref_id) "
			+ "join identity_xref ix using (object_xref_id)";

	private final List<String> identityXrefDbs;

	public IdentityXref() {
		super();
		identityXrefDbs = TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/identity_xref_dbs.txt");
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int n = 0;
		for (String dbname : getTemplate(dbre).queryForDefaultObjectList(QUERY,
				String.class)) {
			n++;
			if (!identityXrefDbs.contains(dbname)) {
				ReportManager.problem(this, dbre.getConnection(),
						"Unexpected identity xref db " + dbname);
				passes = false;
			}
		}
		if (n == 0) {
			ReportManager.problem(this, dbre.getConnection(),
					"Warning: No descripitions found with source lines");
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if expected identity xrefs are set";
	}

}
