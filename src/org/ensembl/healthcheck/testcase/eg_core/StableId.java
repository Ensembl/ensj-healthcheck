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
 * Check to see if stable_ids have been set for each species in turn
 * 
 * @author dstaines
 * 
 */
public class StableId extends AbstractEgCoreTestCase {

	private final static String QUERY = "select g.gene_id from gene g "
			+ "join seq_region sr using (seq_region_id) "
			+ "join coord_system cs using (coord_system_id) "
			+ "where g.stable_id is null and cs.species_id=?";

	/*
	 * (non-Javadoc)
	 * 
	 * @seeuk.ac.ebi.proteome.genomebuilder.genomeloader.healthcheck.egtests.
	 * AbstractEgTestCase#run(org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			for (String id : getTemplate(dbre).queryForDefaultObjectList(QUERY,
					String.class,speciesId)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(), "Gene " + id
						+ " has no stable ID");
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Check to see if stable_ids have been set for each species in turn";
	}
}
