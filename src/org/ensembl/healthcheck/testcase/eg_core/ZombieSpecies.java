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
 * Test to find where species have been marked for death by metakey but still survive
 * @author dstaines
 *
 */
public class ZombieSpecies extends AbstractEgCoreTestCase {

	private final static String QUERY = "select species_id from meta where meta_key='schema.action' and meta_value='delete'";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		for(String id: getTemplate(dbre).queryForDefaultObjectList(QUERY, String.class)) {
			passes= false;
			ReportManager.problem(this, dbre.getConnection(), "Species "+id+" has been marked for death by schema.action=delete but is still alive");
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find where species have been marked for death by metakey but still survive";
	}

}
