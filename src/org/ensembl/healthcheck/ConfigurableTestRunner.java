package org.ensembl.healthcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import org.ensembl.healthcheck.ReporterFactory.ReporterType;
import org.ensembl.healthcheck.TestRegistryFactory.TestRegistryType;
import org.ensembl.healthcheck.configuration.ConfigurationUserParameters;
import org.ensembl.healthcheck.configuration.ConfigureConfiguration;
import org.ensembl.healthcheck.configuration.ConfigureHealthcheckDatabase;
import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.configuration.ConfigureTestGroups;
import org.ensembl.healthcheck.configurationmanager.ConfigurationException;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory;
import org.ensembl.healthcheck.configurationmanager.ConfigurationDumper;
import org.ensembl.healthcheck.configurationmanager.ConfigurationFactory.ConfigurationType;
import org.ensembl.healthcheck.util.CreateHealthCheckDB;
import org.ensembl.healthcheck.util.DBUtils;

/**
 *
 * Runs test which can be configured on the command line and stores them in a database.
 *
 */
public class ConfigurableTestRunner extends TestRunner {
	
	static final Logger log = Logger.getLogger(ConfigurableTestRunner.class.getCanonicalName());
	
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
	protected final ReporterType     reporterType;

	/**
	 * The type of test registry used.
	 */
	protected final TestRegistryType testRegistryType;
	
	protected final SystemPropertySetter systemPropertySetter;

	protected final TestRegistry testRegistry;		
	protected final Reporter     reporter;

	/**
	 * @param configuration - A configuration object of type 
	 *     ConfigurationUserParameters
	 * 
	 * Creates a ConfigurableTestRunner using the parameters from the
	 * configuration object.
	 * 
	 */
	public ConfigurableTestRunner(ConfigurationUserParameters configuration) {

		log.config(
				"Using classpath: \n\n"
				+ Debug.classpathToString()
		);
		
		this.configuration        = configuration;
		this.systemPropertySetter = new SystemPropertySetter(configuration);
		this.testRegistryType     = getTestRegistryType (configuration.getTestRegistryType());
		this.reporterType         = getReporterType     (configuration.getReporterType());
		this.testRegistry         = getTestRegistry     (this.testRegistryType, configuration);
		this.reporter             = getReporter         (this.reporterType);

		DBUtils.setHostConfiguration( (ConfigureHost) configuration );
	}
	
	/**
	 * @param args - The command line arguments
	 * 
	 * Creates a ConfigurableTestRunner using command line arguments. The 
	 * configuration parameters are taken from the command line and from
	 * the properties files that can be specifies on the command line
	 * with the --conf option. The DEFAULT_PROPERTIES_FILE is added as
	 * well. 
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
			r = ReporterType.valueOf(reporterType);
		} catch(IllegalArgumentException e) {
			throw new ConfigurationException(
				"Parameter reportertype has been set to an illegal value: "
				+ reporterType
			);
		}

		return r;
	}
	
	/**
	 * Used for created layered constructors.
	 */
	protected static TestRegistryType getTestRegistryType(String testRegistryType) {
		
		TestRegistryType t;
		
		try {
			t = TestRegistryType.valueOf(testRegistryType);
		} catch(IllegalArgumentException e) {
			throw new ConfigurationException(
				"Parameter reportertype has been set to an illegal value: "
				+ testRegistryType
			);			
		}

		return t;
	}
	
	/**
	 * @return TestRegistry
	 * 
	 * Creates a TestRegistry. The type of TestRegistry is determined by the 
	 * configuration object.
	 * 
	 */
	protected static TestRegistry getTestRegistry(
		TestRegistryType testRegistryType,
		ConfigureTestGroups configuration
	) {
		
		TestRegistryFactory testRegistryFactory = new TestRegistryFactory(

			// Downcast of configuration, the TestRegistryFactory and the 
			// Registries it produces from it only have access to the  
			// params in the ConfigureTestGroups interface
			//
			(ConfigureTestGroups) configuration
		);
		
		log.config("Using test registry of type: " + testRegistryType);
		
		TestRegistry testRegistry = null;
		
		try {
			testRegistry = testRegistryFactory.getTestRegistry(testRegistryType);
		} catch (TestRegistryCreationException e) {
			throw new ConfigurationException(e);
		}
		
		log.config( "Using testregistry with this configuration:\n" + testRegistry.toString() );
		
		return testRegistry;
	}
	
	/**
	 * @return Reporter
	 * 
	 * Creates a reporter. The type of reporter is determined by the 
	 * configuration object.
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
	protected static ConfigurationUserParameters createConfigurationObj(String[] args) {

		// A temporary configuration file for accessing the command line  
		// parameters in which the user configures where the configuration
		// files are located. Since only this information is of interest at
		// this point the configuration object is subcast to the
		// ConfigureConfiguration interface.
		//
		ConfigureConfiguration conf = new ConfigurationFactory<ConfigurationUserParameters>(
				ConfigurationUserParameters.class, 
				(Object[]) args
		).getConfiguration(ConfigurationType.Commandline);
			
		// Get the list of property files the user specified and add the 
		// default property file to it.
		//
		List<File> propertyFileNames = new ArrayList<File>();
		
		// The user is not required to provide a property file. In this case only the
		// command line arguments and the default properties file will be used.
		//
		if (conf.isConf()) {
			propertyFileNames.addAll(conf.getConf());
			
			//for (File propertyFileName : conf.getConf()) {				
				//propertyFileNames.add(propertyFileName);
			//}
		}
		propertyFileNames.add(new File(DEFAULT_PROPERTIES_FILE));
		
		// Use this to create the final configuration object.
		ConfigurationFactory<ConfigurationUserParameters> confFact = new ConfigurationFactory(
			ConfigurationUserParameters.class, 
			args,
			propertyFileNames
		);
		
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
		ConfigurationUserParameters configuration = confFact.getConfiguration(ConfigurationType.Cascading);

		// Show user the final configuration settings that will be used.
		log.config(
			"The following settings will be used:\n\n"
			+ new ConfigurationDumper<ConfigurationUserParameters>().dump(configuration)
		);
			
		return configuration;
	}
	
	public static void main(String[] args) {
		
		try {

			ConfigurableTestRunner configurableTestRunner = new ConfigurableTestRunner(args);
			configurableTestRunner.run();

		} catch (ConfigurationException e) {
			
			//ConfigurableTestRunner.logger.log(Level.CONFIG, e.getMessage(), e);
			ConfigurableTestRunner.logger.log(Level.CONFIG, e.getMessage());
		}
	}
	
	protected DatabaseServer connectToDatabase(ConfigureHost conf) {

		DatabaseServer ds = new DatabaseServer(
			conf.getHost(), 
			conf.getPort(), 
			conf.getUser(), 
			conf.getPassword(), 
			conf.getDriver()
		);
		return ds;
	}

	protected void run() {

		TestRegistry testRegistry = this.testRegistry;		
		Reporter     reporter     = this.reporter;

		ReportManager.setReporter(reporter);
		
		// Notice the elegant downcast happening here
		DatabaseServer ds = connectToDatabase(configuration);
		
		List<DatabaseRegistryEntry> databasesToTestList = new ArrayList<DatabaseRegistryEntry>();

		if (!configuration.isOutputDatabases()) {
			throw new ConfigurationException("Parameter output.databases has not been set!");
		}
		
		// Create a DatabaseRegistryEntry for every database the user specified 
		for(String databaseToTest : configuration.getOutputDatabases()) {

			DatabaseRegistryEntry currentDatabaseToTest = new DatabaseRegistryEntry(ds, databaseToTest, null, null);
			databasesToTestList.add(currentDatabaseToTest);
		}
		
		
		DatabaseRegistry databasesToTestRegistry = new DatabaseRegistry(databasesToTestList);
		
		if (databasesToTestRegistry.getAll().length == 0) {
			logger.warning("Warning: no databases configured!");
		}

		if (this.reporterType == ReporterType.Database) {
		
			// Create the database to which tests will be written
			CreateHealthCheckDB c = new CreateHealthCheckDB((ConfigureHealthcheckDatabase) configuration);
			
			boolean reportDatabaseExistsAlready = c.databaseExists(configuration.getOutputDatabase());
			
			if (reportDatabaseExistsAlready) {
				logger.info("Reporting database "+configuration.getOutputDatabase()+" already exists, will reuse.");
				System.out.println("Reporting database "+configuration.getOutputDatabase()+" already exists, will reuse.");
			} else {
				logger.info("Reporting database "+configuration.getOutputDatabase()+" does not exist, will create.");
				System.out.println("Reporting database "+configuration.getOutputDatabase()+" does not exist, will create.");
				c.run();
			}

			systemPropertySetter.setPropertiesForReportManager_connectToOutputDatabase();
			ReportManager.connectToOutputDatabase();

			systemPropertySetter.setPropertiesForReportManager_createDatabaseSession();
			ReportManager.createDatabaseSession();
		}
		
		systemPropertySetter.setPropertiesForHealthchecks();
		
		log.info("Running tests\n\n");
		runAllTests(databasesToTestRegistry, testRegistry, false);
		log.info("Done running tests\n\n");
		
		boolean printFailureText = true;

		printReportsByTest(outputLevel, printFailureText);

		if (this.reporterType == ReporterType.Database) {
			// Commented out, because all it does is try to update a column
			// in the database that doesn't exist.
			
			// ReportManager.endDatabaseSession();
		}
	}
}








