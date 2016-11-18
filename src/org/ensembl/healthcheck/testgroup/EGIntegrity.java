package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Group containing tests that need to be flagged in the production monitor
 * 
 * @author dstaines
 *
 */
public class EGIntegrity extends GroupOfTests {

	public EGIntegrity() {
		addTest(EGCoreIntegrity.class);
		addTest(EGVariationHandover.class);
	}

}
