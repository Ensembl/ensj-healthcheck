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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * Submit multiple NodeDatabaseTestRunners in parallel.
 */
public class ParallelDatabaseTestRunner extends TestRunner {

	protected boolean debug = false;

	// ---------------------------------------------------------------------
	/**
	 * Main run method.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	protected void run(String[] args) {

		parseCommandLine(args);

		setupLogging();

		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

		parseProperties();

		ReportManager.connectToOutputDatabase();

		ReportManager.createDatabaseSession();

		List databasesAndGroups = getDatabasesAndGroups();

		submitJobs(databasesAndGroups, ReportManager.getSessionID());

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

		new ParallelDatabaseTestRunner().run(args);

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

			}

		}

	} // parseCommandLine

	// -------------------------------------------------------------------------

	private void printUsage() {

		System.out.println("\nUsage: ParallelDatabaseTestRunner {options} \n");
		System.out.println("Options:");
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

	// ---------------------------------------------------------------------

	private void parseProperties() {

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
	 * Create a list; of database regexps and test case groups Parsed from the
	 * output.databases property, which should be of the form
	 * regexp1:group1,regexp2:group2 etc
	 */
	private List getDatabasesAndGroups() {

		return Arrays.asList(System.getProperty("output.databases").split(","));

	}

	// ---------------------------------------------------------------------

	private void submitJobs(List databasesAndGroups, long sessionID) {

		String dir = System.getProperty("user.dir");

		String s = null;
		
		Iterator it = databasesAndGroups.iterator();
		
		while (it.hasNext()) {
			
			String[] databaseAndGroup = ((String) it.next()).split(":");
			String database = databaseAndGroup[0];
			String group = databaseAndGroup[1];

			String[] cmd = { "bsub", "-q", "long", "-o", "healthcheck_%J.out", "-e", "healthcheck_%J.err",
					dir + File.separator + "run-healthcheck-node.sh", "-d", database, "-group", group, "-session", "" + sessionID };

			try {
				
				Process p = Runtime.getRuntime().exec(cmd);
				System.out.println("Submitted job with database regexp " + database + " and group " + group + ", session ID " + sessionID);
			
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				while ((s = stdInput.readLine()) != null) {
					System.out.println(s);
				}

				while ((s = stdError.readLine()) != null) {
				}
			} catch (Exception ioe) {
				System.err.println("Error in head job " + ioe.getMessage());
			}
		}
	}

	// ---------------------------------------------------------------------

} // ParallelDatabaseTestRunner
