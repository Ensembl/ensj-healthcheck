/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to see if description is empty (skip pseudogenes)
 * 
 * @author dstaines
 * 
 */
public class GeneDescription extends AbstractRowCountTestCase {

	public GeneDescription() {
		super();
		addToGroup(AbstractEgCoreTestCase.EG_GROUP);
	}

	private final static String QUERY = "select count(*) from gene where description is null and biotype not in ('pseudogene')";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
	 * ()
	 */
	@Override
	protected int getExpectedCount() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return QUERY;
	}
	
	@Override
	protected String getErrorMessage() {
		return "$actual$ genes found with null descriptions";
	}

}
