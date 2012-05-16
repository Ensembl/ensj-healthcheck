package org.ensembl.healthcheck.testcase.eg_core;

import static org.ensembl.healthcheck.DatabaseType.CDNA;
import static org.ensembl.healthcheck.DatabaseType.CORE;
import static org.ensembl.healthcheck.DatabaseType.EST;
import static org.ensembl.healthcheck.DatabaseType.ESTGENE;
import static org.ensembl.healthcheck.DatabaseType.OTHERFEATURES;
import static org.ensembl.healthcheck.DatabaseType.RNASEQ;
import static org.ensembl.healthcheck.DatabaseType.SANGER_VEGA;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Test for correctness of core schemas, suggests a patch, if the schemas 
 * differ.
 * </p>
 *
 */
public class EGCompareCoreSchema extends EGAbstractCompareSchema {

	@Override
	protected String getDefinitionFileKey() {
		return "schema.file";
	}
	
	@Override
	protected String getMasterSchemaKey() {
		return "master.schema";
	}

	@Override
	public void types() {
		addAppliesToType(CORE);
		addAppliesToType(CDNA);
		addAppliesToType(EST);
		addAppliesToType(ESTGENE);
		addAppliesToType(OTHERFEATURES);
		addAppliesToType(RNASEQ);
		addAppliesToType(SANGER_VEGA);
	}
}
