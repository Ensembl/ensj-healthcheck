package org.ensembl.healthcheck.testcase.eg_core;

import static org.ensembl.healthcheck.DatabaseType.CDNA;
import static org.ensembl.healthcheck.DatabaseType.CORE;
import static org.ensembl.healthcheck.DatabaseType.EST;
import static org.ensembl.healthcheck.DatabaseType.ESTGENE;
import static org.ensembl.healthcheck.DatabaseType.OTHERFEATURES;
import static org.ensembl.healthcheck.DatabaseType.RNASEQ;
import static org.ensembl.healthcheck.DatabaseType.SANGER_VEGA;

import java.sql.Connection;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;

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

	/**
	 * @param masterCon
	 * @param checkCon
	 * 
	 * <p>
	 * 	Checks, if the schema versions of the two databases are identical. If
	 * not, it will report this as a problem to the ReportManager.
	 * </p>
	 * <p>
	 * 	Returns true or false depending on whether or not the schema versions
	 * were identical.
	 * </p>
	 * 
	 */
	protected boolean assertSchemaCompatibility(
			Connection masterCon, 
			Connection checkCon
	) {
		String sql = "SELECT meta_value FROM meta WHERE meta_key='schema_version'";
		String schemaVersionCheck  = DBUtils.getRowColumnValue(checkCon, sql);
		String schemaVersionMaster = DBUtils.getRowColumnValue(masterCon, sql);
		
		if (!schemaVersionCheck.equals(schemaVersionMaster)) {

			String checkShortName = DBUtils.getShortDatabaseName(checkCon);
			
			logger.severe(
				"Schema versions in " + checkShortName + " and the master "
				+ "database differ. The test will be aborted."
			);
			
			ReportManager.problem(this, checkCon, 

					  "Database version error: You are comparing " 
					+ checkShortName + " which has a version " + schemaVersionCheck 
					+ " schema with a version " + schemaVersionMaster + " schema.\n"
					+ "Please ensure the version of the database you are "
					+ "checking is the same as the version of the schema to "
					+ "which you are comparing and rerun the test."

			);
			return false;
		}
		logger.info("Schemas are compatible.");
		return true;
	}
	
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
