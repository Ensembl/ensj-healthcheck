package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;

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

	public EGCompareCoreSchema() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		addAppliesToType(DatabaseType.CORE);
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
	
	@Override
	protected String getDefinitionFileKey() {
		return "schema.file";
	}
	
	@Override
	protected String getMasterSchemaKey() {
		return "master.schema";
	}
}
