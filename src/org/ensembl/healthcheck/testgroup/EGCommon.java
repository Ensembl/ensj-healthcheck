package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.SeqRegionLength;
import org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine;

public class EGCommon extends GroupOfTests {

	public EGCommon() {
		addTest(
			MySQLStorageEngine.class,
			SeqRegionLength.class
		);
	}

}
