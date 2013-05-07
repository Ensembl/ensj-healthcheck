package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbCompareNames;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbSpeciesNames;
import org.ensembl.healthcheck.testcase.eg_core.MultiDbStableId;


/**
 * Set of tests to compare multiple core databases against each other
 * 
 * @author dstaines
 * 
 */
public class EGCoreMulti extends GroupOfTests {

	public EGCoreMulti() {
		addTest(MultiDbSpeciesNames.class, MultiDbStableId.class, MultiDbCompareNames.class);
	}

}
