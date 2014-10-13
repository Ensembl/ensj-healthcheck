/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.ReporterFactory.ReporterType;
import org.ensembl.healthcheck.TestRegistryFactory.TestRegistryType;
import org.ensembl.healthcheck.configuration.ConfigurationUserParameters;
import org.ensembl.healthcheck.configuration.ConfigureConfiguration;
import org.ensembl.healthcheck.configuration.ConfigureHealthcheckDatabase;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configuration.ConfigureTestGroups;
import org.ensembl.healthcheck.configurationmanager.ConfigurationDumper;
import org.ensembl.healthcheck.configurationmanager.ConfigurationException;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.CreateHealthCheckDB;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * 
 * Runs test which can be configured on the command line and stores them in a
 * database.
 * 
 */
public class ConfigurableTestRunner extends TestRunner {

	/**
	 * 
	 */
	private static final String GET_DIVISION_DBS = "select division_db.db_name from division_db "
			+ "join division using (division_id) where division_db.is_current=1 and (division.name=? or division.shortname=?)";

	/**
	 * 
	 */
	private static final String GET_DIVISION_SPECIES_DBS = "select "
			+ "concat(species.db_name,'_',db.db_type,'_',db.db_release,'_',db.db_assembly) from db "
			+ "join species using (species_id) join division_species using (species_id) "
			+ "join division using (division_id) where db.is_current=1 and species.is_current=1 and (division.name=? or division.shortname=?)";

	static final Logger log = Logger.getLogger(ConfigurableTestRunner.class
			.getCanonicalName());

	/**
	 * Name of a properties file from which parameters can be taken, if they
	 * have not been set by the user anywhere.
	 */
	protected final static String DEFAULT_PROPERTIES_FILE = "database.defaults.properties";

	/**
	 * The configuration object from which configuration information is
	 * retrieved.
	 */
	protected final ConfigurationUserParameters configuration;

	/**
	 * The type of reporter used.
	 */
	protected final ReporterType reporterType;

	/**
	 * The type of test registry used.
	 */
	protected final TestRegistryType testRegistryType;

	protected final SystemPropertySetter systemPropertySetter;

	protected final TestRegistry testRegistry;
	protected final Reporter reporter;

	/**
	 * @param configuration
	 *            - A configuration object of type ConfigurationUserParameters
	 * 
	 *            Creates a ConfigurableTestRunner using the parameters from the
	 *            configuration object.
	 * 
	 */
	public ConfigurableTestRunner(ConfigurationUserParameters configuration) {

		log.config("Using classpath: \n\n" + Debug.classpathToString());

		this.configuration = configuration;
		this.systemPropertySetter = new SystemPropertySetter(configuration);
		this.testRegistryType = getTestRegistryType(configuration
				.getTestRegistryType());
		this.reporterType = getReporterType(configuration.getReporterType());
		this.testRegistry = getTestRegistry(this.testRegistryType,
				configuration);
		this.reporter = getReporter(this.reporterType);

		String repair = configuration.getRepair().toLowerCase();
		this.doRepair = (repair.equals("do") || repair.equals("1") || repair.equals("yes"));
		this.showRepair = (repair.equals("show"));

		DBUtils.setHostConfiguration((ConfigureHost) configuration);
	}

	/**
	 * @param args
	 *            - The command line arguments
	 * 
	 *            Creates a ConfigurableTestRunner using command line arguments.
	 *            The configuration parameters are taken from the command line
	 *            and from the properties files that can be specifies on the
	 *            command line with the --conf option. The
	 *            DEFAULT_PROPERTIES_FILE is added as well.
	 * 
	 */
	public ConfigurableTestRunner(String[] args) {

		this(createConfigurationObj(args));
	}

	/**
	 * Used for created layered constructors.
	 */
	protected static ReporterType getReporterType(String reporterType) {

		ReporterType r;

		try {
			r = ReporterType.valueOf(reporterType.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException(
					"Parameter reportertype has been set to an illegal value: "
							+ reporterType);
		}

		return r;
	}

	/**
	 * Used for created layered constructors.
	 */
	protected static TestRegistryType getTestRegistryType(
			String testRegistryType) {

		TestRegistryType t;

		try {
			t = TestRegistryType.valueOf(testRegistryType);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException(
					"Parameter reportertype has been set to an illegal value: "
							+ testRegistryType);
		}

		return t;
	}

	/**
	 * @return TestRegistry
	 * 
	 *         Creates a TestRegistry. The type of TestRegistry is determined by
	 *         the configuration object.
	 * 
	 */
	protected static TestRegistry getTestRegistry(
			TestRegistryType testRegistryType, ConfigureTestGroups configuration) {

		TestRegistryFactory testRegistryFactory = new TestRegistryFactory(

		// Downcast of configuration, the TestRegistryFactory and the
		// Registries it produces from it only have access to the
		// params in the ConfigureTestGroups interface
		//
				(ConfigureTestGroups) configuration);

		log.config("Using test registry of type: " + testRegistryType);

		TestRegistry testRegistry = null;

		try {
			testRegistry = testRegistryFactory
					.getTestRegistry(testRegistryType);
		} catch (TestRegistryCreationException e) {
			throw new ConfigurationException(e);
		}

		log.config("Using testregistry with this configuration:\n"
				+ testRegistry.toString());

		return testRegistry;
	}

	/**
	 * @return Reporter
	 * 
	 *         Creates a reporter. The type of reporter is determined by the
	 *         configuration object.
	 * 
	 */
	protected static Reporter getReporter(ReporterType reporterType) {

		Reporter reporter = new ReporterFactory().getTestReporter(reporterType);

		log.config("Using reporter of type: " + reporterType);

		return reporter;
	}

	/**
	 * Used for creating layered constructors.
	 */
	protected static ConfigurationUserParameters createConfigurationObj(
			String[] args) {

		// A temporary configuration object for accessing the command line
		// parameters in which the user configures where the configuration
		// files are located. Since only this information is of interest at
		// this point the configuration object is subcast to the
		// ConfigureConfiguration interface.
		//
		ConfigureConfiguration conf = new ConfigurationFactory<ConfigurationUserParameters>(
				ConfigurationUserParameters.class, (Object[]) args)
				.getConfiguration(ConfigurationType.Commandline);

		// Get the list of property files the user specified and add the
		// default property file to it.
		//
		List<File> propertyFileNames = new ArrayList<File>();

		// The user is not required to provide a property file. In this case
		// only the
		// command line arguments and the default properties file will be used.
		//
		if (conf.isConf()) {
			propertyFileNames.addAll(conf.getConf());
		}
		propertyFileNames.add(new File(DEFAULT_PROPERTIES_FILE));

		// Use this to create the final configuration object.
		ConfigurationFactory<ConfigurationUserParameters> confFact = new ConfigurationFactory(
				ConfigurationUserParameters.class, args, propertyFileNames);

		// Users may want to know what went into the the configuration of this,
		// So some information on where the configuration information came
		// from is compiled here and sent to the logger.
		//
		StringBuffer msg = new StringBuffer();

		//
		// Create a few logging messages about the configuration information
		// that that will be used. Useful for debugging.
		//

		// Information about the command line arguments and which property files
		// are used
		msg.append("Creating configuration for this run.\n\n");
		msg.append("The following arguments were specified on the command line:\n");

		for (String arg : args) {
			if (arg.startsWith("-")) {
				msg.append("\n");
			}
			msg.append(" " + arg);
		}

		msg.append("\n\nThe following property files will be used:\n\n");

		for (File propertyFileName : propertyFileNames) {
			msg.append("  - " + propertyFileName.getName() + "\n");
		}

		log.config(msg.toString());

		// Finally create the configuration object.
		ConfigurationUserParameters configuration = confFact
				.getConfiguration(ConfigurationType.Cascading);

		// Show user the final configuration settings that will be used.
		log.config("The following settings will be used:\n\n"
				+ new ConfigurationDumper<ConfigurationUserParameters>()
						.dump(configuration));

		return configuration;
	}

	public static void main(String[] args) {

		try {

			ConfigurableTestRunner configurableTestRunner = new ConfigurableTestRunner(
					args);
			configurableTestRunner.run();

		} catch (ConfigurationException e) {

			ConfigurableTestRunner.logger.log(Level.INFO, e.getMessage());
		}
	}

	protected DatabaseServer connectToDatabase(ConfigureHost conf) {

		DatabaseServer ds = new DatabaseServer(conf.getHost(), conf.getPort(),
				conf.getUser(), conf.getPassword(), conf.getDriver());
		return ds;
	}

	List<Class<? extends EnsTestCase>> getAllRegisteredTestClasses(TestRegistry testRegistry) {
	
		List<Class<? extends EnsTestCase>> registeredClasses = new ArrayList<Class<? extends EnsTestCase>>();
		
		for (EnsTestCase currentTestCase : testRegistry.getAll()) {
			registeredClasses.add(currentTestCase.getClass());
		}
		return registeredClasses;
	}
	
	protected void run() {

		TestRegistry testRegistry = this.testRegistry;
		Reporter reporter = this.reporter;
                String outputLevelString = configuration.getOutputLevel();
                setOutputLevel(outputLevelString);

		ReportManager.setReporter(reporter);

		DatabaseServer ds = connectToDatabase(configuration);

                if (this.reporterType == ReporterType.DATABASE && configuration.isEndSession()) {
                        log.info("Finishing reporter session");
                        systemPropertySetter.setPropertiesForReportManager_connectToOutputDatabase();
                        ReportManager.connectToOutputDatabase();
                        ReportManager.setSessionID(Long.valueOf(configuration.getEndSession()));
                        ReportManager.endDatabaseSession();
                        log.info("Finished reporter session");
                        return;
                }

		List<String> testDatabases = new ArrayList<String>(getTestDatabases());
		
		Species globalSpecies = null;

		if (configuration.isSpecies()) {
			globalSpecies = Species.resolveAlias(configuration.getSpecies());
			if (globalSpecies != Species.UNKNOWN) {
				logger.info("Will override guessed species with "
						+ globalSpecies + " for all databases");
			} else {
				String msg = "Argument " + configuration.getSpecies()
						+ " to -species not recognised";
				logger.severe(msg);
				throw new ConfigurationException(msg);
			}
		}

		DatabaseType globalDatabaseType = null;

		if (configuration.isDbType()) {
			globalDatabaseType = DatabaseType.resolveAlias(configuration
					.getDbType());
			if (globalDatabaseType != DatabaseType.UNKNOWN) {
				logger.info("Will override guessed database types with "
						+ globalDatabaseType + " for all databases");
			} else {
				String msg = "Argument " + configuration.getDbType()
						+ " to -type not recognised";
				logger.severe(msg);
				throw new ConfigurationException(msg);
			}
		}

		DatabaseRegistry databasesToTestRegistry = new DatabaseRegistry(
				testDatabases, globalDatabaseType, globalSpecies, false);

		if (databasesToTestRegistry.getAll().length == 0) {
			logger.warning("Warning: no databases configured!");
		}
		
		if (this.reporterType == ReporterType.DATABASE) {

			// Create the database to which tests will be written
			CreateHealthCheckDB c = new CreateHealthCheckDB(
					(ConfigureHealthcheckDatabase) configuration);

			boolean reportDatabaseExistsAlready = c
					.databaseExists(configuration.getOutputDatabase());

			if (reportDatabaseExistsAlready) {
				logger.info("Reporting database "
						+ configuration.getOutputDatabase()
						+ " already exists, will reuse.");
				System.out.println("Reporting database "
						+ configuration.getOutputDatabase()
						+ " already exists, will reuse.");
			} else {
				logger.info("Reporting database "
						+ configuration.getOutputDatabase()
						+ " does not exist, will create.");
				System.out.println("Reporting database "
						+ configuration.getOutputDatabase()
						+ " does not exist, will create.");
				c.run();
			}

			systemPropertySetter
					.setPropertiesForReportManager_connectToOutputDatabase();
			ReportManager.connectToOutputDatabase();

			systemPropertySetter
					.setPropertiesForReportManager_createDatabaseSession();
                        if (configuration.isSessionID()) {
                                ReportManager.reuseDatabaseSession(Long.valueOf(configuration.getSessionID()));
                        } else {
			        ReportManager.createDatabaseSession();
                        }
		}

		// When writing to a database, this must only be run after calling
		//
		// ReportManager.connectToOutputDatabase()
		//
		// Otherwise reporting problems won't work.
		//
		complainAboutDatabasesNotFound(databasesToTestRegistry, testDatabases);
		
		systemPropertySetter.setPropertiesForHealthchecks();

		log.info("Running tests\n\n");
		List<Class<? extends EnsTestCase>> testsThrowingAnException     = new ArrayList<Class<? extends EnsTestCase>>();
		List<Class<? extends EnsTestCase>> testsSkippedLongRunning      = new ArrayList<Class<? extends EnsTestCase>>();
		List<Class<? extends EnsTestCase>> testsSkippedForUnknownReason = new ArrayList<Class<? extends EnsTestCase>>();
		
		List<Class<? extends EnsTestCase>> testsApplyingToNoDb = findTestsApplyingToNoDb(testRegistry, databasesToTestRegistry);
		
		if (!testsApplyingToNoDb.isEmpty()) {
			
			ReportManager.problem(
					new TestRunnerSelfCheck(), 
					"Skipped tests", 
					"These tests apply to none of the databases that will be tested:\n" + testListToBulletPoints(testsApplyingToNoDb)
			);
		}
		
		Map<Class<? extends EnsTestCase>, List<DatabaseRegistryEntry>> exceptionToDb;
		
		try {
			TestRunStats accounting = runAllTestsWithAccounting(databasesToTestRegistry, testRegistry, false);

			exceptionToDb = accounting.getExceptionToDb();
			
			if (!exceptionToDb.isEmpty()) {
				
				for (Class<? extends EnsTestCase> currentTestClass : exceptionToDb.keySet()) {
					
					ReportManager.problem(
							new TestRunnerSelfCheck(), 
							"Skipped tests", 
							"The following test died with an exception: " + currentTestClass.getName() + "\n"
							+ "on the following databases: \n"
							+ dbreListToBulletPoints(
									exceptionToDb.get(currentTestClass)
							)
					);					
				}
				
			}
			
			for (Class<? extends EnsTestCase> currentTestCase : accounting.getTrackCompletionStatus().keySet()) {
				
				if (!accounting.getTrackCompletionStatus().get(currentTestCase).equals(TestRunStats.CompletionStatus.DIED_WITH_EXCEPTION)) {					
					testsThrowingAnException.add(currentTestCase);					
				}
				if (!accounting.getTrackCompletionStatus().get(currentTestCase).equals(TestRunStats.CompletionStatus.SKIPPED_LONG_RUNNING)) {					
					testsSkippedLongRunning.add(currentTestCase);					
				}
			}

			testsSkippedForUnknownReason = getAllRegisteredTestClasses(testRegistry);
			testsSkippedForUnknownReason.removeAll(accounting.getTestsRun());
			testsSkippedForUnknownReason.removeAll(testsThrowingAnException);
			testsSkippedForUnknownReason.removeAll(testsSkippedLongRunning);
			testsSkippedForUnknownReason.removeAll(testsApplyingToNoDb);			

		} catch (Throwable e) {
			log.severe("Execution of tests failed: " + e.getMessage());
			log.log(Level.FINE, "Execution of tests failed: " + e.getMessage(),
					e);
		}

		if (!testsSkippedForUnknownReason.isEmpty()) {
			
			ReportManager.problem(
				new TestRunnerSelfCheck(), 
				"Skipped tests", 
				"The following tests were skipped for no known reason:\n" + testListToBulletPoints(testsSkippedForUnknownReason)
			);
		}		
		
		if (!testsSkippedLongRunning.isEmpty()) {
			
			ReportManager.correct(
				new TestRunnerSelfCheck(), 
				"Skipped tests", 
				"The following tests were not run, because they are long running and the run was configured to skip these:\n" + testListToBulletPoints(testsSkippedLongRunning)
			);
		}

		
		log.info("Done running tests\n\n");
		
		boolean printFailureText = true;

		log.info("Printing output by test");
		printReportsByTest(outputLevel, printFailureText);

		if (this.reporterType == ReporterType.DATABASE && !configuration.isSessionID()) {
			log.info("Finishing reporter session");
			ReportManager.endDatabaseSession();
			log.info("Finished reporter session");
		}
	}

	public static String getDefaultPropertiesFile() {
		return DEFAULT_PROPERTIES_FILE;
	}

	/**
	 * <p>
	 * Generates a Collection<String> of the databases to be tested.
	 * </p>
	 * 
	 * <p>
	 * The databases to be tested are the ones specified explicitly via the
	 * testDatabases parameter and the ones specified by the division 
	 * parameter.
	 * </p> 
	 * 
	 * <p>
	 * If a division parameter has been set, this method will try to query the 
	 * production database. If a production database has not been configured,
	 * a ConfigurationException is thrown.
	 * </p>
	 * 
	 */
	protected Collection<String> getTestDatabases() {
		Collection<String> dbs = new HashSet<String>();
		if (configuration.isTestDatabases()
				&& configuration.getTestDatabases().size() > 0) {
			for (String db : configuration.getTestDatabases()) {
				if (!StringUtils.isEmpty(db)) {
					dbs.add(db);
				}
			}
		}
		if (configuration.isDivisions()
				&& configuration.getDivisions().size() > 0) {
			if (!configuration.isProductionDatabase()
					&& !configuration.isOutputHost()) {
				throw new ConfigurationException(
						"Parameters production.database and output.host etc. must be set to use test_divisions");
			} else {
				// don't want to have to do this, but need to query production
				// separately
				Connection conn = null;
				SqlTemplate template = null;
				try {
					conn = DBUtils.openConnection(
							configuration.getOutputDriver(),
							configuration.getOutputHost(),
							configuration.getOutputPort(),
							configuration.getOutputUser(),
							configuration.getOutputPassword(),
							configuration.getProductionDatabase());
					template = new ConnectionBasedSqlTemplateImpl(conn);
					for (String division : configuration.getDivisions()) {
						dbs.addAll(template.queryForDefaultObjectList(
								GET_DIVISION_SPECIES_DBS, String.class,
								division, division));
						dbs.addAll(template.queryForDefaultObjectList(
								GET_DIVISION_DBS, String.class, division,
								division));
					}
				} catch (SQLException e) {
					throw new RuntimeException(
							"Could not open connection to production db "
									+ configuration.getProductionDatabase(), e);
				} finally {
					DBUtils.closeQuietly(conn);
				}
			}
		}
		if (dbs.isEmpty()) {
			logger.warning(
					"No test databases found - Parameters test_databases or test_divisions have not been set - testing all databases...");
		}
		return dbs;
	}

	/**
	 * Run appropriate tests against databases. Also run show/repair methods if
	 * the test implements the Repair interface and the appropriate flags are
	 * set.
	 * 
	 * @param databaseRegistry
	 *            The DatabaseRegistry to use.
	 * @param testRegistry
	 *            The TestRegistry to use.
	 * @param skipSlow
	 *            If true, skip long-running tests.
	 */
	protected TestRunStats runAllTestsWithAccounting(DatabaseRegistry databaseRegistry,
			TestRegistry testRegistry, boolean skipSlow) {

		int numberOfTestsRun = 0;
		
		HashSet<Class<? extends EnsTestCase>> testsRun = new HashSet<Class<? extends EnsTestCase>>();
		Map<Class<? extends EnsTestCase>,TestRunStats.CompletionStatus> trackCompletionStatus = new HashMap<Class<? extends EnsTestCase>,TestRunStats.CompletionStatus>();
		Map<
			Class<? extends EnsTestCase>,
			List<DatabaseRegistryEntry>
		> exceptionToDb = new HashMap<
			Class<? extends EnsTestCase>,
			List<DatabaseRegistryEntry>
		>();

		// --------------------------------
		// Single-database tests

		// run the appropriate tests on each of them
		for (DatabaseRegistryEntry database : databaseRegistry.getAll()) {

			for (SingleDatabaseTestCase testCase : testRegistry.getAllSingle(
					groupsToRun, database.getType())) {

				if (!testCase.isLongRunning()
						|| (testCase.isLongRunning() && !skipSlow)) {

					try {
						ReportManager.startTestCase(testCase, database);

						testCase.types();
						
						boolean result = testCase.run(database);

						testsRun.add(testCase.getClass());
						trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.COMPLETED);
						
						ReportManager
								.finishTestCase(testCase, result, database);

						checkRepair(testCase, database);
						numberOfTestsRun++;

					} catch (Throwable e) {
						
						trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.DIED_WITH_EXCEPTION);
						
						if (!exceptionToDb.containsKey(testCase.getClass())) {
							exceptionToDb.put(testCase.getClass(), new ArrayList<DatabaseRegistryEntry>());
						}
						
						exceptionToDb.get(testCase.getClass()).add(database);							
						
					  String msg = "Could not execute test "
                + testCase.getName() + " on "
                + database.getName() + ": " + e.getMessage();
					  logger.log(Level.WARNING, msg, e);
					}

				} else {
					logger.info("Skipping long-running test "
							+ testCase.getName());
					trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.SKIPPED_LONG_RUNNING);

				}

			} // foreach test

		} // foreach DB

		// --------------------------------
		// Multi-database tests

		// here we just pass the whole DatabaseRegistry to each test
		// and let the test decide what to do

		for (MultiDatabaseTestCase testCase : testRegistry
				.getAllMulti(groupsToRun)) {

			if (!testCase.isLongRunning()
					|| (testCase.isLongRunning() && !skipSlow)) {
				try {
					ReportManager.startTestCase(testCase, null);

					logger.info("Starting test " + testCase.getName() + " ");

					testCase.types();
					boolean result = testCase.run(databaseRegistry);
					testsRun.add(testCase.getClass());
					trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.COMPLETED);

					ReportManager.finishTestCase(testCase, result, null);
					logger.info(testCase.getName() + " "
							+ (result ? "PASSED" : "FAILED"));

					numberOfTestsRun++;
				} catch (Throwable e) {
				  //TODO If we had a throwable then we should mark the test as failed 
          String msg = "Could not execute test "
              + testCase.getName() + ": " + e.getMessage();
          logger.log(Level.WARNING, msg, e);
          			trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.DIED_WITH_EXCEPTION);
				}
			} else {

				logger.info("Skipping long-running test " + testCase.getName());
				trackCompletionStatus.put(testCase.getClass(), TestRunStats.CompletionStatus.SKIPPED_LONG_RUNNING);

			}

		} // foreach test

		// --------------------------------
		// Ordered database tests

		// getAll() should give back databases in the order they were specified
		// on the command line
		DatabaseRegistryEntry[] orderedDatabases = databaseRegistry.getAll();

		for (OrderedDatabaseTestCase testCase : testRegistry
				.getAllOrdered(groupsToRun)) {

			ReportManager.startTestCase(testCase, null);

			try {
				boolean result = testCase.run(orderedDatabases);
				testsRun.add(testCase.getClass());

				ReportManager.finishTestCase(testCase, result, null);
				logger.info(testCase.getName() + " "
						+ (result ? "PASSED" : "FAILED"));
			} catch (Throwable e) {
			  //TODO If we had a throwable then we should mark the test as failed
        String msg = "Could not execute test "
            + testCase.getName() + ": " + e.getMessage();
        logger.log(Level.WARNING, msg, e);
			}

			numberOfTestsRun++;

		} // foreach test

		// --------------------------------

		if (numberOfTestsRun == 0) {
			logger.warning("Warning: no tests were run.");
		}

		return new TestRunStats(testsRun, trackCompletionStatus, exceptionToDb);
	} // runAllTests
	
	/**
	 * <p>
	 * Users specify the exact names of databases and these are used to 
	 * initialise the DatabaseRegistry object.
	 * </p>
	 * 
	 * <p>
	 * The DatabaseRegistry however uses them as regular expressions to which
	 * databases may match or not. If no matching database was found, it does
	 * not complain about it. The database will appear as if it has passed.
	 * </p>
	 * 
	 * <p>
	 * This is not the desired behaviour in the configurable testrunner. If 
	 * users misspell a database name, this should flag as an error.
	 * </p>
	 * 
	 * @param databasesToTestRegistry
	 * @param testDatabases
	 */
	void complainAboutDatabasesNotFound(DatabaseRegistry databasesToTestRegistry, List<String> testDatabases) {
		
		HashSet<String> namesOfDbsFound = new HashSet<String>();
		for (DatabaseRegistryEntry currentDRE: databasesToTestRegistry.getAll()) {			
			namesOfDbsFound.add(currentDRE.getName());			
		}
		for (String dbName : testDatabases) {
			if (!namesOfDbsFound.contains(dbName)) {
				ReportManager.problem(
					new TestRunnerSelfCheck(), 
					"Configuration problem", 
					"Database " + dbName + " has been specified for testing, but it doesn't exist on the server!"
				);
			}
		}
	}

	/**
	 * <p>
	 * 	Convert a list of tests into a string in which the names are listed
	 * in bullet points. The list is sorted by class names.
	 * </p>
	 * 
	 * <p>
	 * Useful for printing.
	 * </p>
	 * 
	 * @param listOfTests
	 * @return Stringified version of the list as bullet points
	 */
	protected String testListToBulletPoints(List<Class<? extends EnsTestCase>> listOfTests) {
		
		StringBuffer missingTestToString = new StringBuffer();

		// Sort list before printing. Otherwise they will appear in an
		// arbitrary order and appear as new every day on the admin site.
		//
		Collections.sort(listOfTests, new Comparator<Class<? extends EnsTestCase>>() {
			@Override
			public int compare(
					Class<? extends EnsTestCase> o1,
					Class<? extends EnsTestCase> o2) {

				return o1.getName().compareTo(o2.getName());
			}			
		});
		
		for (Class<? extends EnsTestCase> currentMissingTest : listOfTests) {
			missingTestToString.append("  - " + currentMissingTest.getName() + "\n");
		}
		return missingTestToString.toString();
	}
	
	/**
	 * <p>
	 * 	Convert a list of DatabaseRegistryEntry into a string in which the 
	 * names are listed in bullet points. The list is sorted using the 
	 * {@link DatabaseRegistryEntry#compareTo(DatabaseRegistryEntry)} method.
	 * </p>
	 * 
	 * <p>
	 * Useful for printing.
	 * </p>
	 * 
	 * @return Stringified version of the list as bullet points
	 */
	protected String dbreListToBulletPoints(List<DatabaseRegistryEntry> listOfDbres) {

		// Sort list before printing. Otherwise they will appear in an
		// arbitrary order and appear as new every day on the admin site.
		//
		Collections.sort(listOfDbres, new Comparator<DatabaseRegistryEntry>() {
			@Override
			public int compare(
					DatabaseRegistryEntry o1,
					DatabaseRegistryEntry o2) {

				return o1.compareTo(o2);
			}			
		});
		
		StringBuffer listOfDbresToString = new StringBuffer();
		
		for (DatabaseRegistryEntry currentDbre : listOfDbres) {
			listOfDbresToString.append("  - " + currentDbre.getName() + "\n");
		}
		return listOfDbresToString.toString();
	}
	
	List<Class<? extends EnsTestCase>> findTestsApplyingToNoDb(TestRegistry testRegistry, DatabaseRegistry databasesToTestRegistry) {
		
		HashSet<DatabaseType> databaseTypesRegistered = new HashSet<DatabaseType>(); 
		
		for (DatabaseRegistryEntry dbre : databasesToTestRegistry.getAll()) {
			databaseTypesRegistered.add(dbre.getType());
		}
		
		List<Class<? extends EnsTestCase>> testsApplyingToNoDb = new ArrayList<Class<? extends EnsTestCase>>(); 
		
		for (Class<? extends EnsTestCase> currentTest : getAllRegisteredTestClasses(testRegistry)) {
			
			DatabaseType[] dbT;

			/*
			 * Instead of instantiating a test directly, a group of this one 
			 * test is created and then retrieved from it.
			 * 
			 * The reason is that "getTests" has method calls to set the types
			 * of databases to which this test can be applied.
			 * 
			 * By instantiating the test this way, the tescase is initialised
			 * the same way as it will be when it is run by the testrunner.
			 * 
			 */
			GroupOfTests g = new GroupOfTests();
			g.addTest(currentTest);
			dbT = g.getTests().iterator().next().getAppliesToTypes();

			boolean currentTestAppliesToADb = false;
			
			for (DatabaseType currentDbt : dbT) {
				if (databaseTypesRegistered.contains(currentDbt)) {
					currentTestAppliesToADb = true;
					if (currentTestAppliesToADb) {
						break;
					}
				}
			}
			if (!currentTestAppliesToADb) {
				testsApplyingToNoDb.add(currentTest);
			}
		}
		return testsApplyingToNoDb;
	}
}


class TestRunnerSelfCheck extends EnsTestCase {
	public TestRunnerSelfCheck() {
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}
};

class TestRunStats {

	protected enum CompletionStatus {
		COMPLETED,
		SKIPPED_LONG_RUNNING,
		DIED_WITH_EXCEPTION		
	}

	public HashSet<Class<? extends EnsTestCase>> getTestsRun() {
		return testsRun;
	}

	public Map<Class<? extends EnsTestCase>, CompletionStatus> getTrackCompletionStatus() {
		return trackCompletionStatus;
	}

	protected final HashSet<Class<? extends EnsTestCase>> testsRun;
	protected final Map<Class<? extends EnsTestCase>, CompletionStatus> trackCompletionStatus;
	protected final Map<
		Class<? extends EnsTestCase>,
		List<DatabaseRegistryEntry>
	> exceptionToDb;

	public Map<Class<? extends EnsTestCase>, List<DatabaseRegistryEntry>> getExceptionToDb() {
		return exceptionToDb;
	}

	public TestRunStats(
			HashSet<Class<? extends EnsTestCase>> testsRun, 
			Map<Class<? extends EnsTestCase>, CompletionStatus> trackCompletionStatus,
			Map<
				Class<? extends EnsTestCase>,
				List<DatabaseRegistryEntry>
			> exceptionToDb
	) {
		this.testsRun = testsRun;
		this.trackCompletionStatus = trackCompletionStatus;
		this.exceptionToDb = exceptionToDb;
	}
}



