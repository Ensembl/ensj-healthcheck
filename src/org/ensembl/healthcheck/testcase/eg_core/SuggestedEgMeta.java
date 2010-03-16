/**
 * File: EgMeta.java
 * Created by: dstaines
 * Created on: Mar 2, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

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
				resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/meta_keys.txt"));
	}

}
