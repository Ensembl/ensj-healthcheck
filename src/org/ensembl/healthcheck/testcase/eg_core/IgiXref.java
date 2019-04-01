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

/**
 * File: IgiXrefTest.java
 * Created by: dstaines
 * Created on: Dec 3, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to see if IGIs have been accidentally left in
 * 
 * @author dstaines
 * 
 */
public class IgiXref extends AbstractEgCoreTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int n = getTemplate(dbre).queryForDefaultObject(
				"Select count(*) from xref where external_db_id=60014",
				Integer.class);
		if (n > 0) {
			ReportManager
					.problem(this, dbre.getConnection(), "IGI xrefs found");
		}
		return passes;
	}

	@Override
	public boolean canRepair() {
		return true;
	}

	@Override
	public String getFix() {
		return "delete ox.*,x.* from object_xref ox, xref x where x.xref_id=ox.xref_id and x.external_db_id=60014;";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#
	 * getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if IGIs have been accidentally left in";
	}

}
