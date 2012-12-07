package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableAttribType extends AbstractControlledTable {

	@Override protected String getControlledTableName() {
		return "attrib_type";
	}

	protected DatabaseRegistryEntry getMasterDatabase() {
		return getProductionDatabase();
	}
}
