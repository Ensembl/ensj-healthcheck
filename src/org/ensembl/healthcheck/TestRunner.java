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

import java.util.*;
import java.util.logging.*;
import java.sql.*;
import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.util.*;

/**
 * <p>
 * TestRunner is a base class that provides utilities for running tests - logging, the
 * ability to find and run tests from certain locations, etc.
 * </p>
 */

public class TestRunner {

	/** List that holds an instance of each test. */
	protected List allTests;

	/** The List of group names (as Strings) that will be run. */
	protected List groupsToRun;

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

	// -------------------------------------------------------------------------
	/** Creates a new instance of TestRunner */

	public TestRunner() {
		
		groupsToRun = new ArrayList();
	
	} // TestRunner

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the schema names .
	 * 
	 * @return An array of the schema names.
	 */
	public String[] getAllSchemaNames() {
		
		Connection conn;
		String[] schemaNames = null;

		// open connection
		try {
			conn = DBUtils.openConnection(System.getProperty("driver"), System.getProperty("databaseURL"), System.getProperty("user"), System.getProperty("password"));
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
	 * 
	 * @param regexp The regular expression to match.
	 * @return An array of the matching database names (may be empty if none matched).
	 */
	public String[] getListOfDatabaseNames(String regexp) {
		
		Connection conn;
		String[] databaseNames = null;

		// open connection
		try {
			conn = DBUtils.openConnection(System.getProperty("driver"), System.getProperty("databaseURL"), System.getProperty("user"), System.getProperty("password"));
			logger.fine("Opened connection to " + System.getProperty("databaseURL") + " as " + System.getProperty("user"));
			databaseNames = DBUtils.listDatabases(conn, regexp);
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
	 * Prints, to stdout, a list of the database names that match a given regular
	 * expression.
	 * 
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
	 * Run appropriate tests against databases. Also run show/repair methods if the test implements the
	 * Repair interface and the appropriate flags are set.
	 * 
	 * @param databaseRegistry The DatabaseRegistry to use.
	 * @param testRegistry The TestRegistry to use.
	 */
	protected void runAllTests(DatabaseRegistry databaseRegistry, TestRegistry testRegistry) {

		int numberOfTestsRun = 0;
		
		// --------------------------------	
		// Single-database tests

		DatabaseRegistryEntry[] databases = databaseRegistry.getAll();

		// run the appropriate tests on each of them
		for (int i = 0; i < databases.length; i++) {

			DatabaseRegistryEntry database = databases[i];

			List allSingleDatabaseTests = testRegistry.getAllSingle(groupsToRun, database.getType());
			
			for (Iterator it = allSingleDatabaseTests.iterator(); it.hasNext();) {

				SingleDatabaseTestCase testCase = (SingleDatabaseTestCase)it.next();

				ReportManager.startTestCase(testCase);

				boolean result = testCase.run(database);

				ReportManager.finishTestCase(testCase, result);
				logger.info(testCase.getName() + " " + (result ? "PASSED" : "FAILED"));

				numberOfTestsRun++;

				checkRepair(testCase, database);

			} // foreach test

		} // foreach DB

		// --------------------------------
		// Multi-database tests

		// TODO implement

		// --------------------------------

		if (numberOfTestsRun == 0) {
			logger.warning("Warning: no tests were run.");
		}

	} // runAllTests

	//---------------------------------------------------------------------
	/**
	 * Check if the given testcase can repair errors on the given database.
	 */
	private void checkRepair(EnsTestCase testCase, DatabaseRegistryEntry database) {
	
		// check for show/do repair
		if (testCase.canRepair()) {
			if (showRepair) {
				((Repair)testCase).show(database);
			}
			if (doRepair) {
				((Repair)testCase).repair(database);
			}
		}
		
	} // checkRepair
	
	// -------------------------------------------------------------------------
	/**
	 * Get a connection to a particular database.
	 * 
	 * @return A connection to database.
	 */
	public Connection getDatabaseConnection() {
		
		return DBUtils.openConnection(System.getProperty("driver"), System.getProperty("databaseURL"), System.getProperty("user"), System.getProperty("password"));
	
	} // getDatabaseConnection

	// -------------------------------------------------------------------------
	/**
	 * Get an iterator that will iterate over database connections whose names match a
	 * particular regular expression.
	 * 
	 * @param databaseRegexp The regular expression to match.
	 * @return A DatabaseConnectionIterator object that will iterate over database
	 *         Connections for databases whose names match the regular expression.
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
	 * Get the union of all the test groups.
	 * 
	 * @param tests The tests to check.
	 * @return An array containing the names of all the groups that any member of tests is
	 *         a member of.
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
	 * 
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
	 * 
	 * @param level The lowest report level (see ReportLine) to print. Reports with a level
	 *          lower than this are not printed.
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
	 * 
	 * @param level The minimum level of report to print - see ReportLine. Reports below
	 *          this level are not printed.
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
	 * Set the outputLevel variable based on an input string (probably from the command
	 * line)
	 * 
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
	 * 
	 * @param l The new output level.
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
