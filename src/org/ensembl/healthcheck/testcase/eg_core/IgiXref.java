/**
 * File: IgiXrefTest.java
 * Created by: dstaines
 * Created on: Dec 3, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to see if IGIs have been accidentally left in
 * 
 * @author dstaines
 * 
 */
public class IgiXref extends AbstractEgCoreTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int n = getTemplate(dbre).queryForDefaultObject(
				"Select count(*) from xref where external_db_id=60014",
				Integer.class);
		if (n > 0) {
			ReportManager
					.problem(this, dbre.getConnection(), "IGI xrefs found");
		}
		return passes;
	}

	@Override
	public boolean canRepair() {
		return true;
	}

	@Override
	public String getFix() {
		return "delete ox.*,x.* from object_xref ox, xref x where x.xref_id=ox.xref_id and x.external_db_id=60014;";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#
	 * getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if IGIs have been accidentally left in";
	}

}
