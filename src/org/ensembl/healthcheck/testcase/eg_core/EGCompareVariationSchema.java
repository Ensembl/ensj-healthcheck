package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Test for correctness of variation schemas, suggests a patch, if the schemas 
 * differ.
 * </p>
 *
 */
public class EGCompareVariationSchema extends EGAbstractCompareSchema {

	public EGCompareVariationSchema() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.VARIATION);
	}

	@Override
	protected String getDefinitionFileKey() {
		return "variation_schema.file";
	}

	@Override
	protected String getMasterSchemaKey() {
		return "master.variation_schema";
	}

	@Override
	protected boolean assertSchemaCompatibility(
			Connection masterCon,
			Connection checkCon
	) {		
		return 
			assertSchemaTypesCompatible(masterCon, checkCon)
			&& assertSchemaVersionCompatible(masterCon, checkCon)
		;
	}
}
