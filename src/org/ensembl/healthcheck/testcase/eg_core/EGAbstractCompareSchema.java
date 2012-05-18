package org.ensembl.healthcheck.testcase.eg_core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.SystemCommand;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema;
import org.ensembl.healthcheck.util.ActionAppendable;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Abstract class from which EGCompareSchema tests can inherit. Uses 
 * mysqldiff, which will suggest a patch file, if the schemas differ from one 
 * another.
 * </p>
 */
public abstract class EGAbstractCompareSchema extends MultiDatabaseTestCase {

	protected final String mysqldiffBin = "mysqldiff";
	
	public boolean isDoSchemaVersionCheck() {
		return doSchemaCompatibilityChecks;
	}

	public void setDoSchemaVersionCheck(boolean doSchemaVersionCheck) {
		this.doSchemaCompatibilityChecks = doSchemaVersionCheck;
	}

	public boolean isTolerant() {
		return tolerant;
	}

	public void setTolerant(boolean tolerant) {
		this.tolerant = tolerant;
	}

	protected boolean doSchemaCompatibilityChecks = true;
	protected boolean tolerant;
	
	public EGAbstractCompareSchema() {

		tolerant = true;
		doSchemaCompatibilityChecks = true;
	}
	
	/**
	 * Should return the property key used to locate a target schema file
	 */
	protected abstract String getDefinitionFileKey();

	/**
	 * Should return the property key used to locate a target master schema
	 */
	protected abstract String getMasterSchemaKey();
	
	/**
	 * @param compareSchemaInstance
	 * <p>
	 * 	Returns a concrete CompareSchemaStrategy which will compare schemas.
	 * Depending on how the healthchecks are configured, this can be using
	 * a schema file or a master database.
	 * </p>
	 */
	protected CompareSchemaStrategy createCompareSchemaStrategy(
			EGAbstractCompareSchema compareSchemaInstance
	) {
			String definitionFileKey = getDefinitionFileKey();
			String masterSchemaKey   = getMasterSchemaKey();
			
			String definitionFile = System.getProperty(definitionFileKey);			
			definitionFile = System.getProperty(definitionFileKey);
			
			if (definitionFile == null) {
				
				logger.info(
			        "No schema definition file found! Set "
			        + definitionFileKey
			        + " property in "
			        + TestRunner.getPropertiesFile() 
			        + " if you want to use a table.sql file or similar. "
			        + "This is not an error if you are using "
			        + masterSchemaKey);
				
				String masterSchema = System.getProperty(masterSchemaKey);
				
				return new CompareToMasterSchema(compareSchemaInstance, masterSchema);
				
			} else {
				return new CompareToSchemaFile(compareSchemaInstance, definitionFile);
			}
	}
	
	/**
	 * @param masterCon
	 * @param checkCon
	 * 
	 * <p>
	 * 	Checks, if the schemas that will be compared are compatible with one
	 * another. In core databases the relevant information will be in the 
	 * schema_type and schema_version entries of the meta table, in variation
	 * and funcgen schemas there is only the schema_type.
	 * </p>
	 * 
	 */
	abstract protected boolean assertSchemaCompatibility(
			Connection masterCon, 
			Connection checkCon
	);
	
	public boolean run(DatabaseRegistry dbr) {
		
		boolean result = true;
		boolean somethingWasChecked = false;
		
		CompareSchemaStrategy compareSchemaStrategy = createCompareSchemaStrategy(this);

		SystemCommand systemCommand = new SystemCommand();

		// If mysqldiff can't be found, the test can terminate right away.
		//
		if (!systemCommand.checkCanExecute(mysqldiffBin)) {

			ReportManager.problem(this, (Connection) null, 
					"Can't find mysqldiff! "
					+ this.getShortTestName() + " relies on this program to "
					+ "compare database schemas.\n"
					+ "Please ensure it is on your path. If it is not "
					+ "installed, it should be installable via CPAN with the "
					+ "command \"cpan MySQL::Diff\"."
			);
			result = false;
			return result;
		}

		Connection masterCon = compareSchemaStrategy.buildMasterConnection();
		String masterShortName = DBUtils.getShortDatabaseName(masterCon);

		final EGAbstractCompareSchema compareSchemaTest = this;
		DatabaseRegistryEntry[] databases = dbr.getAll();
		
		for (final DatabaseRegistryEntry dbre : databases) {
			
			DatabaseType type = dbre.getType();

			if (!appliesToType(type)) { continue; }

			final Connection checkCon = dbre.getConnection();
			if (checkCon == masterCon) { continue; }
			
			logger.info("Checking schema of " + dbre.getName());
			
			if (
				doSchemaCompatibilityChecks 
				&& !assertSchemaCompatibility(masterCon, checkCon)
			) {
				result = false;
				continue;
			}
		
			DatabaseServer srv = dbre.getDatabaseServer();
			final StringBuffer patch = new StringBuffer();
			
			logger.info("Running " + mysqldiffBin);
			
			systemCommand.runCmd(
				new String[] {
						mysqldiffBin, 
						"--tolerant",
						"--host", srv.getHost(),
						"--port", srv.getPort(),
						"--user", srv.getUser(),
						"--password", srv.getPass(),
						"db:" + dbre.getName(),
						"db:" + masterShortName
				}, 
				new ActionAppendable() {
					@Override public void process(String message) {
						patch.append(message);
					}
				}, 
				new ActionAppendable() {
					@Override public void process(String message) {
						ReportManager.problem(compareSchemaTest, checkCon, message);
					}
				}
			);
			
			logger.info("Done running " + mysqldiffBin);
			
			boolean schemasAreEqual = patch.toString().trim().equals("");
			somethingWasChecked = true;
			
			if (schemasAreEqual) {
				ReportManager.correct(
					compareSchemaTest, 
					checkCon, 
					"The schema of " + dbre.getName() + " is correct."
				);
				continue;
			}
			
			logger.info("Found schema differences.");
			
			String patchFileNameBase = "schema_patch_from_"+compareSchemaTest.getShortTestName()+".sql";
			String patchFileDir      = "external_reports/" + dbre.getName();
			
			new File(patchFileDir).mkdirs();
			
			File patchFile = new File(patchFileDir + File.separatorChar + patchFileNameBase); 
			
			// Mysqldiff will insert the name of the master database into the
			// report. If a temporary database was used, the name will be 
			// different during every run. This will cause problems in the
			// web interface, which assumes that the exact same error is
			// given for the same problem every time.
			//
			// There fore the name of the master database is replaced with the
			// constant string "master_database" here.
			//
			String patchedPatch = patch.toString().replaceAll(masterShortName, "master_database");
			
			ReportManager.problem(compareSchemaTest, checkCon, 
					"\n"
					+ "\nDifferences between the two schemas were found. The "
					+ "following sql commands would patch the schema of your "
					+ "database to match the one of the master database:\n"
					+ "\n"
					+ "\n-----------------------------------\n"
					+ patchedPatch
					+ "\n-----------------------------------\n"
			);
			try {
				
				logger.info("Storing patch file in " + patchFile.getCanonicalPath());
				
				PrintWriter out = new PrintWriter(patchFile);
				out.println(patchedPatch);
				out.close();
				
				ReportManager.problem(compareSchemaTest, checkCon, 
						  "\nA patch file with the commands shown above has been written to:\n"
						+ patchFile.getCanonicalPath()
						+ "\n\n"
				);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		if (!somethingWasChecked) {
			
			// Depending on the users configuration this doesn't have to be an
			// error, but most of the time it will be a misconfiguration.
			//
			ReportManager.correct(
				compareSchemaTest, 
				masterCon, 
				"Warning: Nothing was compared."
			);
		}
		
		compareSchemaStrategy.cleanup();
		return result;
	}
	
	protected boolean assertSchemaTypesCompatible(
			Connection masterCon,
			Connection checkCon
	) {
		
		String sql = "SELECT meta_value FROM meta WHERE meta_key='schema_type'";
		String schemaVersionCheck  = DBUtils.getRowColumnValue(checkCon, sql);
		String schemaVersionMaster = DBUtils.getRowColumnValue(masterCon, sql);
		
		if (!schemaVersionCheck.equals(schemaVersionMaster)) {
			
			ReportManager.problem(this, checkCon,
				"Database schema type error: The schema type of your database "
				+ "is not that of the database checked." 
			);
			return false;
		}
		return true;
	}
	
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
	protected boolean assertSchemaVersionCompatible(
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
		logger.info("Good: Schema versions are the same.");
		return true;
	}
}

/**
 * @author mnuhn
 * 
 * <p>
 * 	Concrete implementations of this implement specific ways on comparing
 * schemas depending on which way the user has chosen.
 * </p>
 */
abstract class CompareSchemaStrategy {
	
	protected final EGAbstractCompareSchema compareSchemaInstance;
	protected Connection masterCon;
	protected Logger logger;
	
	public CompareSchemaStrategy(EGAbstractCompareSchema compareSchemaInstance) {
		this.compareSchemaInstance = compareSchemaInstance;
		this.logger = compareSchemaInstance.getLogger();
	}
	protected abstract Connection buildMasterConnection();
	protected abstract void cleanup();
}

/**
 * @author mnuhn
 * 
 * <p>
 * 	Methods for comparing to a schema file.
 * </p>
 */

class CompareToSchemaFile extends CompareSchemaStrategy {
	
	protected String definitionFile;
	
	public CompareToSchemaFile(EGAbstractCompareSchema compareSchemaInstance, String definitionFile) {
		super(compareSchemaInstance);
		logger.fine("Will use schema definition from " + definitionFile);
		this.definitionFile = definitionFile;
	}
	
	protected Connection buildMasterConnection() {
		
		logger.info("About to import " + definitionFile);
		try {
			masterCon = compareSchemaInstance.importSchema(definitionFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		logger.info("Got connection to "
		    + DBUtils.getShortDatabaseName(masterCon));
		return masterCon;
	}
	
	protected void cleanup() {
		String dbName = DBUtils.getShortDatabaseName(masterCon);
		
		if (dbName.indexOf("_temp_") > -1) {
			compareSchemaInstance.removeDatabase(masterCon);
			logger.info("Removed " + DBUtils.getShortDatabaseName(masterCon));
		}
	}
}

/**
 * @author mnuhn
 * 
 * <p>
 * 	Methods for comparing to a master schema.
 * </p>
 */
class CompareToMasterSchema extends CompareSchemaStrategy {	

	protected String masterSchema;
	
	public CompareToMasterSchema(EGAbstractCompareSchema compareSchemaInstance, String masterSchema) {
		super(compareSchemaInstance);
		logger.fine("Will master schema: " + masterSchema);
		this.masterSchema = masterSchema;
	}
	
	protected Connection buildMasterConnection() {
		
		masterCon = compareSchemaInstance.getSchemaConnection(masterSchema);
		
		logger.fine("Opened connection to master schema in "
			    + DBUtils.getShortDatabaseName(masterCon));
		
		return masterCon;
	}
	protected void cleanup() {}		
	
}
