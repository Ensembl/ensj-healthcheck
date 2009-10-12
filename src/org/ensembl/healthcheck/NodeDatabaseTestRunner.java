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

import java.util.ArrayList;
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
 * Subclass of DatabaseTestRunner optimised for running a single group of tests against
 * a single set of databases. Intended to be called by ParallelDatabaseTestRunner to
 * allow parallel running of healthchecks. Same as DatabaseTestRunner but group and
 * databases are specified on the command-line rather than in the properties file.
 */
public class NodeDatabaseTestRunner extends DatabaseTestRunner implements Reporter {

	private boolean debug = false;

	private boolean deletePrevious = false;

	private List databaseRegexps;
	
	private long sessionID = -1;
	
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
		
		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE, false);
		
		TestRegistry testRegistry = new TestRegistry();

		DatabaseRegistry databaseRegistry = new DatabaseRegistry(databaseRegexps, null, null, false);
		if (databaseRegistry.getAll().length == 0) {
			logger.warning("Warning: no database names matched any of the database regexps given");
		}

		ReportManager.connectToOutputDatabase();

		if (deletePrevious) {
			ReportManager.deletePrevious();
		}

		runAllTests(databaseRegistry, testRegistry, false);

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

		new NodeDatabaseTestRunner().run(args);

	} // main

	// -------------------------------------------------------------------------

	private void parseCommandLine(String[] args) {

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-h") || args[i].equals("-help")) {

				printUsage();
				System.exit(0);

			} else if (args[i].equals("-debug")) {

				debug = true;
				logger.finest("Running in debug mode");

			} else if (args[i].equals("-group")) {
				
				i++;
				groupsToRun.add(args[i]);
				logger.finest("Added " + args[i] + " to group");
				
			} else if (args[i].equals("-d")) {
				i++;
				databaseRegexps = new ArrayList();
				databaseRegexps.add(args[i]);
				logger.finest("Database regexp: " + args[i]);
				
			} else if (args[i].equals("-session")) {
				
				i++;
				sessionID = Integer.parseInt(args[i]);
				ReportManager.setSessionID(sessionID);
				logger.finest("Will use session ID " + sessionID);
				
			}
			
		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: NodeDatabaseTestRunner {options} \n");
		System.out.println("Options:");
		System.out.println("  -d {regexp}     The databases on which to run the group of tests (required)");
		System.out.println("  -group {group}  The group of tests to run (required)");
		System.out.println("  -session {id}   The session ID to use (required)");
		System.out.println("  -h              This message.");
		System.out.println("  -debug          Print debugging info");
		System.out.println();
		System.out.println("All configuration information is read from the file database.properties. ");
		System.out.println("See the comments in that file for information on which options to set.");

	}

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
		
	    if (dbre != null) {
		ReportManager.info(testCase, dbre.getConnection(), "#Started");
	    }


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

	    if (dbre !=null) {
		ReportManager.info(testCase, dbre.getConnection(), "#Ended");
	    }
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

	// ---------------------------------------------------------------------

} // NodeDatabaseTestRunner
