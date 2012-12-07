package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableMappingSession extends AbstractControlledTable {
	@Override protected String getControlledTableName() {
		return "mapping_session";	
	}
}
