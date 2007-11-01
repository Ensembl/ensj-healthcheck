/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * TestRunner optimised for outputting results to a database.
 */
public class DatabaseTestRunner extends TestRunner implements Reporter {

	protected boolean debug = false;

	private boolean deletePrevious = false;

	private long testStartTime;

	private static String TIMINGS_FILE = "timings.txt";

	// ---------------------------------------------------------------------
	/**
	 * Main run method.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	protected void run(String[] args) {

		ReportManager.setReporter(this);

		parseCommandLine(args);

		setupLogging();

		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

		parseProperties();

		groupsToRun = getGroupsFromProperties();

		List databaseRegexps = getDatabasesFromProperties();

		TestRegistry testRegistry = new TestRegistry();

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(databaseRegexps, null, null);
		if (databaseRegistry.getAll().length == 0) {
			logger.warning("Warning: no database names matched any of the database regexps given");
		}

		ReportManager.connectToOutputDatabase();

		if (deletePrevious) {
			ReportManager.deletePrevious();
		}

		ReportManager.createDatabaseSession();

		Utils.deleteFile(TIMINGS_FILE);
		
		runAllTests(databaseRegistry, testRegistry, false);

		ReportManager.endDatabaseSession();

		ConnectionPool.closeAll();

	} // run

	// ---------------------------------------------------------------------

	/**
	 * Command-line entry point.
	 * 
	 * @param args
	 *          Command line args.
	 */
	public static void main(String[] args) {

		new DatabaseTestRunner().run(args);

	} // main

	// -------------------------------------------------------------------------

	private void parseCommandLine(String[] args) {

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-h")) {

				printUsage();
				System.exit(0);

			} else if (args[i].equals("-debug")) {

				debug = true;
				logger.finest("Running in debug mode");

			} else if (args[i].equals("-delete")) {

				deletePrevious = true;
				logger.finest("Will delete previous data");

			}

		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: DatabaseTestRunner {options} \n");
		System.out.println("Options:");
		System.out.println("  -delete         Delete all previous entries from output database");
		System.out.println("  -h              This message.");
		System.out.println("  -debug          Print debugging info");
		System.out.println();
		System.out.println("All configuration information is read from the file database.properties. ");
		System.out.println("See the comments in that file for information on which options to set.");

	}

	// ---------------------------------------------------------------------

	protected void setupLogging() {

		// stop parent logger getting the message
		logger.setUseParentHandlers(false);

		Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());

		logger.addHandler(myHandler);
		logger.setLevel(Level.WARNING);

		if (debug) {

			logger.setLevel(Level.FINEST);

		}

	} // setupLogging

	// -------------------------------------------------------------------------
	// Implementation of Reporter interface

	/**
	 * Called when a message is to be stored in the report manager.
	 * 
	 * @param reportLine
	 *          The message to store.
	 */
	public void message(ReportLine reportLine) {

	}

	// ---------------------------------------------------------------------

	/**
	 * Called just before a test case is run.
	 * 
	 * @param testCase
	 *          The test case about to be run.
	 * @param dbre
	 *          The database which testCase is to be run on, or null of no/several
	 *          databases.
	 */
	public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

		testStartTime = System.currentTimeMillis();

	}

	// ---------------------------------------------------------------------

	/**
	 * Should be called just after a test case has been run.
	 * 
	 * @param testCase
	 *          The test case that was run.
	 * @param result
	 *          The result of testCase.
	 * @param dbre
	 *          The database which testCase was run on, or null of no/several
	 *          databases.
	 */
	public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

		long duration = System.currentTimeMillis() - testStartTime;

		String str = duration + "\t";
		if (dbre != null) {
			str += dbre.getName() + "\t";
		}
		str += testCase.getShortTestName() + "\t";
		str += Utils.formatTimeString(duration);

		Utils.writeStringToFile(TIMINGS_FILE, str, true, true);

	}

	// ---------------------------------------------------------------------

	private void parseProperties() {

		if (System.getProperty("output.groups") == null) {
			System.err.println("No tests or groups specified in " + PROPERTIES_FILE);
			System.exit(1);
		}

		if (System.getProperty("output.databases") == null) {
			System.err.println("No databases specified in " + PROPERTIES_FILE);
			System.exit(1);
		}
		
		if (System.getProperty("output.release") == null) {
			System.err.println("No release specified in " + PROPERTIES_FILE + " - please add an output.release property");
			System.exit(1);
		}

	}

	// ---------------------------------------------------------------------

	/**
	 * Get a list of test groups by parsing the appropriate property.
	 * 
	 * @return the list of group or test names.
	 */
	private List getGroupsFromProperties() {

		String[] groups = System.getProperty("output.groups").split(",");

		return Arrays.asList(groups);

	}

	// ---------------------------------------------------------------------

	/**
	 * Get a list of databases by parsing the appropriate property.
	 * 
	 * @return The list of database names or patterns.
	 */
	private List getDatabasesFromProperties() {

		String[] dbs = System.getProperty("output.databases").split(",");

		return Arrays.asList(dbs);

	}

	// ---------------------------------------------------------------------

} // DatabaseTestRunner
