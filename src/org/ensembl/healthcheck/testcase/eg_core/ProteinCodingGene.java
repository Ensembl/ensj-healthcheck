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
 * Test to check if we have at least 1 protein coding gene per species
 * 
 * @author dstaines
 * 
 */
public class ProteinCodingGene extends AbstractEgCoreTestCase {

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

}
