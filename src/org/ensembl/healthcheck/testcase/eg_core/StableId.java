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
