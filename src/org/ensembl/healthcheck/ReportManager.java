/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * ReportManager is the main class for reporting in the Ensj Healthcheck system. It provides methods for storing reports - single
 * items of information - and retrieving them in various formats.
 */
public class ReportManager {

	/**
	 * <p>
	 * Resets attributes of the ReportManager. This way the ReportManager can be used for running more than one session. (Not at the
	 * same time) Before starting a new session, calling ReportManager.initialise() will put the ReportManager back into a state in
	 * which it can be used again.
	 * </p>
	 * 
	 */
	public static void initialise() {

		reportsByTest = new HashMap();
		reportsByDatabase = new HashMap();

		outputDatabaseConnection = null;
		sessionID = -1;
	}

	/** A hash of lists keyed on the test name. */
	protected static Map reportsByTest = new HashMap();

	/** A hash of lists keyed on the database name */
	protected static Map reportsByDatabase = new HashMap();

	/** The logger to use for this class */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	/**
	 * The maximum number of lines to store to prevent very verbose test cases causing memory problems
	 */
	protected static final int MAX_BUFFER_SIZE = 2000;

	private static boolean bufferSizeWarningPrinted = false;

	private static Reporter reporter;

	private static boolean usingDatabase = false;

	private static Connection outputDatabaseConnection;

	private static long sessionID = -1;

	// hide constructor to stop instantiation
	private ReportManager() {

	}

	/**
	 * Set the reporter for this ReportManager.
	 * 
	 * @param rep
	 *          The Reporter to set.
	 */
	public static void setReporter(Reporter rep) {

		reporter = rep;
	}

	/**
	 * Should be called before a test case is run.
	 * 
	 * @param testCase
	 *          The testcase to be run.
	 * @param dbre
	 *          The database that testCase will run on.
	 */
	public static void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

		if (reporter != null) {
			reporter.startTestCase(testCase, dbre);
		}
	}

	/**
	 * Should be called immediately after a test case has run.
	 * 
	 * @param testCase
	 *          The testcase that was run.
	 * @param result
	 *          The result of the test case.
	 * @param dbre
	 *          The database which the test case was run on.
	 */
	public static void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

		if (reporter != null) {
			reporter.finishTestCase(testCase, result, dbre);
		}
	}

	// -------------------------------------------------------------------------
	/**
	 * Add a test case report.
	 * 
	 * @param report
	 *          The ReportLine to add.
	 */
	public static void add(ReportLine report) {

		if (usingDatabase) {

			checkAndAddToDatabase(report);
			return;

		}

		String testCaseName = report.getTestCaseName();
		String databaseName = report.getDatabaseName();

		ArrayList lines;

		// add to each hash
		if (testCaseName != null && testCaseName.length() > 0) {
			// create the lists if they're not there already
			if (reportsByTest.get(testCaseName) == null) {
				lines = new ArrayList();
				lines.add(report);
				reportsByTest.put(testCaseName, lines);
			} else {
				// get the relevant list, update it, and re-add it
				lines = (ArrayList) reportsByTest.get(testCaseName);

				// prevent the buffer getting too big
				if (lines.size() > MAX_BUFFER_SIZE) {
					if (!bufferSizeWarningPrinted) {
						System.err.println("\n\nReportManager has reached its maximum buffer size (" + MAX_BUFFER_SIZE + " lines) - no more output will be stored\n");
						bufferSizeWarningPrinted = true;
					}
				} else {
					// buffer small enough, add it
					lines.add(report);
					reportsByTest.put(testCaseName, lines);
				}

			}

		} else {
			logger.warning("Cannot add report with test case name not set");
		}

		if (databaseName != null && databaseName.length() > 0) {
			// create the lists if they're not there already
			if (reportsByDatabase.get(databaseName) == null) {
				lines = new ArrayList();
				lines.add(report);
				reportsByDatabase.put(databaseName, lines);
			} else {
				// get the relevant list, update it, and re-add it
				lines = (ArrayList) reportsByDatabase.get(databaseName);
				lines.add(report);
				reportsByDatabase.put(databaseName, lines);
			}

		}

		if (reporter != null) {
			reporter.message(report);
		}
	} // add

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for storing reports, intended to be easy to call from an EnsTestCase.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param con
	 *          The database connection involved.
	 * @param level
	 *          The level of this report.
	 * @param message
	 *          The message to be reported.
	 */
	public static void report(EnsTestCase testCase, Connection con, int level, String message) {

		// this may be called when there is no DB connection
		String dbName = (con == null) ? "no_database" : DBUtils.getShortDatabaseName(con);

		add(new ReportLine(testCase, dbName, level, message, testCase.getTeamResponsible(), testCase.getSecondTeamResponsible()));

	} // report

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for storing reports, intended to be easy to call from an EnsTestCase.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param dbName
	 *          The name of the database involved.
	 * @param level
	 *          The level of this report.
	 * @param message
	 *          The message to be reported.
	 */
	public static void report(EnsTestCase testCase, String dbName, int level, String message) {

		add(new ReportLine(testCase, dbName, level, message, testCase.getTeamResponsible(), testCase.getSecondTeamResponsible()));

	} // report

	// -------------------------------------------------------------------------
	/**
	 * Store a ReportLine with a level of ReportLine.INFO.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param con
	 *          The database connection involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void problem(EnsTestCase testCase, Connection con, String message) {

		report(testCase, con, ReportLine.PROBLEM, message);

	} // problem

	/**
	 * Store a ReportLine with a level of ReportLine.PROBLEM.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param dbName
	 *          The name of the database involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void problem(EnsTestCase testCase, String dbName, String message) {

		report(testCase, dbName, ReportLine.PROBLEM, message);

	} // problem

	/**
	 * Store a ReportLine with a level of ReportLine.INFO.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param con
	 *          The database connection involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void info(EnsTestCase testCase, Connection con, String message) {

		report(testCase, con, ReportLine.INFO, message);

	} // info

	/**
	 * Store a ReportLine with a level of ReportLine.INFO.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param dbName
	 *          The name of the database involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void info(EnsTestCase testCase, String dbName, String message) {

		report(testCase, dbName, ReportLine.INFO, message);

	} // info

	/**
	 * Store a ReportLine with a level of ReportLine.SUMMARY.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param con
	 *          The database connection involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void warning(EnsTestCase testCase, Connection con, String message) {

		report(testCase, con, ReportLine.WARNING, message);

	} // summary

	/**
	 * Store a ReportLine with a level of ReportLine.SUMMARY.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param dbName
	 *          The name of the database involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void warning(EnsTestCase testCase, String dbName, String message) {

		report(testCase, dbName, ReportLine.WARNING, message);

	} // summary

	/**
	 * Store a ReportLine with a level of ReportLine.CORRECT.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param con
	 *          The database connection involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void correct(EnsTestCase testCase, Connection con, String message) {

		report(testCase, con, ReportLine.CORRECT, message);

	} // summary

	/**
	 * Store a ReportLine with a level of ReportLine.CORRECT.
	 * 
	 * @param testCase
	 *          The test case filing the report.
	 * @param dbName
	 *          The name of the database involved.
	 * @param message
	 *          The message to be reported.
	 */
	public static void correct(EnsTestCase testCase, String dbName, String message) {

		report(testCase, dbName, ReportLine.CORRECT, message);

	} // summary

	// -------------------------------------------------------------------------
	/**
	 * Get a HashMap of all the reports, keyed on test case name.
	 * 
	 * @return The HashMap of all the reports, keyed on test case name.
	 */
	public static Map getAllReportsByTestCase() {

		return reportsByTest;

	} // getAllReportsByTestCase

	// -------------------------------------------------------------------------
	/**
	 * Get a HashMap of all the reports, keyed on test case name.
	 * 
	 * @param level
	 *          The ReportLine level (e.g. PROBLEM) to filter on.
	 * @return The HashMap of all the reports, keyed on test case name.
	 */
	public static Map getAllReportsByTestCase(int level) {

		return filterMap(reportsByTest, level);

	} // getAllReportsByTestCase

	// -------------------------------------------------------------------------
	/**
	 * Get a HashMap of all the reports, keyed on database name.
	 * 
	 * @return The HashMap of all the reports, keyed on database name.
	 */
	public static Map getAllReportsByDatabase() {

		return reportsByDatabase;

	} // getReportsByDatabase

	// -------------------------------------------------------------------------
	/**
	 * Get a HashMap of all the reports, keyed on test case name.
	 * 
	 * @param level
	 *          The ReportLine level (e.g. PROBLEM) to filter on.
	 * @return The HashMap of all the reports, keyed on test case name.
	 */
	public static Map getAllReportsByDatabase(int level) {

		return filterMap(reportsByDatabase, level);

	} // getAllReportsByTestCase

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the reports corresponding to a particular test case.
	 * 
	 * @return A List of the results (as a list) corresponding to test.
	 * @param testCaseName
	 *          The test case to filter by.
	 * @param level
	 *          The minimum level of report to include, e.g. ReportLine.INFO
	 */
	public static List getReportsByTestCase(String testCaseName, int level) {

		List allReports = (List) reportsByTest.get(testCaseName);

		return filterList(allReports, level);

	} // getReportsByTestCase

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the reports corresponding to a particular database.
	 * 
	 * @param databaseName
	 *          The database to report on.
	 * @param level
	 *          The minimum level of report to include, e.g. ReportLine.INFO
	 * @return A List of the ReportLines corresponding to database.
	 */
	public static List getReportsByDatabase(String databaseName, int level) {

		return filterList((List) reportsByDatabase.get(databaseName), level);

	} // getReportsByDatabase

	// -------------------------------------------------------------------------
	/**
	 * Filter a list of ReportLines so that only certain entries are returned.
	 * 
	 * @param list
	 *          The list to filter.
	 * @param level
	 *          All reports with a priority above this level will be returned.
	 * @return A list of the ReportLines that have a level >= that specified.
	 */
	public static List filterList(List list, int level) {

		ArrayList result = new ArrayList();

		if (list != null) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				ReportLine line = (ReportLine) it.next();
				if (line.getLevel() >= level) {
					result.add(line);
				}
			}
		}

		return result;

	} // filterList

	// -------------------------------------------------------------------------
	/**
	 * Filter a HashMap of lists of ReportLines so that only certain entries are returned.
	 * 
	 * @param map
	 *          The list to filter.
	 * @param level
	 *          All reports with a priority above this level will be returned.
	 * @return A HashMap with the same keys as map, but with the lists filtered by level.
	 */
	public static Map filterMap(Map map, int level) {

		HashMap result = new HashMap();

		Set keySet = map.keySet();
		Iterator it = keySet.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			List list = (List) map.get(key);
			result.put(key, filterList(list, level));
		}

		return result;

	} // filterList

	// ---------------------------------------------------------------------
	/**
	 * Count how many tests passed and failed for a particular database. A test is considered to have passed if there are no reports
	 * of level ReportLine.PROBLEM.
	 * 
	 * @param database
	 *          The database to check.
	 * @return An array giving the number of passes and then fails for this database.
	 */
	public static int[] countPassesAndFailsDatabase(String database) {

		int[] result = new int[2];

		List testsRun = new ArrayList();

		// get all of them to build a list of the tests that were run
		List reports = getReportsByDatabase(database, ReportLine.ALL);
		Iterator it = reports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			String test = line.getTestCaseName();
			if (!testsRun.contains(test)) {
				testsRun.add(test);
			}
		}

		// count those that failed
		List testsFailed = new ArrayList();
		reports = getReportsByDatabase(database, ReportLine.PROBLEM);
		it = reports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			String test = line.getTestCaseName();
			if (!testsFailed.contains(test)) {
				testsFailed.add(test);
			}
		}

		result[1] = testsFailed.size();
		result[0] = testsRun.size() - testsFailed.size(); // if it didn't
		// fail, it passed

		return result;

	}

	// ---------------------------------------------------------------------
	/**
	 * Count how many databases passed a particular test. A test is considered to have passed if there are no reports of level
	 * ReportLine.PROBLEM.
	 * 
	 * @param test
	 *          The test to check.
	 * @return An array giving the number of databases that passed [0] and failed [1] this test.
	 */
	public static int[] countPassesAndFailsTest(String test) {

		int[] result = new int[2];

		List allDBs = new ArrayList();

		// get all of them to build a list of the tests that were run
		List reports = getReportsByTestCase(test, ReportLine.ALL);
		Iterator it = reports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			String database = line.getDatabaseName();
			if (!allDBs.contains(database)) {
				allDBs.add(database);
			}
		}

		// count those that failed
		List dbsFailed = new ArrayList();
		reports = getReportsByTestCase(test, ReportLine.PROBLEM);
		it = reports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			String database = line.getDatabaseName();
			if (!dbsFailed.contains(database)) {
				dbsFailed.add(database);
			}
		}

		result[1] = dbsFailed.size();
		result[0] = allDBs.size() - dbsFailed.size(); // if it didn't fail, it
		// passed

		return result;

	}

	// -------------------------------------------------------------------------

	/**
	 * Count how many tests passed and failed. A test is considered to have passed if there are no reports of level
	 * ReportLine.PROBLEM.
	 * 
	 * @return An array giving the number of passes and then fails.
	 */
	public static int[] countPassesAndFailsAll() {

		int[] result = new int[2];

		Map allByDB = getAllReportsByDatabase();
		Set dbs = allByDB.keySet();
		Iterator it = dbs.iterator();
		while (it.hasNext()) {
			String database = (String) it.next();
			int[] dbResult = countPassesAndFailsDatabase(database);
			result[0] += dbResult[0];
			result[1] += dbResult[1];
		}

		return result;

	}

	// -------------------------------------------------------------------------

	/**
	 * Check if all the a particular database passed a particular test.
	 * 
	 * @param test
	 *          The test to check.
	 * @param database
	 *          The database to check.
	 * @return true if database passed test (i.e. had no problems).
	 */
	public static boolean databasePassed(String test, String database) {

		List reports = getReportsByTestCase(test, ReportLine.PROBLEM);
		Iterator it = reports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			if (database.equals(line.getDatabaseName())) {
				return false;
			}
		}

		return true;

	}

	// -------------------------------------------------------------------------
	/**
	 * Check if all the databases passed a particular test.
	 * 
	 * @param test
	 *          The test to check.
	 * @return true if none of the databases failed this test (i.e. had no problems)
	 */
	public static boolean allDatabasesPassed(String test) {

		List reports = getReportsByTestCase(test, ReportLine.PROBLEM);
		if (reports.size() > 0) {
			return false;
		}

		return true;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the reports corresponding to a particular database and test case.
	 * 
	 * @return A List of the results (as a list) corresponding to test and database.
	 * @param test
	 *          The test case to filter by.
	 * @param database
	 *          The database.
	 */
	public static List getReports(String test, String database) {

		List result = new ArrayList();

		List allReports = (List) reportsByTest.get(test);

		Iterator it = allReports.iterator();
		while (it.hasNext()) {
			ReportLine line = (ReportLine) it.next();
			if (database.equals(line.getDatabaseName())) {
				result.add(line);
			}
		}

		return result;

	} // getReports


        // -------------------------------------------------------------------------
        /**
         * Check if reports for a given database have been propagated for the release
         * 
         * @return boolean true if propagation has been run, false else
         * 
         * @param database
         *          The database.
         */
        public static boolean hasPropagated(DatabaseRegistryEntry database) {

                boolean result = true;
                String sql = "SELECT count(*) FROM propagated WHERE database_name = '" + database.getName() + "'";
                int rows = DBUtils.getRowCount(outputDatabaseConnection, sql);
                if (rows == 0) {
                        result = false;
                }

                return result;

        } // getReports


	// -------------------------------------------------------------------------
	/**
	 * Flag to state whether a database is being used as the output destination.
	 */
	public static boolean usingDatabase() {

		return usingDatabase;

	}

	// -------------------------------------------------------------------------
	/**
	 * Set up connection to a database for output. Sets usingDatabase to true.
	 */
	public static void connectToOutputDatabase() {

		logger.info("Connecting to " + System.getProperty("output.databaseURL") + System.getProperty("output.database") + " as " + System.getProperty("output.user"));

		try {
			outputDatabaseConnection = DBUtils.openConnection(
				System.getProperty("output.driver"), 
				System.getProperty("output.databaseURL") + 
				System.getProperty("output.database"),
				System.getProperty("output.user"), 
				System.getProperty("output.password")
			);
		} catch(SQLException e) {			
			throw new RuntimeException(e);			
		}

		usingDatabase = true;

	}

	// -------------------------------------------------------------------------
	/**
	 * Create a new entry in the session table. Store the ID of the created session in sessionID.
	 */
	public static void createDatabaseSession() {

		// build comma-separated list of hosts
		StringBuffer buf = new StringBuffer();
		Iterator<DatabaseServer> it = DBUtils.getMainDatabaseServers().iterator();

		while (it.hasNext()) {

			DatabaseServer server = (DatabaseServer) it.next();
			buf.append(String.format("%s:%s", server.getHost(), server.getPort()));
			if (it.hasNext()) {
				buf.append(",");
			}

		}

		String hosts = buf.toString();

		String outputDatabases = Utils.listToString(Utils.getDatabasesAndGroups(), ",");

		String outputRelease = System.getProperty("output.release");

		String sql = String.format("INSERT INTO session (host, config, db_release, start_time) VALUES (" + "\"" + hosts.toString() + "\", " + "\"" + outputDatabases + "\", " + "\"" + outputRelease
				+ "\", " + "NOW())");

		try {
			Statement stmt = outputDatabaseConnection.createStatement();
			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				sessionID = rs.getLong(1);
				logger.fine("Created new session with ID " + sessionID);
			}
			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		if (sessionID == -1) {
			logger.severe("Could not get new session ID");
			logger.severe(sql);
		}

	}

        // -------------------------------------------------------------------------
        /**
         * Reuse an existing entry in the session table.
         */
        public static void reuseDatabaseSession(long sessionID) {

                // build comma-separated list of hosts
                StringBuffer buf = new StringBuffer();
                Iterator<DatabaseServer> it = DBUtils.getMainDatabaseServers().iterator();

                while (it.hasNext()) {

                        DatabaseServer server = (DatabaseServer) it.next();
                        buf.append(String.format("%s:%s", server.getHost(), server.getPort()));
                        if (it.hasNext()) {
                                buf.append(",");
                        }

                }

                String hosts = buf.toString();

                String outputDatabases = Utils.listToString(Utils.getDatabasesAndGroups(), ",");

                String outputRelease = System.getProperty("output.release");

                String sql = "SELECT session_id FROM session WHERE config=? AND db_release=? AND session_id=?";

                long newSessionID = -1;

                try {

                        PreparedStatement stmt = outputDatabaseConnection.prepareStatement(sql);
                        stmt.setString(1, outputDatabases);
                        stmt.setString(2, outputRelease);
                        stmt.setLong(3, sessionID);
                        ResultSet rs = stmt.executeQuery();
                        if (rs != null) {
                                if (rs.first()) {
                                        newSessionID = rs.getLong(1);
                                } else {
                                        newSessionID = -1; // probably signifies an empty ResultSet
                                }
                        }
                        rs.close();
                        stmt.close();

                } catch (SQLException e) {

                        System.err.println("Error executing:\n" + sql);
                        e.printStackTrace();

                }

                if (newSessionID == -1) {
                        logger.severe("Could not reuse " + newSessionID);
                        logger.severe(sql);
                }

                setSessionID(sessionID);
        }


	// -------------------------------------------------------------------------
	/**
	 * End a database session. Write the end time into the database.
	 */
	public static void endDatabaseSession() {

		String sql = "UPDATE session SET end_time=NOW() WHERE session_id=" + sessionID;

		try {

			Statement stmt = outputDatabaseConnection.createStatement();
			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Delete all previous data.
	 */
	public static void deletePrevious() {

		String[] tables = { "session", "report", "annotation" };

		for (int i = 0; i < tables.length; i++) {

			String sql = "DELETE FROM " + tables[i];

			try {

				Statement stmt = outputDatabaseConnection.createStatement();
				stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

				stmt.close();

			} catch (SQLException e) {

				System.err.println("Error executing:\n" + sql);
				e.printStackTrace();

			}

		}
	}

	// -------------------------------------------------------------------------
	/**
	 * Update a report in the database. Two possible actions: 1. If the report already exists and hasn't changed, just update it. 2.
	 * If the report is new, add a new record.
	 */
	public static void checkAndAddToDatabase(ReportLine report) {

		long reportID = reportExistsInDatabase(report);

		if (reportID > -1) {

			updateReportInDatabase(report, reportID);

		} else {

			addReportToDatabase(report);

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Check if a report exists (i.e. same database, testcase, result and text).
	 * 
	 * @return -1 if the report does not exist, report_id if it does.
	 */
	public static long reportExistsInDatabase(ReportLine report) {

		String sql = "SELECT report_id FROM report WHERE database_name=? AND testcase=? AND result=? AND BINARY(text)=BINARY(?)";

		long reportID = -1;

		try {

			PreparedStatement stmt = outputDatabaseConnection.prepareStatement(sql);
			stmt.setString(1, report.getDatabaseName());
			stmt.setString(2, report.getShortTestCaseName());
			stmt.setString(3, report.getLevelAsString());
			stmt.setString(4, report.getMessage());
			ResultSet rs = stmt.executeQuery();
			if (rs != null) {
				if (rs.first()) {
					reportID = rs.getLong(1);
				} else {
					reportID = -1; // probably signifies an empty ResultSet
				}
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		if (reportID > -1) {
			logger.finest("Report already exists (ID " + reportID + "): " + report.getDatabaseName() + " " + report.getTestCaseName() + " " + report.getLevelAsString() + " " + report.getMessage());
		} else {
			logger.finest("Report does not already exist: " + report.getDatabaseName() + " " + report.getTestCaseName() + " " + report.getLevelAsString() + " " + report.getMessage());
		}

		return reportID;

	}

	// -------------------------------------------------------------------------
	/**
	 * Store a report in the database.
	 */
	public static void addReportToDatabase(ReportLine report) {

		if (outputDatabaseConnection == null) {
			logger.severe("No connection to output database!");
			return;
		}

		logger.fine("Adding report for: " + report.getDatabaseName() + " " + report.getTestCaseName() + " " + report.getLevelAsString() + " " + report.getMessage());

		String sql = "INSERT INTO report (first_session_id, last_session_id, database_name, species, database_type, testcase, result, text, timestamp, team_responsible, created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, NOW())";

		try {

			PreparedStatement stmt = outputDatabaseConnection.prepareStatement(sql);
			stmt.setLong(1, sessionID);
			stmt.setLong(2, sessionID);
			stmt.setString(3, report.getDatabaseName());
			// EG Store species name and db type from explicit report line, not from database
			stmt.setString(4, report.getSpeciesName());
			stmt.setString(5, report.getType().toString());
			stmt.setString(6, report.getShortTestCaseName());
			stmt.setString(7, report.getLevelAsString());
			stmt.setString(8, report.getMessage());
			stmt.setString(9, report.getPrintableTeamResponsibleString());
			stmt.executeUpdate();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Update the last_session_id of a report in the database.
	 */
	public static void updateReportInDatabase(ReportLine report, long reportID) {

		if (outputDatabaseConnection == null) {
			logger.severe("No connection to output database!");
			return;
		}

		logger.fine("Updating report for: " + report.getDatabaseName() + " " + report.getTestCaseName() + " " + report.getLevelAsString() + " " + report.getMessage() + ", new last_session_id="
				+ sessionID);

		String sql = "UPDATE report SET last_session_id=?, timestamp=NOW() WHERE report_id=?";

		try {

			PreparedStatement stmt = outputDatabaseConnection.prepareStatement(sql);
			stmt.setLong(1, sessionID);
			stmt.setLong(2, reportID);
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

	}

	// -------------------------------------------------------------------------

	public static long getSessionID() {
		return sessionID;
	}

	public static void setSessionID(long sessionID) {
		ReportManager.sessionID = sessionID;
	}

} // ReportManager
