/*
 * Copyright [1999-2018] EMBL-European Bioinformatics Institute
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.StandaloneReporter.OutputFormat;
import org.ensembl.healthcheck.configuration.ConfigureTestGroups;
import org.ensembl.healthcheck.configurationmanager.ConfigurationException;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

import com.google.gson.Gson;
import com.mysql.jdbc.Driver;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * Simple class to run one or more tests or groups on a single database
 * 
 * @author dstaines
 *
 */
public class StandaloneTestRunner {

	/**
	 * 
	 */
	private static final String WRITE_STDOUT = "-";

	/**
	 * Options that specify how the tests run
	 * 
	 * @author dstaines
	 *
	 */
	public interface StandaloneTestOptions extends ConfigureTestGroups {

		@Option(helpRequest = true, description = "display help")
		boolean getHelp();

		@Option(shortName = "o", longName = "output_file", defaultValue = WRITE_STDOUT, description = "File to write any failures to (use '-' for standard out)")
		String getOutputFile();

		@Option(shortName = "f", longName = "output_format", defaultValue = "text", description = "Format for writing output")
		String getOutputFormat();

		@Option(shortName = "v", longName = "verbose", description = "Show detailed debugging output")
		boolean isVerbose();

		@Option(shortName = "d", longName = "dbname", description = "Database to test")
		String getDbname();

		boolean isDbname();

		@Option(shortName = "u", longName = "user", description = "Username for test database")
		String getUser();

		boolean isUser();

		@Option(shortName = "h", longName = "host", description = "Host for test database")
		String getHost();

		boolean isHost();

		@Option(shortName = "p", longName = "pass", description = "Password for test database")
		String getPassword();

		boolean isPassword();

		@Option(shortName = "P", longName = "port", description = "Port for test database")
		int getPort();

		boolean isPort();

		@Option(longName = "compara_dbname", defaultValue = "ensembl_compara_master", description = "Name of compara master database")
		String getComparaMasterDbname();

		boolean isComparaMasterDbname();

		@Option(longName = "compara_host", description = "Compara master database host")
		String getComparaHost();

		boolean isComparaHost();

		@Option(longName = "compara_port", description = "Compara master database port")
		int getComparaPort();

		boolean isComparaPort();

		@Option(longName = "compara_user", description = "Compara master database user")
		String getComparaUser();

		boolean isComparaUser();

		@Option(longName = "compara_pass", description = "Compara master database password")
		String getComparaPassword();

		boolean isComparaPassword();

		@Option(longName = "prod_dbname", defaultValue = "ensembl_production_91", description = "Name of production database")
		String getProductionDbname();

		boolean isProductionDbname();

		@Option(longName = "prod_host", description = "Production database host")
		String getProductionHost();

		boolean isProductionHost();

		@Option(longName = "prod_port", description = "Production database port")
		int getProductionPort();

		boolean isProductionPort();

		@Option(longName = "prod_user", description = "Production database user")
		String getProductionUser();

		boolean isProductionUser();

		@Option(longName = "prod_pass", description = "Production database password")
		String getProductionPassword();

		boolean isProductionPassword();

		@Option(longName = "secondary_host", description = "Secondary database host (ie. previous release)")
		String getSecondaryHost();

		boolean isSecondaryHost();

		@Option(longName = "secondary_port", description = "Secondary database port")
		int getSecondaryPort();

		boolean isSecondaryPort();

		@Option(longName = "secondary_user", description = "Secondary database user")
		String getSecondaryUser();

		boolean isSecondaryUser();

		@Option(longName = "secondary_pass", description = "Secondary database password")
		String getSecondaryPassword();

		boolean isSecondaryPassword();

		@Option(longName = "staging_host", description = "Staging database host (ie. current release)")
		String getStagingHost();

		boolean isStagingHost();

		@Option(longName = "staging_port", description = "Staging database port")
		int getStagingPort();

		boolean isStagingPort();

		@Option(longName = "staging_user", description = "Staging database user")
		String getStagingUser();

		boolean isStagingUser();

		@Option(longName = "staging_pass", description = "Staging database password")
		String getStagingPassword();

		boolean isStagingPassword();

		@Option(longName = "release", shortName = "r", description = "Current release")
		String getRelease();

		boolean isRelease();

		@Option(longName = "data_files_path", shortName = "D", description = "Path to data files directory")
		String getDataFilesPath();

		boolean isDataFilesPath();

		@Option(longName = "master_schema", shortName = "m", description = "Name of master schema for comparisons")
		String getMasterSchema();

		boolean isMasterSchema();

		@Option(longName = "list_tests", shortName = "l", description = "Show all the tests in the specified groups and tests")
		boolean isListTests();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		StandaloneTestOptions options = null;
		Cli<StandaloneTestOptions> cli = CliFactory.createCli(StandaloneTestOptions.class);
		try {
			options = cli.parseArguments(args);
		} catch (ArgumentValidationException e) {
			System.err.println(e.getMessage());
			System.exit(2);
		}

		if (options.isListTests()) {
			System.exit(listTests(options));
		}

		if (!options.isDbname() || !options.isHost() || !options.isPort() || !options.isUser()) {
			System.err.println("--dbname, --host, --port and --user are required options");
			System.err.println(cli.getHelpMessage());
			System.exit(2);
		}

		StandaloneTestRunner runner = new StandaloneTestRunner(options);

		if (!StringUtils.isEmpty(options.getOutputFile()) && !options.getOutputFile().equals(WRITE_STDOUT)) {
			File outfile = new File(options.getOutputFile());
			if (outfile.exists()) {
				runner.getLogger().fine("Deleting existing output file " + options.getOutputFile());
				if (!outfile.delete()) {
					runner.getLogger().fine("Could not delete existing output file " + options.getOutputFile());
					System.exit(3);
				}
			}
		}

		StandaloneReporter reporter = new StandaloneReporter(runner.getLogger());
		ReportManager.setReporter(reporter);

		boolean result = runner.runAll();
		if (!result) {
			printFailures(options, runner, reporter);
		} else {
			runner.getLogger().info("Completed healthchecks with no failures");
		}
		System.exit(result ? 0 : 1);

	}

	private static int listTests(StandaloneTestOptions options) {
		Writer w = null;
		try {
			TestRegistry testRegistry = new ConfigurationBasedTestRegistry(options);
			Set<String> testCases = new HashSet<>();
			for (EnsTestCase test : testRegistry.getAll()) {
				testCases.add(test.getName());
			}
			if (options.getOutputFile().equals(WRITE_STDOUT)) {
				w = new PrintWriter(System.out);
			} else {
				w = new BufferedWriter(new FileWriter(new File(options.getOutputFile())));
			}
			switch (OutputFormat.valueOf(options.getOutputFormat().toUpperCase())) {
			case JSON:
				w.write(new Gson().toJson(testCases));
				break;
			case TEXT:
				for (String test : testCases) {
					w.write(test);
					w.write("\n");
				}
				break;
			default:
				throw new IllegalArgumentException("Don't know how to handle format " + options.getOutputFormat());
			}
			w.close();
			return 0;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return 1;
		} finally {
			IOUtils.closeQuietly(w);
		}
	}

	private static void printFailures(StandaloneTestOptions options, StandaloneTestRunner runner,
			StandaloneReporter reporter) {
		OutputFormat format = OutputFormat.valueOf(options.getOutputFormat().toUpperCase());
		if (options.getOutputFile().equals(WRITE_STDOUT)) {
			runner.getLogger().severe("Failures detected - writing details to screen");
			try {
				PrintWriter writer = new PrintWriter(System.out);
				reporter.writeFailures(writer, format);
				writer.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(4);
			}
		} else {
			runner.getLogger().severe("Failures detected - writing details to " + options.getOutputFile());
			reporter.writeFailureFile(options.getOutputFile(), format);
		}
	}

	private Logger logger;
	private final StandaloneTestOptions options;
	private DatabaseRegistryEntry productionDb;
	private DatabaseRegistryEntry comparaMasterDb;
	private DatabaseRegistryEntry testDb;
	private DatabaseServer primaryServer;
	private DatabaseServer secondaryServer;
	private DatabaseServer stagingServer;

	public StandaloneTestRunner(StandaloneTestOptions options) {
		this.options = options;
		getLogger().fine("Connecting to primary server " + options.getHost());
		DBUtils.overrideMainDatabaseServer(getPrimaryServer(), true);
		if (options.isSecondaryHost()) {
			getLogger().fine("Connecting to secondary server " + options.getSecondaryHost());
			DBUtils.overrideSecondaryDatabaseServer(getSecondaryServer());
		}
		String release = null;
		if (!options.isRelease()) {
			getLogger().fine("Release not specified, inferring from " + options.getDbname());
			release = DatabaseRegistryEntry.getInfoFromName(options.getDbname()).getSchemaVersion();
		} else {
			release = options.getRelease();
		}
		if (options.isStagingHost()) {
			getLogger().fine("Connecting to staging server " + options.getStagingHost());
			DBUtils.overrideMainDatabaseServer(getStagingServer(), false);
		}
		if (!StringUtils.isEmpty(release)) {
			getLogger().fine("Setting release " + release);
			DBUtils.setRelease(release);
		}
		if (options.isDataFilesPath()) {
			System.setProperty("dataFileBasePath", options.getDataFilesPath());
		}
		System.setProperty("compara_master.database", options.getComparaMasterDbname());

		String masterSchema = options.isMasterSchema() ? options.getMasterSchema() : null;
		if (options.getDbname().matches(".*_compara_.*")) {
			if (StringUtils.isEmpty(masterSchema)) {
				masterSchema = "master_schema_compara_" + release;
			}
			System.setProperty("master.schema.compara", masterSchema);
		} else if (options.getDbname().matches(".*_funcgen_.*")) {
			if (StringUtils.isEmpty(masterSchema)) {
				masterSchema = "master_schema_funcgen_" + release;
			}
			System.setProperty("master.schema.funcgen", masterSchema);
		} else if (options.getDbname().matches(".*_variation_.*")) {
			if (StringUtils.isEmpty(masterSchema)) {
				masterSchema = "master_schema_variation_" + release;
			}
			System.setProperty("master.schema.variation", masterSchema);
		} else {
			if (StringUtils.isEmpty(masterSchema)) {
				masterSchema = "master_schema_" + release;
			}
			System.setProperty("master.schema.core", masterSchema);
		}
	}

	public Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(StandaloneTestRunner.class.getCanonicalName());
			ConsoleHandler localConsoleHandler = new ConsoleHandler();
			localConsoleHandler.setFormatter(new Formatter() {
				DateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

				@Override
				public String format(LogRecord record) {
					return String.format("%s %s %s : %s%n", format.format(new Date(record.getMillis())),
							record.getSourceClassName(), record.getLevel().toString(), record.getMessage());
				}
			});
			if (options.isVerbose()) {
				localConsoleHandler.setLevel(Level.ALL);
				logger.setLevel(Level.ALL);
			} else {
				localConsoleHandler.setLevel(Level.INFO);
				logger.setLevel(Level.INFO);
			}
			logger.setUseParentHandlers(false);
			logger.addHandler(localConsoleHandler);
		}
		return logger;
	}

	public DatabaseRegistryEntry getProductionDb() {
		if (productionDb == null && options.isProductionHost()) {
			getLogger().info("Connecting to production database " + options.getProductionDbname());
			productionDb = new DatabaseRegistryEntry(new DatabaseServer(options.getProductionHost(),
					String.valueOf(options.getProductionPort()), options.getProductionUser(),
					options.isProductionPassword() ? options.getProductionPassword() : null, Driver.class.getName()),
					options.getProductionDbname(), null, null);
		}
		return productionDb;
	}

	public DatabaseRegistryEntry getComparaMasterDb() {
		if (comparaMasterDb == null && options.isComparaHost()) {
			getLogger().info("Connecting to compara master database " + options.getComparaMasterDbname());
			comparaMasterDb = new DatabaseRegistryEntry(new DatabaseServer(options.getComparaHost(),
					String.valueOf(options.getComparaPort()), options.getComparaUser(),
					options.isComparaPassword() ? options.getComparaPassword() : null, Driver.class.getName()),
					options.getComparaMasterDbname(), null, null);
		}
		return comparaMasterDb;
	}

	public DatabaseRegistryEntry getTestDb() {
		if (testDb == null) {
			getLogger().info("Connecting to test database " + options.getDbname());
			testDb = new DatabaseRegistryEntry(getPrimaryServer(), options.getDbname(), null, null);
			if (testDb.getConnection() == null) {
				throw new ConfigurationException("Test database " + options.getDbname() + " not found");
			}
		}
		return testDb;
	}

	public DatabaseServer getPrimaryServer() {
		if (primaryServer == null)
			primaryServer = new DatabaseServer(options.getHost(), String.valueOf(options.getPort()), options.getUser(),
					options.isPassword() ? options.getPassword() : null, Driver.class.getName());
		return primaryServer;
	}

	public DatabaseServer getSecondaryServer() {
		if (secondaryServer == null) {
			secondaryServer = new DatabaseServer(options.getSecondaryHost(), String.valueOf(options.getSecondaryPort()),
					options.getSecondaryUser(), options.isSecondaryPassword() ? options.getSecondaryPassword() : null,
					Driver.class.getName());
		}
		return secondaryServer;
	}

	public DatabaseServer getStagingServer() {
		if (stagingServer == null) {
			stagingServer = new DatabaseServer(options.getStagingHost(), String.valueOf(options.getStagingPort()),
					options.getStagingUser(), options.isStagingPassword() ? options.getStagingPassword() : null,
					Driver.class.getName());
		}
		return stagingServer;
	}

	private TestRegistry testRegistry;

	private TestRegistry getTestRegistry() {
		if (testRegistry == null) {
			try {
				this.testRegistry = new ConfigurationBasedTestRegistry(options);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (UnknownTestTypeException e) {
				throw new RuntimeException(e);
			}
		}
		return testRegistry;
	}

	public boolean runAll() {
		boolean success = true;
		for (EnsTestCase testCase : getTestRegistry().getAll()) {
			success &= runTestCase(testCase);
		}
		return success;
	}

	public boolean runTestCase(EnsTestCase test) {
		boolean success = true;
		if (SingleDatabaseTestCase.class.isAssignableFrom(test.getClass())) {
			getLogger().info("Executing testcase " + test.getName());
			test.setProductionDatabase(getProductionDb());
			test.setComparaMasterDatabase(getComparaMasterDb());
			ReportManager.startTestCase(test, getTestDb());
			if (test.appliesToType(getTestDb().getType())) {
				boolean result = ((SingleDatabaseTestCase) test).run(getTestDb());
				ReportManager.finishTestCase(test, result, getTestDb());
				getLogger().info(test.getName() + " " + (result ? "succeeded" : "failed"));
				success &= result;
			} else {
				getLogger().info("Skipping testcase " + test.getName() + " for database " + getTestDb().getName()
						+ " of type " + getTestDb().getType().getName());
			}
		} else {
			getLogger().fine("Skipping non-single testcase " + test.getName());
		}
		return success;
	}

}
