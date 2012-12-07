package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableMethodLink extends AbstractControlledTable {
	@Override protected String getControlledTableName() {
		return "method_link";	
	}
}
