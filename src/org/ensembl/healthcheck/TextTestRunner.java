/*
 copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.TestComparator;
import org.ensembl.healthcheck.util.Utils;

/**
 * Subclass of TestRunner intended for running tests from the command line.
 */
public class TextTestRunner extends TestRunner implements Reporter {

	private static String version = "$Id$";

	private ArrayList<String> databaseRegexps = new ArrayList<String>();

	private ArrayList<String> secondaryDatabaseRegexps = new ArrayList<String>();

	private boolean debug = false;

	private ArrayList<String> outputBuffer = new ArrayList<String>();

	private String lastDatabase = "";

	private int outputLineLength = 65;

	private TestRegistry testRegistry;

	private DatabaseRegistry mainDatabaseRegistry;

	private DatabaseRegistry secondaryDatabaseRegistry;

	private Species globalSpecies = null;

	private DatabaseType globalType = null;

	private boolean printResultsByTest = true;

	private boolean printResultsByDatabase = false;

	private boolean printFailureText = true;

	private boolean skipSlow = false;

	private boolean noLogging = false;

	// allow specification of custom database connection details on the command
	// line
	private String customHost = null;

	private String customPort = null;

	private String customUser = null;

	private String customPass = null;

	private static final String CORE_DB_REGEXP = "[a-z]+_[a-z]+_(core|otherfeatures|cdna|vega|otherfeatures|sanger_vega|rnaseq)";

	// -------------------------------------------------------------------------

	/**
	 * Command-line run method.
	 * 
	 * @param args
	 *          The command-line arguments.
	 */
	public static void main(String[] args) {

		new TextTestRunner().run(args);

	} // main

	// -----------------------------------------------------------------

	private void run(String[] args) {

		testRegistry = new TestRegistry();
		
		parseCommandLine(args);

		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE, true);

		setupLogging();

		setCustomProperties();

		Utils.buildDatabaseURLs();

		ReportManager.setReporter(this);

		mainDatabaseRegistry = new DatabaseRegistry(databaseRegexps, globalType, globalSpecies, false);
		DBUtils.setMainDatabaseRegistry(mainDatabaseRegistry);
		
		secondaryDatabaseRegistry = new DatabaseRegistry(secondaryDatabaseRegexps, globalType, globalSpecies, true);
		DBUtils.setSecondaryDatabaseRegistry(secondaryDatabaseRegistry);

		if (mainDatabaseRegistry.getEntryCount() == 0) {
			logger.warning("Warning: no database names matched any of the database regexps given");
		}

		runAllTests(mainDatabaseRegistry, testRegistry, skipSlow);

		if (printResultsByDatabase) {
			printReportsByDatabase(outputLevel);
		}

		if (printResultsByTest) {
			printReportsByTest(outputLevel, printFailureText);
		}

		ConnectionPool.closeAll();

	} // run

	// -------------------------------------------------------------------------
	private void printUsage() {

		System.out.println("\nUsage: TextTestRunner {options} {group1} {group2} ...\n");
		System.out.println("Options:");
		System.out.println("  -d regexp       Use the given regular expression to decide which databases to use.");
		System.out.println("  -d2 regexp      Same for the secondary server.");
		System.out
				.println("                  Note that more than one -d argument can be used; testcases that depend on the order of databases will be passed the databases in the order in which they appear on the command line");
		System.out.println("  -h              This message.");
		System.out.println("  -output level   Set output level; level can be one of ");
		System.out.println("                    none      nothing is printed");
		System.out.println("                    problem   only problems are reported (this is the default)");
		System.out.println("                    correct   only correct results (and problems) are reported");
		System.out.println("                    summary   only summary info (and problems, and correct reports) are reported");
		System.out.println("                    info      info (and problem, correct, summary) messages reported");
		System.out.println("                    all       everything is printed");
		System.out.println("  -species s      Use s as the species for all databases instead of trying to guess the species from the name");
		System.out.println("  -type t         Use t as the type for all databases instead of trying to guess the type from the name");
		System.out.println("  -debug          Print debugging info (for developers only)");
		System.out.println("  -config file    Read configuration information from file instead of " + PROPERTIES_FILE);
		System.out.println("  -repair         If appropriate, carry out repair methods on test cases that support it");
		System.out.println("  -showrepair     Like -repair, but the repair is NOT carried out, just reported.");
		System.out.println("  -length n       Break output lines at n columns; default is " + outputLineLength + ". 0 means never break");
		System.out.println("  -resultsbydb    Print results by databases as well as by test case.");
		System.out.println("  -nofailuretext  Don't print failure hints.");
		System.out.println("  -skipslow       Don't run long-running tests");
		System.out.println("  -nologging      Suppress logging info");
		System.out.println("  -host           Custom database connection information. Note that in most cases this should be set in the");
		System.out.println("  -port           properties file and not on the command line.");
		System.out.println("  -user           Note that, since the password is optional, if it is set in the properties file but not on");
		System.out.println("  -pass           the command line, the one in the properties file will be used anyway, possibly leading to odd results.");
		System.out.println("                  Note that -password can also be used here.");
		System.out.println("  group1          Names of groups of test cases to run.");
		System.out.println("                  Note each test case is in a group of its own with the name of the test case.");
		System.out.println("                  This allows individual tests to be run if required.");
		System.out.println("");
		System.out.println("If no tests or test groups are specified, and a database regular expression is given with -d, the matching databases are shown. ");

		System.out.println("\nCurrently available tests:");

		List tests = testRegistry.getAll();
		Map groups = new HashMap();
		Collections.sort(tests, new TestComparator());
		Iterator it = tests.iterator();
		while (it.hasNext()) {
			EnsTestCase test = (EnsTestCase) it.next();
			System.out.print(test.getShortTestName() + " ");
			List testGroups = test.getGroups();
			Iterator it2 = testGroups.iterator();
			while (it2.hasNext()) {
				String group = (String) it2.next();
				if (group.equals(group.toLowerCase())) {
					groups.put(group, group);
				}
			}
		}

		System.out.println("\n\nCurrently available test groups (use show-groups.sh to show which tests are in which groups):");
		System.out.println(Utils.listToString(new ArrayList(groups.keySet()), " "));

	}

	/**
	 * Return the CVS version string for this class.
	 * 
	 * @return The version.
	 */
	public String getVersion() {

		// strip off first and last few chars of version since these are only
		// used by CVS
		return version.substring(5, version.length() - 2);

	}

	// -------------------------------------------------------------------------
	private void parseCommandLine(String[] args) {

		if (args.length == 0) {

			printUsage();
			System.exit(1);

		} else {

			for (int i = 0; i < args.length; i++) {

				if (args[i].equals("-h")) {

					printUsage();
					System.exit(0);

				} else if (args[i].equals("-output")) {

					setOutputLevel(args[++i]);
					logger.finest("Set output level to " + outputLevel);

				} else if (args[i].equals("-debug")) {

					debug = true;
					logger.finest("Running in debug mode");

				} else if (args[i].equals("-repair")) {

					doRepair = true;
					logger.finest("Will do repairs if appropriate");

				} else if (args[i].equals("-showrepair")) {

					showRepair = true;
					logger.finest("Will show repairs");

				} else if (args[i].equals("-d")) {

					// undocumented feature - if COREDBS appears in the -d argument, it is
					// expanded
					// to the contents of CORE_DB_REGEXP
					i++;
					databaseRegexps.add(args[i].replaceAll("COREDBS", CORE_DB_REGEXP));
					logger.finest("Added database regular expression " + args[i]);

				} else if (args[i].equals("-d2")) {

					// undocumented feature - if COREDBS appears in the -d argument, it is
					// expanded
					// to the contents of CORE_DB_REGEXP
					i++;
					secondaryDatabaseRegexps.add(args[i].replaceAll("COREDBS", CORE_DB_REGEXP));
					logger.finest("Added database regular expression for secondary server " + args[i]);

				} else if (args[i].equals("-config")) {

					i++;
					PROPERTIES_FILE = args[i];
					logger.finest("Will read properties from " + PROPERTIES_FILE);

				} else if (args[i].equals("-length")) {

					outputLineLength = Integer.parseInt(args[++i]);
					logger.finest((outputLineLength > 0 ? "Will break output lines at column " + outputLineLength : "Will not break output lines"));

				} else if (args[i].equals("-species")) {

					String speciesStr = args[++i];
					if (Species.resolveAlias(speciesStr) != Species.UNKNOWN) {
						globalSpecies = Species.resolveAlias(speciesStr);
						logger.finest("Will override guessed species with " + globalSpecies + " for all databases");
					} else {
						logger.severe("Argument " + speciesStr + " to -species not recognised");
					}

				} else if (args[i].equals("-type")) {

					String typeStr = args[++i];
					if (DatabaseType.resolveAlias(typeStr) != DatabaseType.UNKNOWN) {
						globalType = DatabaseType.resolveAlias(typeStr);
						logger.finest("Will override guessed database types with " + globalType + " for all databases");
					} else {
						logger.severe("Argument " + typeStr + " to -type not recognised");
					}

				} else if (args[i].equals("-resultsbydb")) {

					printResultsByDatabase = true;
					logger.finest("Will print results by database");

				} else if (args[i].equals("-nofailuretext")) {

					printFailureText = false;
					logger.finest("Will not print failure text.");

				} else if (args[i].equals("-skipslow")) {

					skipSlow = true;
					logger.finest("Will skip long-running tests.");

				} else if (args[i].equals("-nologging")) {

					noLogging = true;

				} else if (args[i].equals("-host")) {

					customHost = args[++i];

				} else if (args[i].equals("-port")) {

					customPort = args[++i];

				} else if (args[i].equals("-user")) {

					customUser = args[++i];

				} else if (args[i].equals("-pass") || args[i].equals("-password")) {

					customPass = args[++i];

				} else {

					groupsToRun.add(args[i]);
					logger.finest("Added " + args[i] + " to list of groups to run");

				}

			}

			if (databaseRegexps.size() == 0) {

				System.err.println("No databases specified!");
				System.exit(1);

			}

			// print matching databases if no tests specified
			if (groupsToRun.size() == 0 && databaseRegexps.size() > 0) {

				Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE, false);

				for (Iterator<String> it = databaseRegexps.iterator(); it.hasNext();) {

					String databaseRegexp = it.next();
					System.out.println("Databases that match the regular expression " + databaseRegexp + ":");

					for (DatabaseRegistryEntry entry : mainDatabaseRegistry.getAll()) {
						System.out.println("  " + entry);
					}
				}
			}

		}

	}

	// parseCommandLine

	// -------------------------------------------------------------------------

	private void setupLogging() {

		logger.setUseParentHandlers(false); // stop parent logger getting the message

		Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());

		logger.addHandler(myHandler);

		if (noLogging) {

			logger.setLevel(Level.OFF);

		} else {

			logger.setLevel(Level.WARNING); // default - only print important messages

		}

		if (debug) {

			logger.setLevel(Level.FINEST);

		}

		// logger.info("Set logging level to " + logger.getLevel().getName());
	}

	// setupLogging
	// -------------------------------------------------------------------------

	/**
	 * Called when a message is to be stored in the report manager.
	 * 
	 * @param reportLine
	 *          The message to store.
	 */
	public void message(ReportLine reportLine) {

		String level = "ODD    ";

		System.out.print(".");
		System.out.flush();

		if (reportLine.getLevel() < outputLevel) {
			return;
		}

		if (!reportLine.getDatabaseName().equals(lastDatabase)) {
			outputBuffer.add("  " + reportLine.getDatabaseName());
			lastDatabase = reportLine.getDatabaseName();
		}

		switch (reportLine.getLevel()) {
		case (ReportLine.PROBLEM):
			level = "PROBLEM";
			break;
		case (ReportLine.WARNING):
			level = "WARNING";
			break;
		case (ReportLine.INFO):
			level = "INFO   ";
			break;
		case (ReportLine.CORRECT):
			level = "CORRECT";
			break;
		default:
			level = "PROBLEM";
		}

		outputBuffer.add("    " + level + ":  " + lineBreakString(reportLine.getMessage(), outputLineLength, "              "));
	}

	/**
	 * Called just before a test case is run.
	 * 
	 * @param testCase
	 *          The test case about to be run.
	 * @param dbre
	 *          The database which testCase is to be run on, or null of no/several databases.
	 */
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

		String name;
		name = testCase.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		System.out.print(name + " ");
		if (dbre != null) {
			System.out.print("[" + dbre.getName() + "] ");
		}
		System.out.flush();

	}

	/**
	 * Should be called just after a test case has been run.
	 * 
	 * @param testCase
	 *          The test case that was run.
	 * @param result
	 *          The result of testCase.
	 * @param dbre
	 *          The database which testCase was run on, or null of no/several databases.
	 */
	public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

		// long duration = (System.currentTimeMillis() - startTime) / 1000;

		System.out.println((result ? " PASSED" : " FAILED"));

		/*
		 * lastDatabase = ""; Iterator it = outputBuffer.iterator(); while (it.hasNext()) { System.out.println((String)it.next()); }
		 * outputBuffer.clear();
		 */

	}

	private String lineBreakString(String mesg, int maxLen, String indent) {

		if (mesg.length() <= maxLen || maxLen == 0) {
			return mesg;
		}

		int lastSpace = mesg.lastIndexOf(" ", maxLen);
		if (lastSpace > 15) {
			return mesg.substring(0, lastSpace) + "\n" + indent + lineBreakString(mesg.substring(lastSpace + 1), maxLen, indent);
		} else {
			return mesg.substring(0, maxLen) + "\n" + indent + lineBreakString(mesg.substring(maxLen), maxLen, indent);
		}
	}

	// --------------------------------------------------------------------------
	/**
	 * Check if custom* variables are set and if so, override settings read from properties file.
	 */
	private void setCustomProperties() {

		if (customHost != null) {

			System.setProperty("host", customHost);
			logger.finest("Set database host to " + customHost);

		}

		if (customPort != null) {

			System.setProperty("port", customPort);
			logger.finest("Set database port to " + customPort);

		}

		if (customUser != null) {

			System.setProperty("user", customUser);
			logger.finest("Set database user to " + customUser);

		}

		if (customPass != null) {

			System.setProperty("password", customPass);
			logger.finest("Set database password to " + customPass);

		}

	}

	// --------------------------------------------------------------------------

}

// TextTestRunner
