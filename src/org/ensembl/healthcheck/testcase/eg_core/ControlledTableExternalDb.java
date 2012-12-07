package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.AbstractControlledTable;

public class ControlledTableExternalDb extends AbstractControlledTable {

	@Override protected String getControlledTableName() {
		return "external_db";
	}

	protected DatabaseRegistryEntry getMasterDatabase() {
		return getProductionDatabase();
	}
}
