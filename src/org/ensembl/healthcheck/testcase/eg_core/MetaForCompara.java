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

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks that the genebuild start date meta key is set, and checks
 * that it matches the version of the lowest ranking coordinate system
 * @author dstaines, dbolser
 * 
 */
public class MetaForCompara extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value from meta where meta_key=? and species_id=?";
	private final static String COORD_VERSION_QUERY = "select version from coord_system where rank=(SELECT MIN(rank) FROM coord_system where species_id=?) and species_id=?";
	private static final String GB_START = "genebuild.start_date";
	private static final String ASS_DEF = "assembly.default";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate template = getTemplate(dbre);
		boolean passes = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			// 2. check genebuild start date is set
			passes &= checkGeneBuild(dbre, template, speciesId);
			// 3. check that assembly.default matches version for rank 1
			// coordsystem
			passes &= checkAssembly(dbre, template, speciesId);
		}
		return passes;
	}

	/**
	 * @param dbre
	 * @param template
	 * @return
	 */
	private boolean checkAssembly(DatabaseRegistryEntry dbre,
			SqlTemplate template, int speciesId) {
		boolean passes = true;
		// 1. get assembly.default
		String assDef = template.queryForDefaultObject(META_QUERY,
				String.class, ASS_DEF, speciesId);
		if (StringUtils.isEmpty(assDef)) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Meta value for "
					+ ASS_DEF + " is not set for species " + speciesId);
		}
		if (passes) {
			// 2. check to see if it matches the default coord_system version
			String version = template.queryForDefaultObject(
					COORD_VERSION_QUERY, String.class, speciesId, speciesId);
			if (!assDef.equals(version)) {
				passes = false;
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"Meta value for "
										+ ASS_DEF
										+ " ("
										+ assDef
										+ ") does not match min rank coord_system version "
										+ version + " for species " + speciesId);
			}
		}
		return passes;
	}

	/**
	 * @param dbre
	 * @param template
	 * @return
	 */
	private boolean checkGeneBuild(DatabaseRegistryEntry dbre,
			SqlTemplate template, int speciesId) {
		boolean passes = true;
		String gbStart = template.queryForDefaultObject(META_QUERY,
				String.class, GB_START, speciesId);
		if (StringUtils.isEmpty(gbStart)) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Meta value for "
					+ GB_START + " is not set ");
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return " Checks that the genebuild start date meta key is set, and checks that it matches the version of the lowest ranking coordinate system";
	}

}
