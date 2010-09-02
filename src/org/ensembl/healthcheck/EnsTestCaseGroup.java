/**
 * File: EnsTestCaseGroup.java
 * Created by: dstaines
 * Created on: Mar 16, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck;

import java.util.Collection;

import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Interface specifying a group of testcases
 * @author dstaines
 */
public interface EnsTestCaseGroup {

	public Collection<Class<EnsTestCase>> getTestCases();

	public String getName();
	
}
