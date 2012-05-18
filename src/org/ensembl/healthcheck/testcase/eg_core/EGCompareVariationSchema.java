package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.util.DBUtils;

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
	protected boolean assertSchemaCompatibility(Connection masterCon,
			Connection checkCon) {
		
		String sql = "SELECT meta_value FROM meta WHERE meta_key='schema_type'";
		String schemaVersionCheck  = DBUtils.getRowColumnValue(checkCon, sql);
		String schemaVersionMaster = DBUtils.getRowColumnValue(masterCon, sql);
		
		if (!schemaVersionCheck.equals("variation")) {
			
			ReportManager.problem(this, checkCon,
				"Database schema type error: The schema type of your database "
				+ "is not variation." 
			);
			return false;
		}
		if (!schemaVersionMaster.equals("variation")) {
			
			ReportManager.problem(this, checkCon,
				"Database schema type error: The schema type of the master "
				+ "database is not variation." 
			);
			return false;
		}
		return true;
	}
}
