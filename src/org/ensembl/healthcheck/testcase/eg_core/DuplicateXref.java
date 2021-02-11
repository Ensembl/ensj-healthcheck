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
 * Test for where xrefs have been added twice
 * @author dstaines
 *
 */
public class DuplicateXref extends AbstractEgCoreTestCase {

	private final static String DUPLICATE_XREF = "select count(*) from (select count(*) from xref x group by x.dbprimary_acc,x.external_db_id,x.info_type,x.info_text having count(*)>1) cc";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nDupX =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_XREF, Integer.class);
		if(nDupX>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupX+" duplicates found in xref: "+DUPLICATE_XREF);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test for where xrefs have been added twice with different descriptions or versions etc.";
	}

}
