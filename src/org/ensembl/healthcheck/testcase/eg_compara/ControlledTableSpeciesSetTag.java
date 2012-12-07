package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableSpeciesSetTag extends AbstractControlledTable {
	@Override protected String getControlledTableName() {
		return "species_set_tag";	
	}
}
