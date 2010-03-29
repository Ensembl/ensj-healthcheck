/**
 * File: AbstractRowCountTestCase.java
 * Created by: dstaines
 * Created on: Mar 8, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase;

/**
 * Base class for testing number returned by a query
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractRowCountTestCase extends AbstractIntegerTestCase {

	public AbstractRowCountTestCase() {
		super();
	}

	/**
	 * @return number that the rowcount should return
	 */
	protected abstract int getExpectedCount();

	@Override
	protected boolean testValue(int value) {
		return getExpectedCount() == value;
	}

}
