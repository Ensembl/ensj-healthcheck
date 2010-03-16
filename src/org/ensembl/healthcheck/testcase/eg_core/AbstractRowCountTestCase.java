/**
 * File: AbstractRowCountTestCase.java
 * Created by: dstaines
 * Created on: Mar 8, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * @author dstaines
 * 
 */
public abstract class AbstractRowCountTestCase extends AbstractEgCoreTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		int count = getTemplate(dbre).queryForDefaultObject(getSql(),
				Integer.class);
		boolean passes = true;
		int expected = getExpectedCount();
		if (count != expected) {
			ReportManager.problem(this, dbre.getConnection(),
					"Count not correct: expected " + expected + " but got "
							+ count);
			passes = false;
		}
		return passes;
	}

	protected abstract int getExpectedCount();

	protected abstract String getSql();

}
