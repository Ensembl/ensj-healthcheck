/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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
	protected List<EnsTestCase> allTests;

	/** The List of group names (as Strings) that will be run. */
	protected List<String> groupsToRun;

	/** The logger to use for this class */
	protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	/** Output level used by ReportManager */
	protected int outputLevel = ReportLine.PROBLEM;

	// EG change to public to allow Database runner to modify this
	/** The name of the file where configuration is stored */
	// public static String propertiesFile = "";

	private static String propertiesFile = "database.properties";

	public static String getPropertiesFile() {
		return propertiesFile;
	}

	public static void setPropertiesFile(String propertiesFile) {
		TestRunner.propertiesFile = propertiesFile;
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

// If a healthcheck database is being used and nothing has been propagated for the database being tested, skip
                        if (ReportManager.usingDatabase()) {
                                boolean propagated = ReportManager.hasPropagated(database);
                                if (!propagated) {
                                        continue;
                                }
                        }

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
					  String msg = "Could not execute test "
                + testCase.getName() + " on "
                + database.getName() + ": " + e.getMessage();
					  logger.log(Level.WARNING, msg, e);
					  //TODO Get the logger to do this
					  e.printStackTrace();
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
          String msg = "Could not execute test "
              + testCase.getName() + ": " + e.getMessage();
          logger.log(Level.WARNING, msg, e);
        //TODO Get the logger to do this
          e.printStackTrace();
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
        String msg = "Could not execute test "
            + testCase.getName() + ": " + e.getMessage();
        logger.log(Level.WARNING, msg, e);
      //TODO Get the logger to do this
        e.printStackTrace();
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
	protected void checkRepair(EnsTestCase testCase,
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
		for(EnsTestCase test: tests) {
			List<String> thisTestsGroups = test.getGroups();
			for(String group: thisTestsGroups) {
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
	public String[] listTestsInGroup(List<EnsTestCase> tests, String group) {

		ArrayList<String> g = new ArrayList<String>();
		for(EnsTestCase test: tests) {
			if (test.inGroup(group)) {
				g.add(test.getShortTestName());
			}
		}

		return g.toArray(new String[g.size()]);

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
		Map<String,List<ReportLine>> map = ReportManager.getAllReportsByTestCase(level);

		for(String test: map.keySet()) {

			List<ReportLine> lines = map.get(test);

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

				for(ReportLine reportLine: lines) {
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
		Map<String,List<ReportLine>> map = ReportManager.getAllReportsByDatabase(level);
		for(String key: map.keySet()) {
			System.out.print("\n" + key + ": ");
			List<ReportLine> lines = map.get(key);
			int nProblems = lines.size();
			if (nProblems == 0) {
				System.out.println("No problems found");
			} else {
				String s = (nProblems == 1) ? "" : "s";
				System.out.println(nProblems + " problem" + s + " found");
				for(ReportLine reportLine: lines) {
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
