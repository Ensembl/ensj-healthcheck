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
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check to see if species.production_name entries are valid
 * @author dstaines
 * 
 */
public class SpeciesProductionName extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value from meta where meta_key=?";

	private final static String[] META_KEYS = { "species.production_name" };

	private static final Pattern VALID_SQL_NAME = Pattern
			.compile("^[A-z0-9_]+$");

	private static final Pattern INVALID_SQL_NAME2 = Pattern
	.compile("__+");

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String key : META_KEYS) {
			for (String speciesName : template.queryForDefaultObjectList(
					META_QUERY, String.class, key)) {
				if (!VALID_SQL_NAME.matcher(speciesName).matches()) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Meta value " + speciesName + " for key  " + key
									+ " does not match the required value");
				} else if (INVALID_SQL_NAME2.matcher(speciesName).matches()) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Meta value " + speciesName + " for key  " + key
									+ " does not match the required value");					
				}
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Check to see if species.production_name entries are valid";
	}

}
