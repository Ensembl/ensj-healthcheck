/*
 Copyright (C) 2004 EBI, GRL
 
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

import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

import java.util.*;
import java.util.logging.*;

/**
 * Subclass of TestRunner intended for running tests from the command line.
 */
public class TextTestRunner extends TestRunner implements Reporter {

	private static String version = "$Id$";

	private String databaseRegexp = null;
	private boolean debug = false;

	public ArrayList outputBuffer = new ArrayList();

	private String lastDatabase = "";

	private int outputLineLength = 65;

	private TestRegistry testRegistry;
	private DatabaseRegistry databaseRegistry;

	private Species globalSpecies = null;
	private DatabaseType globalType = null;

	// -------------------------------------------------------------------------

	/**
	 * Command-line run method.
	 * 
	 * @param args The command-line arguments.
	 */
	public static void main(String[] args) {

		new TextTestRunner().run(args);

	} // main

	// -----------------------------------------------------------------

	private void run(String[] args) {

		Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

		parseCommandLine(args);

		setupLogging();

		ReportManager.setReporter(this);

		testRegistry = new TestRegistry();
		databaseRegistry = new DatabaseRegistry(databaseRegexp);

		if (globalSpecies != null) {
			databaseRegistry.setSpeciesOfAll(globalSpecies);
		}
		if (globalType != null) {
			databaseRegistry.setTypeOfAll(globalType);
		}

		runAllTests(databaseRegistry, testRegistry);

		ConnectionPool.closeAll();

	} // run

	// -------------------------------------------------------------------------
	private void printUsage() {

		System.out.println("\nUsage: TextTestRunner {options} {group1} {group2} ...\n");
		System.out.println("Options:");
		System.out.println("  -d regexp       Use the given regular expression to decide which databases to use.");
		System.out.println("  -h              This message.");
		System.out.println("  -output level   Set output level; level can be one of ");
		System.out.println("                  none      nothing is printed");
		System.out.println("                  problem   only problems are reported");
		System.out.println("                  correct   only correct results (and problems) are reported");
		System.out.println("                  summary   only summary info (and problems, and correct reports) are reported");
		System.out.println("                  info      info (and problem, correct, summary) messages reported");
		System.out.println("                  all       everything is printed");
		System.out.println("  -species s      Use s as the species for all databases instead of trying to guess the species from the name");
		System.out.println("  -type t         Use 2 as the type for all databases instead of trying to guess the type from the name");
		System.out.println("  -debug          Print debugging info (for developers only)");
		System.out.println("  -config file    Read configuration information from file instead of " + PROPERTIES_FILE);
		System.out.println("  -repair         If appropriate, carry out repair methods on test cases that support it");
		System.out.println("  -showrepair     Like -repair, but the repair is NOT carried out, just reported.");
		System.out.println("  -length n       Break output lines at n columns; default is " + outputLineLength + ". 0 means never break");
		System.out.println("  group1          Names of groups of test cases to run.");
		System.out.println("                  Note each test case is in a group of its own with the name of the test case.");
		System.out.println("                  This allows individual tests to be run if required.");
		System.out.println("");
		System.out.println("If no tests or test groups are specified, and a database regular expression is given with -d, the matching databases are shown. ");
		System.out.println("");
		System.out.println("Currently available tests:");

		List tests = testRegistry.findAllTests();
		Collections.sort(tests, new TestComparator());
		Iterator it = tests.iterator();
		while (it.hasNext()) {
			EnsTestCase test = (EnsTestCase)it.next();
			System.out.print(test.getShortTestName() + " ");
		}

		System.out.println("");

	}

	/**
	 * Return the CVS version string for this class.
	 * 
	 * @return The version.
	 */
	public String getVersion() {

		// strip off first and last few chars of version since these are only used by CVS
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

					i++;
					databaseRegexp = args[i];
					logger.finest("Set database regular expression to " + databaseRegexp);

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
						logger.severe("Argument " + speciesStr + " to -species argument not recognised");
					}

				} else if (args[i].equals("-type")) {

					String typeStr = args[++i];
					if (DatabaseType.resolveAlias(typeStr) != DatabaseType.UNKNOWN) {
						globalType = DatabaseType.resolveAlias(typeStr);
						logger.finest("Will override guessed database types with " + globalType + " for all databases");
					} else {
						logger.severe("Argument " + typeStr + " to -typeargument not recognised");
					}

				} else {

					groupsToRun.add(args[i]);
					logger.finest("Added " + args[i] + " to list of groups to run");

				}

			}

			if (databaseRegexp == null) {

				System.err.println("No databases specified!");
				System.exit(1);

			}

			// print matching databases if no tests specified
			if (groupsToRun.size() == 0 && databaseRegexp != null) {
				System.out.println("Databases that match the regular expression '" + databaseRegexp + "':");
				String[] names = getListOfDatabaseNames(".*");
				for (int i = 0; i < names.length; i++) {
					System.out.println("  " + names[i]);
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
		logger.setLevel(Level.WARNING); // default - only print important messages

		if (debug) {

			logger.setLevel(Level.FINEST);

		}

		//logger.info("Set logging level to " + logger.getLevel().getName());
	}

	// setupLogging
	// -------------------------------------------------------------------------

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
			case (ReportLine.PROBLEM) :
				level = "PROBLEM";
				break;
			case (ReportLine.WARNING) :
				level = "WARNING";
				break;
			case (ReportLine.INFO) :
				level = "INFO   ";
				break;
			case (ReportLine.CORRECT) :
				level = "CORRECT";
				break;
		}

		outputBuffer.add("    " + level + ":  " + lineBreakString(reportLine.getMessage(), outputLineLength, "              "));
	}

	public void startTestCase(EnsTestCase testCase) {
		String name;
		name = testCase.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		System.out.print(name + " ");
		System.out.flush();
	}

	public void finishTestCase(EnsTestCase testCase, boolean result) {

		System.out.println(result ? " PASSED" : " FAILED");

		lastDatabase = "";
		Iterator it = outputBuffer.iterator();
		while (it.hasNext()) {
			System.out.println((String)it.next());
		}
		outputBuffer.clear();

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
}

// TextTestRunner
