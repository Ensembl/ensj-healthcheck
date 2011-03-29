package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Supergroup of tests for Ensembl Genomes annotation (incorporates
 * {@link EGCoreAnnotationCritical} and {@link EGCoreAnnotationMajor})
 * 
 * @author dstaines
 * 
 */
public class EGCoreAnnotation extends GroupOfTests {

	public EGCoreAnnotation() {
		addTest(
			EGCoreAnnotationCritical.class,
			EGCoreAnnotationMajor.class
		);
	}
}
