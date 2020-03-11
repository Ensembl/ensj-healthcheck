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
 * File: SeqRegionName.java
 * Created by: dstaines
 * Created on: Apr 9, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * @author dstaines
 * 
 */
public class SeqRegionName extends AbstractEgCoreTestCase {

	private static final String VALID_NAME = "^[A-z0-9.-]+$";

	public SeqRegionName() {
		super();
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
		boolean passes = true;
		for (String name : getTemplate(dbre).queryForDefaultObjectList(
				"select name from seq_region", String.class)) {
			if (!name.matches(VALID_NAME)) {
				ReportManager.problem(this, dbre.getConnection(),
						"Seq_region name " + name
								+ " does not match expected expression "
								+ VALID_NAME);
				passes = false;
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Checks that seq_region names contain only the permitted characters";
	}
}
