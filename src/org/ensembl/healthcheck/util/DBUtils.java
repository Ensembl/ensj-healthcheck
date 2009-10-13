/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseServer;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.EnsTestCase;

/**
 * Various database utilities.
 */

public final class DBUtils {

	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	private static List<DatabaseServer> mainDatabaseServers;

	private static List<DatabaseServer> secondaryDatabaseServers;

	private static DatabaseRegistry mainDatabaseRegistry;

	private static DatabaseRegistry secondaryDatabaseRegistry;

	// hide constructor to stop instantiation
	private DBUtils() {

	}

	// -------------------------------------------------------------------------
	/**
	 * Open a connection to the database.
	 * 
	 * @param driverClassName
	 *          The class name of the driver to load.
	 * @param databaseURL
	 *          The URL of the database to connect to.
	 * @param user
	 *          The username to connect with.
	 * @param password
	 *          Password for user.
	 * @return A connection to the database, or null.
	 */
	public static Connection openConnection(String driverClassName, String databaseURL, String user, String password) {

		return ConnectionPool.getConnection(driverClassName, databaseURL, user, password);

	} // openConnection

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the database names for a particular connection.
	 * 
	 * @param con
	 *          The connection to query.
	 * @return An array of Strings containing the database names.
	 */

	public static String[] listDatabases(Connection con) {

		if (con == null) {
			logger.severe("Database connection is null");
		}

		ArrayList<String> dbNames = new ArrayList<String>();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW DATABASES");

			while (rs.next()) {
				dbNames.add(rs.getString(1));
			}

			rs.close();

			stmt.close();

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

		String[] ret = new String[dbNames.size()];

		return (String[]) dbNames.toArray(ret);

	} // listDatabases

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the database names that match a certain pattern for a particular connection.
	 * 
	 * @param conn
	 *          The connection to query.
	 * @param regex
	 *          A regular expression to match. If null, match all.
	 * @return An array of Strings containing the database names.
	 */
	public static String[] listDatabases(Connection con, String regex) {

		ArrayList<String> dbMatches = new ArrayList<String>();
		
		String[] allDBNames = listDatabases(con);

		for (String name : allDBNames) {

			if (regex == null) {
				
				dbMatches.add(name);
				
			} else if (name.matches(regex)) {
				
				dbMatches.add(name);

			}

		} 

		String[] ret = new String[dbMatches.size()];

		return (String[]) dbMatches.toArray(ret);

	} // listDatabases

	// -------------------------------------------------------------------------
	/**
	 * Compare a list of ResultSets to see if there are any differences. Note that if the ResultSets are large and/or there are many
	 * of them, this may take a long time!
	 * 
	 * @return The number of differences.
	 * @param testCase
	 *          The test case that is calling the comparison. Used for ReportManager.
	 * @param resultSetGroup
	 *          The list of ResultSets to compare
	 */
	public static boolean compareResultSetGroup(List<ResultSet> resultSetGroup, EnsTestCase testCase, boolean comparingSchema) {

		boolean same = true;

		// avoid comparing the same two ResultSets more than once
		// i.e. only need the upper-right triangle of the comparison matrix
		int size = resultSetGroup.size();
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				ResultSet rsi = resultSetGroup.get(i);
				ResultSet rsj = resultSetGroup.get(j);
				same &= compareResultSets(rsi, rsj, testCase, "", true, true, "", comparingSchema);
			}
		}

		return same;

	} // compareResultSetGroup

	// -------------------------------------------------------------------------
	/**
	 * Compare two ResultSets.
	 * 
	 * @return True if all the following are true:
	 *         <ol>
	 *         <li>rs1 and rs2 have the same number of columns</li>
	 *         <li>The name and type of each column in rs1 is equivalent to the corresponding column in rs2.</li>
	 *         <li>All the rows in rs1 have the same type and value as the corresponding rows in rs2.</li>
	 *         </ol>
	 * @param testCase
	 *          The test case calling the comparison; used in ReportManager.
	 * @param text
	 *          Additional text to put in any error reports.
	 * @param rs1
	 *          The first ResultSet to compare.
	 * @param rs2
	 *          The second ResultSet to compare.
	 * @param reportErrors
	 *          If true, error details are stored in ReportManager as they are found.
	 * @param singleTableName
	 *          If comparing 2 result sets from a single table (or from a DESCRIBE table) this should be the name of the table, to be
	 *          output in any error text. Otherwise "".
	 */
	public static boolean compareResultSets(ResultSet rs1, ResultSet rs2, EnsTestCase testCase, String text, boolean reportErrors, boolean warnNull, String singleTableName, boolean comparingSchema) {
		return compareResultSets(rs1, rs2, testCase, text, reportErrors, warnNull, singleTableName, null, comparingSchema);
	}

	public static boolean compareResultSets(ResultSet rs1, ResultSet rs2, EnsTestCase testCase, String text, boolean reportErrors, boolean warnNull, String singleTableName, int[] columns,
			boolean comparingSchema) {

		// quick tests first
		// Check for object equality
		if (rs1.equals(rs2)) {
			return true;
		}

		try {

			// get some information about the ResultSets
			String name1 = getShortDatabaseName(rs1.getStatement().getConnection());
			String name2 = getShortDatabaseName(rs2.getStatement().getConnection());

			// Check for same column count, names and types
			ResultSetMetaData rsmd1 = rs1.getMetaData();
			ResultSetMetaData rsmd2 = rs2.getMetaData();
			if (rsmd1.getColumnCount() != rsmd2.getColumnCount() && columns == null) {

				ReportManager.problem(testCase, name1, "Column counts differ " + singleTableName + " " + name1 + ": " + rsmd1.getColumnCount() + " " + name2 + ": " + rsmd2.getColumnCount());

				return false; // Deliberate early return for performance
				// reasons
			}

			if (columns == null) {
				columns = new int[rsmd1.getColumnCount()];
				for (int i = 0; i < columns.length; i++) {
					columns[i] = i + 1;
				}
			}

			for (int j = 0; j < columns.length; j++) {
				int i = columns[j];

				// note columns indexed from l
				if (!((rsmd1.getColumnName(i)).equals(rsmd2.getColumnName(i)))) {

					ReportManager.problem(testCase, name1, "Column names differ for " + singleTableName + " column " + i + " - " + name1 + ": " + rsmd1.getColumnName(i) + " " + name2 + ": "
							+ rsmd2.getColumnName(i));

					// Deliberate early return for performance reasons
					return false;

				}
				if (rsmd1.getColumnType(i) != rsmd2.getColumnType(i)) {

					ReportManager.problem(testCase, name1, "Column types differ for " + singleTableName + " column " + i + " - " + name1 + ": " + rsmd1.getColumnType(i) + " " + name2 + ": "
							+ rsmd2.getColumnType(i));

					return false; // Deliberate early return for performance
					// reasons
				}
			} // for column

			// make sure both cursors are at the start of the ResultSet
			// (default is before the start)
			rs1.beforeFirst();
			rs2.beforeFirst();
			// if quick checks didn't cause return, try comparing row-wise

			int row = 1;
			while (rs1.next()) {

				if (rs2.next()) {
					for (int j = 0; j < columns.length; j++) {
						int i = columns[j];
						// note columns indexed from 1
						if (!compareColumns(rs1, rs2, i, warnNull)) {
							String str = name1 + " and " + name2 + text + " " + singleTableName + " differ at row " + row + " column " + i + " (" + rsmd1.getColumnName(i) + ")" + " Values: "
									+ Utils.truncate(rs1.getString(i), 250, true) + ", " + Utils.truncate(rs2.getString(i), 250, true);
							if (reportErrors) {
								ReportManager.problem(testCase, name1, str);
							}
							return false;
						}
					}
					row++;

				} else {
					// rs1 has more rows than rs2
					ReportManager.problem(testCase, name1, singleTableName + " (or definition) has more rows in " + name1 + " than in " + name2);
					return false;
				}

			} // while rs1

			// if both ResultSets are the same, then we should be at the end of
			// both, i.e. .next() should return false
			String extra = comparingSchema ? ". This means that there are missing columns in the table, rectify!" : "";
			if (rs1.next()) {

				if (reportErrors) {
					ReportManager.problem(testCase, name1, name1 + " " + singleTableName + " has additional rows that are not in " + name2 + extra);
				}
				return false;
			} else if (rs2.next()) {

				if (reportErrors) {
					ReportManager.problem(testCase, name2, name2 + " " + singleTableName + " has additional rows that are not in " + name1 + extra);

				}
				return false;
			}

		} catch (SQLException se) {
			se.printStackTrace();
		}

		return true;

	} // compareResultSets

	// -------------------------------------------------------------------------
	/**
	 * Compare a particular column in two ResultSets.
	 * 
	 * @param rs1
	 *          The first ResultSet to compare.
	 * @param rs2
	 *          The second ResultSet to compare.
	 * @param i
	 *          The index of the column to compare.
	 * @return True if the type and value of the columns match.
	 */
	public static boolean compareColumns(ResultSet rs1, ResultSet rs2, int i, boolean warnNull) {

		try {

			ResultSetMetaData rsmd = rs1.getMetaData();

			Connection con1 = rs1.getStatement().getConnection();
			Connection con2 = rs2.getStatement().getConnection();

			if (rs1.getObject(i) == null) {
				if (warnNull) {
					System.out.println("Column " + rsmd.getColumnName(i) + " is null in table " + rsmd.getTableName(i) + " in " + DBUtils.getShortDatabaseName(con1));
				}
				return (rs2.getObject(i) == null); // true if both are null
			}
			if (rs2.getObject(i) == null) {
				if (warnNull) {
					System.out.println("Column " + rsmd.getColumnName(i) + " is null in table " + rsmd.getTableName(i) + " in " + DBUtils.getShortDatabaseName(con2));
				}
				return (rs1.getObject(i) == null); // true if both are null
			}

			// Note deliberate early returns for performance reasons
			switch (rsmd.getColumnType(i)) {

			case Types.INTEGER:
				return rs1.getInt(i) == rs2.getInt(i);

			case Types.SMALLINT:
				return rs1.getInt(i) == rs2.getInt(i);

			case Types.TINYINT:
				return rs1.getInt(i) == rs2.getInt(i);

			case Types.VARCHAR:
				return rs1.getString(i).equals(rs2.getString(i));

			case Types.FLOAT:
				return rs1.getFloat(i) == rs2.getFloat(i);

			case Types.DOUBLE:
				return rs1.getDouble(i) == rs2.getDouble(i);

			case Types.TIMESTAMP:
				return rs1.getTimestamp(i).equals(rs2.getTimestamp(i));

			default:
				// treat everything else as a String (should deal with ENUM and
				// TEXT)
				if (rs1.getString(i) == null || rs2.getString(i) == null) {
					return true; // ????
				} else {
					return rs1.getString(i).equals(rs2.getString(i));
				}

			} // switch

		} catch (SQLException se) {
			se.printStackTrace();
		}

		return true;

	} // compareColumns

	// -------------------------------------------------------------------------
	/**
	 * Print a ResultSet to standard out. Optionally limit the number of rows.
	 * 
	 * @param maxRows
	 *          The maximum number of rows to print. -1 to print all rows.
	 * @param rs
	 *          The ResultSet to print.
	 */
	public static void printResultSet(ResultSet rs, int maxRows) {

		int row = 0;

		try {
			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					System.out.print(rs.getString(i) + "\t");
				}
				System.out.println("");
				if (maxRows != -1 && ++row >= maxRows) {
					break;
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}

	} // printResultSet

	// -------------------------------------------------------------------------
	/**
	 * Gets the database name, without the jdbc:// prefix.
	 * 
	 * @param con
	 *          The Connection to query.
	 * @return The name of the database (everything after the last / in the JDBC URL).
	 */
	public static String getShortDatabaseName(Connection con) {

		String url = null;

		try {
			url = con.getMetaData().getURL();
		} catch (SQLException se) {
			se.printStackTrace();
		}
		String name = url.substring(url.lastIndexOf('/') + 1);

		return name;

	} // getShortDatabaseName

	// -------------------------------------------------------------------------
	/**
	 * Generate a name for a temporary database. Should be fairly unique; name is _temp_{user}_{time} where user is current user and
	 * time is current time in ms.
	 * 
	 * @return The temporary name. Will not have any spaces.
	 */
	public static String generateTempDatabaseName() {

		StringBuffer buf = new StringBuffer("_temp_");
		buf.append(System.getProperty("user.name"));
		buf.append("_" + System.currentTimeMillis());
		String str = buf.toString();
		str = str.replace(' ', '_'); // filter any spaces

		logger.fine("Generated temporary database name: " + str);

		return str;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the table names.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @return An array of Strings representing the names of the tables, obtained from the SHOW TABLES command.
	 */
	public static String[] getTableNames(Connection con) {

		List<String> result = new ArrayList<String>();

		if (con == null) {
			logger.severe("getTableNames(): Database connection is null");
		}

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW TABLES");

			while (rs.next()) {
				result.add(rs.getString(1));
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return (String[]) result.toArray(new String[result.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the table names that match a particular SQL pattern.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param pattern
	 *          The SQL pattern to match the table names against.
	 * @return An array of Strings representing the names of the tables.
	 */
	public static String[] getTableNames(Connection con, String pattern) {

		List<String> result = new ArrayList<String>();

		if (con == null) {
			logger.severe("getTableNames(): Database connection is null");
		}

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + pattern + "'");

			while (rs.next()) {
				result.add(rs.getString(1));
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return (String[]) result.toArray(new String[result.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * List the columns in a particular table.
	 * 
	 * @param table
	 *          The name of the table to list.
	 * @param con
	 *          The connection to use.
	 * @return A List of Strings representing the column names.
	 */
	public static List<String> getColumnsInTable(Connection con, String table) {

		List<String> result = new ArrayList<String>();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("DESCRIBE " + table);

			while (rs.next()) {
				result.add(rs.getString(1));
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * List the column information in a table - names, types, defaults etc.
	 * 
	 * @param table
	 *          The name of the table to list.
	 * @param con
	 *          The connection to use.
	 * @param typeFilter
	 *          If not null, only return columns whose types start with this string (case insensitive).
	 * @return A List of 6-element String[] arrays representing: 0: Column name 1: Type 2: Null? 3: Key 4: Default 5: Extra
	 */
	public static List<String[]> getTableInfo(Connection con, String table, String typeFilter) {

		List<String[]> result = new ArrayList<String[]>();

		if (typeFilter != null) {
			typeFilter = typeFilter.toLowerCase();
		}

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("DESCRIBE " + table);

			while (rs.next()) {
				String[] info = new String[6];
				for (int i = 0; i < 6; i++) {
					info[i] = rs.getString(i + 1);
				}
				if (typeFilter == null || info[1].toLowerCase().startsWith(typeFilter)) {
					result.add(info);
				}
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Execute SQL and writes results to ReportManager.info().
	 * 
	 * @param testCase
	 *          testCase which created the sql statement
	 * @param con
	 *          connection to execute sql on.
	 * @param sql
	 *          sql statement to execute.
	 */
	public static void printRows(EnsTestCase testCase, Connection con, String sql) {

		try {
			ResultSet rs = con.createStatement().executeQuery(sql);
			if (rs.next()) {
				int nCols = rs.getMetaData().getColumnCount();
				StringBuffer line = new StringBuffer();
				do {
					line.delete(0, line.length());
					for (int i = 1; i <= nCols; ++i) {
						line.append(rs.getString(i));
						if (i < nCols) {
							line.append("\t");
						}

					}
					ReportManager.info(testCase, con, line.toString());
				} while (rs.next());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------------------------------------------

	/**
	 * Get the meta_value for a named key in the meta table.
	 */
	public static String getMetaValue(Connection con, String key) {

		String result = "";

		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT meta_value FROM meta WHERE meta_key='" + key + "'");
			if (rs.next()) {
				result = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get main database server list - build if necessary. Assumes properties file already read.
	 */
	public static List<DatabaseServer> getMainDatabaseServers() {

		Utils.readPropertiesFileIntoSystem("database.properties", false);
		
		if (mainDatabaseServers == null) {

			mainDatabaseServers = new ArrayList<DatabaseServer>();

			checkAndAddDatabaseServer(mainDatabaseServers, "host", "port", "user", "password", "driver");

			checkAndAddDatabaseServer(mainDatabaseServers, "host1", "port1", "user1", "password1", "driver1");

			checkAndAddDatabaseServer(mainDatabaseServers, "host2", "port2", "user2", "password2", "driver2");
			
		}

	logger.fine("Number of main database servers found: " + mainDatabaseServers.size());
		
		return mainDatabaseServers;

	}

	// -------------------------------------------------------------------------
	/**
	 * Look for secondary database servers.
	 */
	public static List<DatabaseServer> getSecondaryDatabaseServers() {

		Utils.readPropertiesFileIntoSystem("database.properties", false);

		if (secondaryDatabaseServers == null) {

			secondaryDatabaseServers = new ArrayList<DatabaseServer>();

			checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host", "secondary.port", "secondary.user", "secondary.password", "secondary.driver");
			
			checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host1", "secondary.port1", "secondary.user1", "secondary.password1", "secondary.driver1");
			
			checkAndAddDatabaseServer(secondaryDatabaseServers, "secondary.host2", "secondary.port2", "secondary.user2", "secondary.password2", "secondary.driver2");

		}

		logger.fine("Number of secondary database servers found: " + secondaryDatabaseServers.size());

		return secondaryDatabaseServers;

	}

	// -------------------------------------------------------------------------
	/**
	 * Check for the existence of a particular database server. Assumes properties file has already been read in. If it exists, add it to the list.
	 */
	private static void checkAndAddDatabaseServer(List<DatabaseServer> servers, String hostProp, String portProp, String userProp, String passwordProp, String driverProp) {

		if (System.getProperty(hostProp) != null && System.getProperty(portProp) != null && System.getProperty(userProp) != null) {

			DatabaseServer server = new DatabaseServer(System.getProperty(hostProp), System.getProperty(portProp), System.getProperty(userProp), System.getProperty(passwordProp), System.getProperty(driverProp));
			servers.add(server);
			logger.fine("Added server: " + server.toString());

		}

	}

	// -------------------------------------------------------------------------

	public static DatabaseRegistry getSecondaryDatabaseRegistry() {

		if (secondaryDatabaseRegistry == null) {

			secondaryDatabaseRegistry = new DatabaseRegistry(null, null, null, true);

		}

		return secondaryDatabaseRegistry;

	}
	
//-------------------------------------------------------------------------

	public static DatabaseRegistry getMainDatabaseRegistry() {

		if (mainDatabaseRegistry == null) {

			mainDatabaseRegistry = new DatabaseRegistry(null, null, null, false);

		}

		return mainDatabaseRegistry;

	}
	// -------------------------------------------------------------------------

} // DBUtils
