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
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.TemplateBuilder;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Check that translation_attribs are valid
 * 
 * @author dstaines
 * 
 */
public class TranslationAttribType extends AbstractEgCoreTestCase {

	private final static String QUERY = "select a.code from translation_attrib ta join attrib_type a "
			+ "using (attrib_type_id) where code not in ($inlist$)";

	private final String query;

	public TranslationAttribType() {
		super();
		query = TemplateBuilder
				.template(
						QUERY,
						"inlist",
						TestCaseUtils.resourceToInList("/org/ensembl/healthcheck/testcase/eg_core/translation_attribs.txt"));
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		Set<String> unknownCodes = CollectionUtils.createHashSet();
		for (String code : getTemplate(dbre).queryForDefaultObjectList(query,
				String.class)) {
			passes = false;
			unknownCodes.add(code);
		}
		for (String code : unknownCodes) {
			ReportManager.problem(this, dbre.getConnection(),
					"Translation attrib found with unexpected code " + code);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Check that translation_attribs are valid";
	}

}
