package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * @author mnuhn
 * 
 * Abstract class providing method "getSpeciesAssemblyDataFileBasePath". This 
 * can be used to write checks for external files.
 *
 */
public abstract class AbstractExternalFileUsingTestcase extends AbstractCoreDatabaseUsingTestCase {
	
	protected String getSpeciesAssemblyDataFileBasePath(DatabaseRegistryEntry dbre) {
		
		Connection coreConnection = getCoreDb(dbre).getConnection();
			
		String productionName = getProductionName(coreConnection);
		String assemblyName   = getAssembly(coreConnection);
		String dbFileRootDir  = getDataFileBasePath();
		
		if (StringUtils.isEmpty(dbFileRootDir)) {
			ReportManager.problem(this, dbre.getConnection(), "datafile_base_path has not been set!");
		}
		String speciesAssemblyDataFileBasePath = dbFileRootDir + "/" + productionName + "/" + assemblyName;
		
		return speciesAssemblyDataFileBasePath;
	}
}
