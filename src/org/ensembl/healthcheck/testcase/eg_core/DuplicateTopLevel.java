/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to see if we have more than one seq_region_attrib
 * 
 * @author dstaines
 * 
 */
public class DuplicateTopLevel extends AbstractRowCountTestCase {

	public DuplicateTopLevel() {
		super();
		addToGroup(AbstractEgCoreTestCase.EG_GROUP);
	}

	private final static String QUERY = "select count(*) from (select count(*) as c "
			+ "from seq_region_attrib join attrib_type using (attrib_type_id) "
			+ "where code='toplevel' group by seq_region_id,attrib_type_id having count(*)>1) as a";

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
		return "$actual$ seq regions found with duplicate top level attribs";
	}

}
