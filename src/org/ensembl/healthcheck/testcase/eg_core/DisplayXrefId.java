/**
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to find genes where display_xref_id is not set
 * 
 * @author dstaines
 * 
 */
public class DisplayXrefId extends AbstractRowCountTestCase {

	public DisplayXrefId() {
		super();
		addToGroup(AbstractEgCoreTestCase.EG_GROUP);
	}

	private final static String QUERY = "select count(*) from gene where status<>'NOVEL' and display_xref_id is null";

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
		return "$actual$ genes with non-novel status found with null display_xref_id";
	}
	

}
