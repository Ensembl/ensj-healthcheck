/**
 * File: AbstractRowCountTestCase.java
 * Created by: dstaines
 * Created on: Mar 8, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TemplateBuilder;

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
		if(testValue(count)) {
			return true;
		} else {
			ReportManager.problem(this, dbre.getConnection(), getErrorMessage(count));
			return false;
		}
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
	
	protected abstract String getErrorMessage(int value);

}
