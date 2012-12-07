package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableGenomeDb extends AbstractControlledTable {
	@Override protected String getControlledTableName() {
		return "genome_db";	
	}
}
