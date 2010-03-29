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
public class SuggestedEgMeta extends AbstractEgMeta {

	/**
	 * @param metaKeys
	 */
	public SuggestedEgMeta() {
		super(
				TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/meta_keys.txt"));
	}

}
