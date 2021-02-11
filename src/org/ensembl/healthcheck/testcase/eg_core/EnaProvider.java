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
 * EnaProvider
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure we're not using ENA as the provider where we could be using something better and more accurate
 * @author dstaines
 *
 */
public class EnaProvider extends AbstractEgCoreTestCase {
	
	private final static String META_VAL = "select distinct(meta_value) from meta where meta_key=?";
	private final static String[] KEYS = {"provider.name","provider.url"};
	private final static String[] VALS = {"European Nucleotide Archive","http://www.ebi.ac.uk/ena/"};
	public EnaProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		for(String key: KEYS) {
			for(String val: temp.queryForDefaultObjectList(META_VAL, String.class, key)) {
				for(String badVal: VALS) {
					if(val.contains(badVal)) {
						ReportManager.problem(this, dbre.getConnection(), "Meta key "+key+" set to GenomeLoader default "+val);
						result = false;
					}
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to make sure the default GenomeLoader providers are not used as there are likely to be better providers to use";
	}

}
