package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Supergroup of tests for Ensembl Genomes (incorporates {@link EGCoreGeneModel}
 * , {@link EGCoreMeta}, {@link EGCoreAnnotation} and {@link EGCoreCompare})
 * 
 * @author dstaines
 * 
 */
public class EGCore extends GroupOfTests {

	public EGCore() {
		addTest(new EGCoreGeneModel());
		addTest(new EGCoreMeta());
		addTest(new EGCoreAnnotation());
		addTest(new EGCoreCompare());
		addTest(new EGCommon());
	}

}
