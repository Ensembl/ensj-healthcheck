package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Supergroup of tests for Ensembl Genomes gene models (incorporates
 * {@link EGGeneModelCritical} and {@link EGGeneModelMajor})
 * 
 * @author dstaines
 * 
 */
public class EGGeneModel extends GroupOfTests {

	public EGGeneModel() {
		addTest(new EGGeneModelCritical());
		addTest(new EGGeneModelMajor());
	}

}
