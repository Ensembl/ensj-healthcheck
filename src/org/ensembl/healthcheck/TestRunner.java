/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Publicpr License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.OrderedDatabaseTestCase;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * <p>
 * TestRunner is a base class that provides utilities for running tests -
 * logging, the ability to find and run tests from certain locations, etc.
 * </p>
 */

public class TestRunner {

	/** List that holds an instance of each test. */
	protected List allTests;

	/** The List of group names (as Strings) that will be run. */
	protected List<String> groupsToRun;

	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	/** Output level used by ReportManager */
	protected int outputLevel = ReportLine.PROBLEM;

	// EG change to public to allow Database runner to modify this
	/** The name of the file where configuration is stored */
	// public static String propertiesFile = "";

	private static String propertiesFile = "database.properties";

	public static String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	/** Flag to determine whether repairs will be shown if appropriate */
	protected boolean showRepair = false;

	/** Flag to determine whether repairs will be carried out if appropriate */
	protected boolean doRepair = false;

	// -------------------------------------------------------------------------
	/** Creates a new instance of TestRunner */

	public TestRunner() {

		groupsToRun = new ArrayList<String>();

	} // TestRunner

	// -------------------------------------------------------------------------
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
	protected void runAllTests(DatabaseRegistry databaseRegistry,
			TestRegistry testRegistry, boolean skipSlow) {

		int numberOfTestsRun = 0;

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
						logger.info("Running " + testCase.getName() + " ["
								+ database.getName() + "]");

						testCase.types();
						
						boolean result = testCase.run(database);

						ReportManager
								.finishTestCase(testCase, result, database);
						logger.info(testCase.getName() + " ["
								+ database.getName() + "]"
								+ (result ? "PASSED" : "FAILED"));

						checkRepair(testCase, database);
						numberOfTestsRun++;

					} catch (Throwable e) {
						logger.warning("Could not execute test "
								+ testCase.getName() + " on "
								+ database.getName() + ": " + e.getMessage());
					}

				} else {
					logger.info("Skipping long-running test "
							+ testCase.getName());

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

					ReportManager.finishTestCase(testCase, result, null);
					logger.info(testCase.getName() + " "
							+ (result ? "PASSED" : "FAILED"));

					numberOfTestsRun++;
				} catch (Throwable e) {
				  //TODO If we had a throwable then we should mark the test as failed 
				  
					// catch and log unexpected exceptions
					logger.warning("Could not execute test "
							+ testCase.getName() + ": " + e.getMessage());
				}
			} else {

				logger.info("Skipping long-running test " + testCase.getName());

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

				ReportManager.finishTestCase(testCase, result, null);
				logger.info(testCase.getName() + " "
						+ (result ? "PASSED" : "FAILED"));
			} catch (Throwable e) {
			  //TODO If we had a throwable then we should mark the test as failed
			  
				// catch and log unexpected exceptions
				logger.warning("Could not execute test " + testCase.getName()
						+ ": " + e.getMessage());
			}

			numberOfTestsRun++;

		} // foreach test

		// --------------------------------

		if (numberOfTestsRun == 0) {
			logger.warning("Warning: no tests were run.");
		}

	} // runAllTests

	// ---------------------------------------------------------------------
	/**
	 * Check if the given testcase can repair errors on the given database.
	 */
	private void checkRepair(EnsTestCase testCase,
			DatabaseRegistryEntry database) {

		// check for show/do repair
		if (testCase.canRepair()) {
			if (showRepair) {
				((Repair) testCase).show(database);
			}
			if (doRepair) {
				((Repair) testCase).repair(database);
			}
		}

	} // checkRepair

	// -------------------------------------------------------------------------
	/**
	 * Get the union of all the test groups.
	 * 
	 * @param tests
	 *            The tests to check.
	 * @return An array containing the names of all the groups that any member
	 *         of tests is a member of.
	 */
	public String[] listAllGroups(List<EnsTestCase> tests) {

		ArrayList<String> g = new ArrayList<String>();
		Iterator<EnsTestCase> it = tests.iterator();
		while (it.hasNext()) {
			List<String> thisTestsGroups = it.next().getGroups();
			Iterator<String> it2 = thisTestsGroups.iterator();
			while (it2.hasNext()) {
				String group = it2.next();
				if (!g.contains(group)) {
					g.add(group);
				}
			}
		}

		return (String[]) g.toArray(new String[g.size()]);

	} // listAllGroups

	// -------------------------------------------------------------------------
	/**
	 * List all the tests in a particular group.
	 * 
	 * @param tests
	 *            The tests to check.
	 * @param group
	 *            The group name to check.
	 * @return An array containing the names whatever tests are a member of
	 *         group.
	 */
	public String[] listTestsInGroup(List tests, String group) {

		ArrayList g = new ArrayList();
		Iterator it = tests.iterator();
		while (it.hasNext()) {
			EnsTestCase test = (EnsTestCase) it.next();
			if (test.inGroup(group)) {
				g.add(test.getShortTestName());
			}
		}

		return (String[]) g.toArray(new String[g.size()]);

	} // listTestsInGroup

	// -------------------------------------------------------------------------
	/**
	 * Print (to stdout) out a list of test reports, keyed by the test type.
	 * 
	 * @param level
	 *            The lowest report level (see ReportLine) to print. Reports
	 *            with a level lower than this are not printed.
	 * @param printFailureText
	 *            If true, print result of getFailureText() for each test.
	 */
	public void printReportsByTest(int level, boolean printFailureText) {

		System.out.println("\n---- RESULTS BY TEST CASE ----");
		Map map = ReportManager.getAllReportsByTestCase(level);
		Set keys = map.keySet();
		Iterator it = keys.iterator();

		while (it.hasNext()) {

			String test = (String) it.next();

			List lines = (List) map.get(test);

			if (lines.size() > 0) {

				System.out.print("\n" + test);

				// print failure text if appropriate
				String failureText = "";
				try {
					EnsTestCase testObj = (EnsTestCase) (Class.forName(test)
							.newInstance());
					String teamResponsible = testObj
							.getPrintableTeamResponsibleString();
					if (teamResponsible == null) {
						teamResponsible = "Not set";
					}
					System.out.println(" [Team responsible: " + teamResponsible
							+ "]");

					failureText = testObj.getFailureText();
					if (testObj.getEffect() != null) {
						failureText += testObj.getEffect() + "\n";
					}
					if (testObj.getFix() != null) {
						failureText += testObj.getFix() + "\n";
					}
				} catch (Exception e) {
					System.err.println("Error, can't instantiate object ");
					e.printStackTrace();
				}
				if (printFailureText && failureText.length() > 0) {
					System.out.println("Note: " + failureText);
				}

				Iterator it2 = lines.iterator();
				while (it2.hasNext()) {
					ReportLine reportLine = (ReportLine) it2.next();
					if (reportLine.getLevel() >= level) {
						String dbName = reportLine.getDatabaseName();
						if (dbName.equals("no_database")) {
							dbName = "";
						} else {
							dbName = reportLine.getDatabaseName() + ": ";
						}
						System.out.println("  " + dbName
								+ reportLine.getMessage());
					} // if level
				} // while it2
			} // if lines

		} // while it

	} // printReportsByTest

	// -------------------------------------------------------------------------
	/**
	 * Print (to stdout) a list of test results, ordered by database.
	 * 
	 * @param level
	 *            The minimum level of report to print - see ReportLine. Reports
	 *            below this level are not printed.
	 */
	public void printReportsByDatabase(int level) {

		System.out.println("\n---- RESULTS BY DATABASE ----");
		Map map = ReportManager.getAllReportsByDatabase(level);
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			System.out.print("\n" + key + ": ");
			List lines = (List) map.get(key);
			int nProblems = lines.size();
			if (nProblems == 0) {
				System.out.println("No problems found");
			} else {
				String s = (nProblems == 1) ? "" : "s";
				System.out.println(nProblems + " problem" + s + " found");
				Iterator it2 = lines.iterator();
				while (it2.hasNext()) {
					ReportLine reportLine = (ReportLine) it2.next();
					if (reportLine.getLevel() >= level) {
						System.out.println(" "
								+ reportLine.getShortTestCaseName() + ": "
								+ reportLine.getMessage());
					} // if level
				} // while it2
			} // if nProblems
		} // while it

	} // printReportsByDatabase

	// -------------------------------------------------------------------------
	/**
	 * Set the outputLevel variable based on an input string (probably from the
	 * command line)
	 * 
	 * @param str
	 *            The output level to use.
	 */
	protected void setOutputLevel(String str) {

		String lstr = str.toLowerCase();
		if (lstr.equals("all")) {
			outputLevel = ReportLine.ALL;
		} else if (lstr.equals("none")) {
			outputLevel = ReportLine.NONE;
		} else if (lstr.equals("problem")) {
			outputLevel = ReportLine.PROBLEM;
		} else if (lstr.equals("correct")) {
			outputLevel = ReportLine.CORRECT;
		} else if (lstr.equals("warning")) {
			outputLevel = ReportLine.WARNING;
		} else if (lstr.equals("info")) {
			outputLevel = ReportLine.INFO;
		} else {
			logger.warning("Output level " + str
					+ " not recognised; using 'all'");
		}

	} // setOutputLevel

	// -------------------------------------------------------------------------
	/**
	 * Set the output level.
	 * 
	 * @param l
	 *            The new output level.
	 */
	public void setOutputLevel(int l) {

		outputLevel = l;
		logger.finest("Set outputLevel to " + outputLevel);

	} // setOutputLevel

	// -------------------------------------------------------------------------

	/**
	 * Get the current output level.
	 * 
	 * @return The current output level. See ReportLine.
	 */
	public int getOutputLevel() {

		return outputLevel;

	} // getOutputLevel

	// -------------------------------------------------------------------------

} // TestRunner

// -------------------------------------------------------------------------
