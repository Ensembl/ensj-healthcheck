package org.ensembl.healthcheck;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

	protected void run() {

		TestRegistry testRegistry = this.testRegistry;
		Reporter reporter = this.reporter;

		ReportManager.setReporter(reporter);

		// Notice the elegant downcast happening here
		DatabaseServer ds = connectToDatabase(configuration);

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
			ReportManager.createDatabaseSession();
		}

		systemPropertySetter.setPropertiesForHealthchecks();

		log.info("Running tests\n\n");
		try {
			runAllTests(databasesToTestRegistry, testRegistry, false);
		} catch (Throwable e) {
			log.severe("Execution of tests failed: " + e.getMessage());
			log.log(Level.FINE, "Execution of tests failed: " + e.getMessage(),
					e);
		}
		log.info("Done running tests\n\n");

		boolean printFailureText = true;

		log.info("Printing output by test");
		printReportsByTest(outputLevel, printFailureText);

		if (this.reporterType == ReporterType.DATABASE) {
			log.info("Finishing reporter session");
			ReportManager.endDatabaseSession();
			log.info("Finished reporter session");
		}
	}

	public static String getDefaultPropertiesFile() {
		return DEFAULT_PROPERTIES_FILE;
	}

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

}
