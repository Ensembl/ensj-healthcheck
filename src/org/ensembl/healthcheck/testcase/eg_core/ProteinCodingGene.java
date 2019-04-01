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
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to check if we have at least 1 protein coding gene per species
 * 
 * @author dstaines
 * 
 */
public class ProteinCodingGene extends AbstractEgCoreTestCase {

	public ProteinCodingGene() {
		super();
		removeAppliesToType(DatabaseType.OTHERFEATURES);
	}

	private final static String QUERY = "select count(*) from gene "
			+ "join seq_region sr using (seq_region_id) "
			+ "join coord_system cs using (coord_system_id) "
			+ "where biotype='protein_coding' and cs.species_id=?";

	/*
	 * (non-Javadoc)
	 * 
	 * @seeuk.ac.ebi.proteome.genomebuilder.genomeloader.healthcheck.egtests.
	 * AbstractEgTestCase#run(org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean found = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			// check we have at least 1 protein coding gene per species
			int geneN = getTemplate(dbre).queryForDefaultObject(QUERY,
					Integer.class, speciesId);
			if (geneN == 0) {
				found = false;
				ReportManager.problem(this, dbre.getConnection(), "Species "
						+ speciesId + " has no protein coding genes");
			}
		}
		return found;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#
	 * getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check if we have at least 1 protein coding gene per species";
	}

}
