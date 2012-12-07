package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.eg_core.ControlledTableAttribType;
import org.ensembl.healthcheck.testcase.eg_core.ControlledTableExternalDb;

public class ControlledCoreTables extends GroupOfTests {

	public ControlledCoreTables() {
			
		addTest(
			ControlledTableExternalDb.class,
			ControlledTableAttribType.class
		);		
	}
}
