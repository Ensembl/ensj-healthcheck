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
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test for where xrefs have been added twice to the same Ensembl object.
 * This is acceptable where analysis_id is different e.g. GO terms from 2 pipelines
 * @author dstaines
 *
 */
public class DuplicateObjectXref extends AbstractEgCoreTestCase {

	private final static String DUPLICATE_OBJ_XREF = "select count(*) from (select count(*) from xref x join object_xref ox using (xref_id) left outer join ontology_xref ontx using (object_xref_id) group by ox.ensembl_id, ox.ensembl_object_type,x.dbprimary_acc,x.external_db_id,x.info_type,x.info_text,ontx.source_xref_id,ontx.linkage_type having count(*)>1) cc";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nDupOX =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_OBJ_XREF, Integer.class);
		if(nDupOX>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupOX+" duplicates found in object_xref: "+DUPLICATE_OBJ_XREF);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test for where object_xrefs have been added twice";
	}

}
