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
 * Test to find genes where display_xref_id is not set
 * 
 * @author dstaines
 * 
 */
public class DisplayXrefId extends AbstractEgCoreTestCase {

	private final static String QUERY = "select gene_id from gene where display_xref_id is null";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		for (String id : getTemplate(dbre).queryForDefaultObjectList(QUERY,
				String.class)) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), "Gene " + id
					+ " has no display xref ID");
		}
		return passes;
	}

}
