package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Supergroup of tests for Ensembl Genomes meta data (incorporates
 * {@link EGCoreMetaCritical} and {@link EGCoreMetaMajor})
 * 
 * @author dstaines
 * 
 */
public class EGCoreMeta extends GroupOfTests {

	public EGCoreMeta() {
		addTest(new EGCoreMetaCritical());
		addTest(new EGCoreMetaMajor());
	}

}
