package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableDnafrag extends AbstractControlledTable {
	@Override protected String getControlledTableName() {
		return "dnafrag";	
	}
}
