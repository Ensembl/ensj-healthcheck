package org.ensembl.healthcheck.testcase.eg_funcgen;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.funcgen.ArrayXrefs;

public class EGArrayXrefs extends ArrayXrefs {

	public EGArrayXrefs() {
		super();
	}

	@Override
	protected String getCoreDbName(DatabaseRegistryEntry dbre,
			String schemaBuild) {
		return dbre.getName().replace("_funcgen_", "_core_");
	}
	
	

}
