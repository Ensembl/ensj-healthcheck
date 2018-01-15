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
 * EnaProvider
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure assembly.default meets the required pattern
 * 
 * @author dstaines
 * 
 */
public class AssemblyDefault extends AbstractEgCoreTestCase {

	private final static String META_VAL = "select meta_value from meta where meta_key=? and species_id=?";
	private final static Pattern ASS_PAT = Pattern.compile("^[A-Za-z0-9._-]+$");

	public AssemblyDefault() {
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
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		for (int speciesId : dbre.getSpeciesIds()) {
			List<String> assAccs = temp.queryForDefaultObjectList(META_VAL,
					String.class, "assembly.default", speciesId);
			if (assAccs.size() == 0) {
				ReportManager.problem(this, dbre.getConnection(),
						"No assembly.default entry found for species "
								+ speciesId);
				result = false;
			} else if (assAccs.size() > 1) {
				ReportManager.problem(this, dbre.getConnection(),
						"Multiple assembly.default entries found for species "
								+ speciesId);
				result = false;
			} else {
				String assAcc = assAccs.get(0);
				if (!ASS_PAT.matcher(assAcc).matches()) {
					ReportManager.problem(this, dbre.getConnection(),
							"assembly.default " + assAcc + " for species "
									+ speciesId
									+ " does not match expected pattern "
									+ ASS_PAT.toString());
					result = false;
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription
	 * ()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check that assembly.default is set correctly";
	}

}
