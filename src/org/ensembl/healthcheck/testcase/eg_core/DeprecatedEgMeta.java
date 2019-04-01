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
 * File: EgMeta.java
 * Created by: dstaines
 * Created on: Mar 2, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * @author dstaines
 * 
 */
public class DeprecatedEgMeta extends AbstractEgMeta {

	/**
	 * Default constructor
	 */
	public DeprecatedEgMeta() {
		super(
				TestCaseUtils
						.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/deprecated_meta_keys.txt"));
	}

	@Override
	protected boolean testKeys(DatabaseRegistryEntry dbre, int speciesId,
			List<String> speciesKeys, List<String> testKeys) {
		boolean passes = true;
		for(String key: speciesKeys) {
			if(testKeys.contains(key)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"Meta table for " + speciesId
								+ " contains a value for the deprecated key " + key);
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Checks for deprecated meta keys";
	}

}

