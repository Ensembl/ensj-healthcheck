package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Supergroup of tests for Ensembl Genomes gene models (incorporates
 * {@link EGCoreGeneModelCritical} and {@link EGCoreGeneModelMajor})
 * 
 * @author dstaines
 * 
 */
public class EGCoreGeneModel extends GroupOfTests {

	public EGCoreGeneModel() {
		addTest(
			EGCoreGeneModelCritical.class,
			EGCoreGeneModelMajor.class
		);
	}
}
