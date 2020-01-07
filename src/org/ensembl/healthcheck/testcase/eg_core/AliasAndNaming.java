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
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test for whether EnsemblGenomes species are correctly named
 * 
 * @author dstaines
 * 
 */
public class AliasAndNaming extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value from meta where meta_key=? and species_id=?";
	private static final Pattern VALID_PRODUCTION_NAME = Pattern
			.compile("^[0-9a-z_]+$");

	private static final Pattern INVALID_PRODUCTION_NAME2 = Pattern
			.compile("__+");

	private static final String PRODUCTION_NAME = "species.production_name";
	private static final String DB_NAME = "species.db_name";
	private static final String SCI_NAME = "species.scientific_name";
	private static final String ALIAS = "species.alias";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate template = getTemplate(dbre);
		boolean passes = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			passes &= checkNames(dbre, template, speciesId);
		}
		return passes;
	}

	private String getName(DatabaseRegistryEntry dbre, SqlTemplate template,
			int speciesId, String key) {
		List<String> sciNames = template.queryForDefaultObjectList(META_QUERY,
				String.class, key, speciesId);
		if (sciNames.size() != 1) {
			ReportManager.problem(this, dbre.getConnection(),
					"Expect exactly one name for key " + key
							+ " for species ID " + speciesId);
			return null;
		} else {
			return sciNames.get(0);
		}
	}

	private boolean checkNames(DatabaseRegistryEntry dbre,
			SqlTemplate template, int speciesId) {
		boolean passes = true;

		List<String> aliases = template.queryForDefaultObjectList(META_QUERY,
				String.class, ALIAS, speciesId);
		String sciName = getName(dbre, template, speciesId, SCI_NAME);
		String binomialName = null;
		if (dbre.isMultiSpecies()) {
			binomialName = TestCaseUtils.getBinomialNameMulti(template,
					speciesId);
			String dbName = getName(dbre, template, speciesId, DB_NAME);
			if (!binomialName.equals(sciName) && !binomialName.equals(dbName)
					&& !aliases.contains(binomialName)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"There should be one " + ALIAS + " or " + DB_NAME
								+ " meta value that matches name '"
								+ binomialName + "' for species " + speciesId);
			}
		} else {
			binomialName = TestCaseUtils.getBinomialName(template, speciesId);
			if (!aliases.contains(binomialName)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(), "No " + ALIAS
						+ " meta value found that matches name '"
						+ binomialName + "' for species " + speciesId);
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"INSERT INTO "
										+ dbre.getName()
										+ ".meta(species_id,meta_key,meta_value) VALUES("
										+ speciesId + ",'species.alias','"
										+ binomialName + "');");
			}
		}

		String productionName = org.ensembl.healthcheck.util.CollectionUtils.getFirstElement(template.queryForDefaultObjectList(META_QUERY,
				String.class, PRODUCTION_NAME, speciesId),null);
		if (StringUtils.isEmpty(productionName)) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Meta value for "
					+ PRODUCTION_NAME + " is not set for species " + speciesId);
		} else if (!VALID_PRODUCTION_NAME.matcher(productionName).matches()) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Meta value "
					+ productionName + " for key  " + PRODUCTION_NAME
					+ " does not match the required value for species "
					+ speciesId);
		} else if (INVALID_PRODUCTION_NAME2.matcher(productionName).matches()) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Meta value "
					+ productionName + " for key  " + PRODUCTION_NAME
					+ " does not match the required value for species "
					+ speciesId);
		}

		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Tests whether species are correctly named for Ensembl Genomes";
	}

}
