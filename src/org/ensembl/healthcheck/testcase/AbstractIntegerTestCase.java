/**
 * File: AbstractRowCountTestCase.java
 * Created by: dstaines
 * Created on: Mar 8, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * Base class for testing number returned by a query against an exact expected
 * value
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractIntegerTestCase extends AbstractTemplatedTestCase {

	public AbstractIntegerTestCase() {
		super();
	}

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
		return testValue(count);
	}

	/**
	 * @return query that returns a number
	 */
	protected abstract String getSql();

	/**
	 * Method to test whether number meets criteria
	 * 
	 * @param value
	 *            number produced by SQL to test
	 * @return true if number meets criteria for success
	 */
	protected abstract boolean testValue(int value);

}
