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
		addTest(
			EGCoreGeneModel.class,
			EGCoreMeta.class,
			EGCoreAnnotation.class,
			EGCoreCompare.class,
			EGCommon.class,
			EGCoreMulti.class
		);
	}
}
