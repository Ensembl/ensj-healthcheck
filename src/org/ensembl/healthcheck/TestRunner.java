/*
  Copyright (C) 2004 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Publicpr
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

import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;
import java.sql.*;
import java.io.*;

import org.ensembl.healthcheck.testcase.*;

import org.ensembl.healthcheck.util.*;

/**
 * <p>TestRunner is a base class that provides utilities for running tests -
 * logging, the ability to find and run tests from certain locations, etc.</p>
 */

public class TestRunner {

	/** List that holds an instance of each test. */
	protected List allTests;
	/** The List of group names (as Strings) that will be run. */
	protected List groupsToRun;
	/** If set, database names are filtered with this regular expression before the regexp built into the tests. */
	protected String preFilterRegexp;
	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");
	/** Output level used by ReportManager */
	protected int outputLevel = ReportLine.ALL;
	/** The name of the file where configuration is stored */
	protected static String PROPERTIES_FILE = "database.properties";
	/** Flag to determine whether repairs will be shown if appropriate */
	protected boolean showRepair = false;
	/** Flag to determine whether repairs will be carried out if appropriate */
	protected boolean doRepair = false;

	/** Where to start looking for testcase classes */
  private static final String TESTCASE_SEARCH_PACKAGE = "org.ensembl.healthcheck";

	// -------------------------------------------------------------------------
	/** Creates a new instance of TestRunner */

	public TestRunner() {

		groupsToRun = new ArrayList();

	} // TestRunner
	
	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the schema names .
	 * @return An array of the schema names.
	 */
	public String[] getAllSchemaNames() {

		Connection conn;

		String[] schemaNames = null;

		// open connection
		try {

			conn =
				DBUtils.openConnection(System.getProperty("driver"),
					System.getProperty("databaseURL"),
					System.getProperty("user"),
					System.getProperty("password"));

			logger.fine("Opened connection to " + System.getProperty("databaseURL") + " as " + System.getProperty("user"));

			schemaNames = DBUtils.listDatabases(conn);

			logger.fine("Connection closed");

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

		return schemaNames;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of database names that match a particular regular expression.
	 * @param regexp The regular expression to match.
	 * @return An array of the matching database names (may be empty if none matched).
	 */
	public String[] getListOfDatabaseNames(String regexp) {

		Connection conn;

		String[] databaseNames = null;

		// open connection
		try {

			conn =
				DBUtils.openConnection(
					System.getProperty("driver"),
					System.getProperty("databaseURL"),
					System.getProperty("user"),
					System.getProperty("password"));

			logger.fine("Opened connection to " + System.getProperty("databaseURL") + " as " + System.getProperty("user"));

			databaseNames = DBUtils.listDatabases(conn, regexp, preFilterRegexp);

			if (databaseNames.length == 0) {
				logger.info("No database names matched");
			}

			//conn.close();

			logger.fine("Connection closed");

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

		return databaseNames;

	} // getDatabaseList

	// -------------------------------------------------------------------------
	/**
	 * Prints, to stdout, a list of the database names that match a given regular expression.
	 * @param regexp The regular expression to match.
	 */
	protected void showDatabaseList(String regexp) {

		logger.fine("Listing databases matching " + regexp + " :\n");

		String[] databaseList = getListOfDatabaseNames(regexp);

		for (int i = 0; i < databaseList.length; i++) {
			logger.fine("\t" + databaseList[i]);
		}

	} // showDatabaseList

	// -------------------------------------------------------------------------
	/**
	 * Finds all tests in several locations.
	 * A test case is a class that extends EnsTestCase.
	 * Test case classes found in more than one location are only added once.
	 * @return A List containing objects of the test case classes found.
	 */
	protected List findAllTests() {

		ArrayList allTests = new ArrayList();

		// some variables that will come in useful later on
 
		String packageName = TESTCASE_SEARCH_PACKAGE + ".testcase";
		String directoryName = packageName.replace('.', File.separatorChar);
		logger.finest("Package name: " + packageName + " Directory name: " + directoryName);
		String runDir = System.getProperty("user.dir") + File.separator;
System.out.println("Rundir is " + runDir);
		// places to look
		ArrayList locations = new ArrayList();
		locations.add(runDir + "src" + File.separator + directoryName); // same directory as this class
		locations.add(runDir + "build" + File.separator + directoryName); // ../build/<packagename>
		locations.add(runDir + "lib" + File.separator + "ensj-healthcheck.jar"); // ../lib

		// look in each of the locations defined above
		Iterator it = locations.iterator();
		while (it.hasNext()) {
			String location = (String)it.next();
			File dir = new File(location);
			if (dir.exists()) {
				if (location.lastIndexOf('.') > -1 && location.substring(location.lastIndexOf('.') + 1).equalsIgnoreCase("jar")) {
					addUniqueTests(allTests, findTestsInJar(location, packageName));
				} else {
					addUniqueTests(allTests, findTestsInDirectory(location, packageName));
				}
			} else {
				logger.info(dir.getAbsolutePath() + " does not exist, skipping.");
			} // if dir.exists
		}

		// --------------------------------
		logger.finer("Found " + allTests.size() + " unique test case classes.");

		/*Iterator it = allTests.iterator();
		while (it.hasNext()) {
		  EnsTestCase tc = (EnsTestCase)it.next();
		  System.out.println("#####" + tc.getTestName());
		}
		 */

		return allTests;

	} // findAllTests

	// -------------------------------------------------------------------------
	/**
	 * Run all the tests in a list. Also run show/repair methods if the test
	 * implements the Repair interface and the appropriate flags are set.
	 * @param allTests The tests to run, as objects.
	 * @param forceDatabases If true, use only the database name pattern specified
	 * on the command line, <em>not</em> the regular expression built in to the test case.
	 */
	protected void runAllTests(List allTests, boolean forceDatabases) {

		int numberOfTestsRun = 0;

		// check if allTests() has been populated
		if (allTests == null) {
			logger.warning("No tests to run! Call findAllTests() first?");
		}

		if (allTests.size() == 0) {
			logger.warning("Warning: no tests found!");
			return;
		}

		Iterator it = allTests.iterator();
		while (it.hasNext()) {

			EnsTestCase testCase = (EnsTestCase)it.next();

			if (testCase.inGroups(groupsToRun)) {
				logger.info("\nRunning test of type " + testCase.getClass().getName());
				if (testCase.isLongRunning()) {
					logger.info("Note that " + testCase.getClass().getName() + " may take a significant amount of time to run");
				}

				if (preFilterRegexp != null) {
					testCase.setPreFilterRegexp(preFilterRegexp);
				}

				if (forceDatabases) {
					// override built-in database regexp with the one specified on the command line
					testCase.setDatabaseRegexp(preFilterRegexp);
				}

				ReportManager.startTestCase(testCase);
				TestResult tr = testCase.run();
				ReportManager.finishTestCase(testCase, tr);

				numberOfTestsRun++;

				String passFail = tr.getResult() ? "PASSED" : "FAILED";
				logger.info(tr.getName() + " " + passFail);

				// check for show/repair
				if (testCase.canRepair()) {
					if (showRepair) {
						((Repair)testCase).show();
					}
					if (doRepair) {
						((Repair)testCase).repair();
					}

				}

			}
		}

		if (numberOfTestsRun == 0) {
			logger.warning("Warning: no tests were run.");
		}

	} // runAllTests

	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Get an iterator that will iterate over database connections whose names
	 * match a particular regular expression.
	 * @param databaseRegexp The regular expression to match.
	 * @return A DatabaseConnectionIterator object that will iterate over database
	 * Connections for databases whose names match the regular expression.
	 */
	public DatabaseConnectionIterator getDatabaseConnectionIterator(String databaseRegexp) {

		return new DatabaseConnectionIterator(
			System.getProperty("driver"),
			System.getProperty("databaseURL"),
			System.getProperty("user"),
			System.getProperty("password"),
			getListOfDatabaseNames(databaseRegexp));

	} // getDatabaseConnectionIterator

	// -------------------------------------------------------------------------
	/**
	 * Find all the tests (ie classes that extend EnsTestCase) in a directory.
	 * @param dir The base directory to look in.
	 * @param packageName The EnsTestCase package name.
	 * @return A list of tests in dir.
	 */
	public List findTestsInDirectory(String dir, String packageName) {

		logger.fine("Looking for tests in " + dir);

		ArrayList tests = new ArrayList();

		File f = new File(dir);

		// find all classes that extend org.ensembl.healthcheck.EnsTestCase
		ClassFileFilenameFilter cnff = new ClassFileFilenameFilter();
		File[] classFiles = f.listFiles(cnff);
		logger.finer("Examining " + classFiles.length + " class files ...");

		// check if each class file extends EnsTestCase by checking its type
		// need to avoid trying to instantiate the abstract class EnsTestCase iteslf
		Class newClass;
		Object obj = new Object();
		String baseClassName;

		for (int i = 0; i < classFiles.length; i++) {

			logger.finest(classFiles[i].getName());
			baseClassName = classFiles[i].getName().substring(0, classFiles[i].getName().lastIndexOf("."));

			try {
				newClass = Class.forName(packageName + "." + baseClassName);
				String className = newClass.getName();
				if (!className.equals("org.ensembl.healthcheck.testcase.EnsTestCase")) { // ignore JUnit tests
					obj = newClass.newInstance();
				}
			} catch (InstantiationException ie) {
				// normally it is a BAD THING to just ignore exceptions
				// however InstantiationExceptions may be thrown often in this case,
				// so we deliberately chose to suppress this particular exception
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (obj instanceof org.ensembl.healthcheck.testcase.EnsTestCase && !tests.contains(obj)) {
				((org.ensembl.healthcheck.testcase.EnsTestCase)obj).init(this);
				tests.add(obj); // note we store an INSTANCE of the test, not just its name
				//logger.info("Added test case " + obj.getClass().getName());
			}

		} // for classFiles

		return tests;

	} // findTestsInDirectory

	// -------------------------------------------------------------------------
	/**
	 * Find tests in a jar file.
	 * @param jarFileName The name of the jar file to search.
	 * @param packageName The package name of the tests.
	 * @return The list of tests in the jar file.
	 */
	public List findTestsInJar(String jarFileName, String packageName) {

		ArrayList tests = new ArrayList();

		try {

			JarFile jarFile = new JarFile(jarFileName);

			for (Enumeration enum = jarFile.entries(); enum.hasMoreElements();) {

				JarEntry entry = (JarEntry)enum.nextElement();
				String entryName = entry.getName();

				// if the entry is a class file in the right package, use it
				// this should deal with sub-packages of org.ensembl.healthcheck.testcase too
				String packagePathName = packageName.replaceAll("\\.", File.separator);

				if (entryName != null && entryName.indexOf(".class") > -1 && entryName.indexOf(packagePathName) > -1) {

					String baseClassName = entryName.substring(entryName.lastIndexOf(File.separator) + 1, entryName.lastIndexOf(".class"));

					Object obj = new Object();

					try {
						Class newClass = Class.forName(packageName + "." + baseClassName);
						String className = newClass.getName();
						if (!className.equals("org.ensembl.healthcheck.testcase.EnsTestCase")) { // ignore JUnit tests
							obj = newClass.newInstance();
						}
					} catch (InstantiationException ie) {
						// normally it is a BAD THING to just ignore exceptions
						// however InstantiationExceptions may be thrown often in this case,
						// so we deliberately chose to suppress this particular exception
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (obj instanceof org.ensembl.healthcheck.testcase.EnsTestCase && !tests.contains(obj)) {
						((org.ensembl.healthcheck.testcase.EnsTestCase)obj).init(this);
						tests.add(obj); // note we store an INSTANCE of the test, not just its name
						//logger.info("Added test case " + obj.getClass().getName());
					}
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return tests;

	} // findTestsInJar

	// -------------------------------------------------------------------------
	/**
	 * Add all tests in subList to mainList, <em>unless</em> the test is already a member of mainList.
	 * @param mainList The list to add to.
	 * @param subList The list to be added.
	 */
	public void addUniqueTests(List mainList, List subList) {

		Iterator it = subList.iterator();

		while (it.hasNext()) {

			EnsTestCase test = (EnsTestCase)it.next();
			if (!testInList(test, mainList)) { // can't really use List.contains() as the lists store objects which may be different
				mainList.add(test);
				logger.fine("Added " + test.getShortTestName() + " to the list of tests");
			} else {
				logger.fine("Skipped " + test.getShortTestName() + " as it is already in the list of tests");
			}
		}

	} // addUniqueTests

	// -------------------------------------------------------------------------
	/**
	 * Check if a particular test is in a list of tests. The check is done by test name.
	 * @param test The test case to check.
	 * @param list The list to search.
	 * @return true if test is in list.
	 */
	public boolean testInList(EnsTestCase test, List list) {

		boolean inList = false;

		Iterator it = list.iterator();
		while (it.hasNext()) {
			EnsTestCase thisTest = (EnsTestCase)it.next();
			if (thisTest.getTestName().equals(test.getTestName())) {
				inList = true;
			}
		}

		return inList;

	} // testInList

	// -------------------------------------------------------------------------
	/**
	 * Get the union of all the test groups.
	 * @param tests The tests to check.
	 * @return An array containing the names of all the groups that any member of tests is a member of.
	 */
	public String[] listAllGroups(List tests) {

		ArrayList g = new ArrayList();

		Iterator it = tests.iterator();
		while (it.hasNext()) {
			List thisTestsGroups = ((EnsTestCase)it.next()).getGroups();
			Iterator it2 = thisTestsGroups.iterator();
			while (it2.hasNext()) {
				String group = (String)it2.next();
				if (!g.contains(group)) {
					g.add(group);
				}
			}
		}

		return (String[])g.toArray(new String[g.size()]);

	} // listAllGroups

	// -------------------------------------------------------------------------
	/**
	 * List all the tests in a particular group.
	 * @param tests The tests to check.
	 * @param group The group name to check.
	 * @return An array containing the names whatever tests are a member of group.
	 */
	public String[] listTestsInGroup(List tests, String group) {

		ArrayList g = new ArrayList();

		Iterator it = tests.iterator();
		while (it.hasNext()) {

			EnsTestCase test = (EnsTestCase)it.next();
			if (test.inGroup(group)) {
				g.add(test.getShortTestName());
			}

		}

		return (String[])g.toArray(new String[g.size()]);

	} // listTestsInGroup

	// -------------------------------------------------------------------------
	/**
	 * Print (to stdout) out a list of test reports, keyed by the test type.
	 * @param level The lowest report level (see ReportLine) to print. Reports with a level lower than this are not printed.
	 */
	public void printReportsByTest(int level) {

		System.out.println("\n---- RESULTS BY TEST CASE ----");
		Map map = ReportManager.getAllReportsByTestCase(level);
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			System.out.println("\n" + key);
			List lines = (List)map.get(key);
			Iterator it2 = lines.iterator();
			while (it2.hasNext()) {
				ReportLine reportLine = (ReportLine)it2.next();
				if (reportLine.getLevel() >= level) {
					String dbName = reportLine.getDatabaseName();
					if (dbName.equals("no_database")) {
						dbName = "";
					} else {
						dbName = reportLine.getDatabaseName() + ": ";
					}
					System.out.println("  " + dbName + reportLine.getMessage());
				} // if level
			} // while it2
		} // while it

	} // printReportsByTest

	// -------------------------------------------------------------------------
	/**
	 * Print (to stdout) a list of test results, ordered by database.
	 * @param level The minimum level of report to print - see ReportLine. Reports below this level are not printed.
	 */
	public void printReportsByDatabase(int level) {

		System.out.println("\n---- RESULTS BY DATABASE ----");
		Map map = ReportManager.getAllReportsByDatabase(level);
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			System.out.println("\n" + key);
			List lines = (List)map.get(key);
			Iterator it2 = lines.iterator();
			while (it2.hasNext()) {
				ReportLine reportLine = (ReportLine)it2.next();
				if (reportLine.getLevel() >= level) {
					System.out.println(" " + reportLine.getShortTestCaseName() + ": " + reportLine.getMessage());
				} // if level
			} // while it2
		} // while it

	} // printReportsByDatabase

	// -------------------------------------------------------------------------
	/**
	 * Set the outputLevel variable based on an input string (probably from the command line)
	 * @param str The output level to use.
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
			logger.warning("Output level " + str + " not recognised; using 'all'");
		}

	} // setOutputLevel

	// -------------------------------------------------------------------------
	/**
	 * Set the output level.
	 * @param l The new output level.
	 */
	public void setOutputLevel(int l) {

		outputLevel = l;
		logger.finest("Set outputLevel to " + outputLevel);

	} // setOutputLevel

	// -------------------------------------------------------------------------

	/**
	 * Get the current output level.
	 * @return The current output level. See ReportLine.
	 */
	public int getOutputLevel() {

		return outputLevel;

	} // getOutputLevel

	// -------------------------------------------------------------------------

} // TestRunner

// -------------------------------------------------------------------------
