/**
 * File: EgMeta.java
 * Created by: dstaines
 * Created on: Mar 2, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * @author dstaines
 * 
 */
public class RequiredEgMeta extends AbstractEgMeta {

	/**
	 * @param metaKeys
	 */
	public RequiredEgMeta() {
		super(
				TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/required_meta_keys.txt"));
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Checks that we have the minimal set of metakeys";
	}

}
