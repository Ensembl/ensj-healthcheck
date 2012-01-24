/*
 * Copyright (C) 2004 EBI, GRL
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

package org.ensembl.healthcheck.testcase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SQLParser;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;

/**
 * Base class for all healthcheck tests.
 */

public abstract class EnsTestCase {

	/** the string that is contained in the name of backup tables */
	public static final String backupIdentifier = "backup_";

	/* comparison flags */
	public static final int COMPARE_LEFT = 0;

	public static final int COMPARE_RIGHT = 1;

	public static final int COMPARE_BOTH = 2;

	/** The TestRunner associated with this EnsTestCase */
	protected TestRunner testRunner;

	/**
	 * A list of Strings representing the groups that this test is a member of. Every test is (at least) a member of a group with the
	 * same name as the test.
	 */
	protected List<String> groups;

	/** Description field */
	protected String description;

	/** Priority field */
	protected Priority priority = null;

	/** Effect field */
	protected String effect = null;

	/** Fix field */
	protected String fix = null;

	/** Optional text to be printed when the test fails */
	protected String failureText;

	/** Which team is responsible for fixing this healthcheck */
	protected Team teamResponsible;

	/** Sometimes more than one team can be responsible */
	protected Team secondTeamResponsible;

	/** Logger object to use */
	protected static Logger logger = Logger.getLogger(EnsTestCase.class.getCanonicalName());

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		EnsTestCase.logger = logger;
	}

	/**
	 * Boolean variable that can be set if the test case is likely to take a long time to run
	 */
	protected boolean hintLongRunning = false;

	/**
	 * Store a list of which types of database this test applies to.
	 */
	protected List<DatabaseType> appliesToTypes = new ArrayList<DatabaseType>();

	/**
	 * Names of tables in core schema that count as "feature" tables. Used in various healthchecks.
	 */

	private String[] featureTables = { "assembly_exception", "gene", "exon", "dna_align_feature", "protein_align_feature", "repeat_feature", "simple_feature", "marker_feature", "misc_feature",
			"qtl_feature", "karyotype", "transcript", "density_feature", "prediction_exon", "prediction_transcript", "ditag_feature", "splicing_event" };

	/**
	 * Tables that have an analysis ID.
	 */

	private String[] tablesWithAnalysisID = { "gene", "protein_feature", "dna_align_feature", "protein_align_feature", "repeat_feature", "prediction_transcript", "simple_feature", "marker_feature",
			"qtl_feature", "density_type", "object_xref", "transcript", "unmapped_object", "ditag_feature" };

	/**
	 * Names of tables in funcgen schema that count as "feature" tables. Used in various healthchecks.
	 */

	private String[] funcgenFeatureTables = { "probe_feature", "annotated_feature", "regulatory_feature", "external_feature", "motif_feature" };

	/**
	 * Funcgen tables that have an analysis ID.
	 */

	private String[] funcgenTablesWithAnalysisID = { "probe_feature", "object_xref", "unmapped_object", "feature_set", "result_set" };

	// do we need to add analysis_description here?

	/**
	 * A DatabaseRegistryEntry pointing to the production database.
	 */
	DatabaseRegistryEntry productionDBRE = null;

	// -------------------------------------------------------------------------
	/**
	 * Creates a new instance of EnsTestCase
	 */
	public EnsTestCase() {

		groups = new ArrayList<String>();
		addToGroup(getShortTestName()); // each test is in a one-test group
		setDescription("No description set for this test.");
		setFailureText("");

	} // EnsTestCase

	// -------------------------------------------------------------------------

	/**
	 * Get the TestRunner that is controlling this EnsTestCase.
	 * 
	 * @return The parent TestRunner.
	 */
	public TestRunner getTestRunner() {

		return testRunner;

	} // getTestRunner

	// -------------------------------------------------------------------------

	/**
	 * Sets up this test. <B>Must </B> be called before the object is used.
	 * 
	 * @param tr
	 *          The TestRunner to associate with this test. Usually just <CODE>
	 *          this</CODE> if being called from the TestRunner.
	 */
	public void init(TestRunner tr) {

		this.testRunner = tr;

	} // init

	// -------------------------------------------------------------------------

	/**
	 * Gets the full name of this test.
	 * 
	 * @return The full name of the test, e.g. org.ensembl.healthcheck.EnsTestCase
	 */
	public String getTestName() {

		return this.getClass().getName();

	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the full name of this test.
	 * 
	 * @return The full name of the test, e.g. org.ensembl.healthcheck.EnsTestCase
	 */
	public String getName() {

		return this.getClass().getName();

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the short form of the test name, ie the name of the test class without the package qualifier.
	 * 
	 * @return The short test name, e.g. EnsTestCase
	 */
	public String getShortTestName() {

		String longName = getTestName();

		return longName.substring(longName.lastIndexOf('.') + 1);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the very short form of the test name; ie that returned by getShortTestName() without the trailing "TestCase"
	 * 
	 * @return The very short test name, e.g. CheckMetaTables
	 */
	public String getVeryShortTestName() {

		String name = getShortTestName();

		return name.substring(0, name.lastIndexOf("TestCase"));

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the names of the groups which this test case is a member of.
	 * 
	 * @return The list of names as Strings.
	 */
	public List<String> getGroups() {

		return groups;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the groups that this test case is a member of, formatted for easy printing.
	 * 
	 * @return The comma-separated list of group names.
	 */
	public String getCommaSeparatedGroups() {
	  return StringUtils.join(groups, ',');
	}

	// -------------------------------------------------------------------------
	/**
	 * Remove a test from all groups.
	 */
	public void removeFromAllGroups() {

		groups = new ArrayList<String>();

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for assigning this test case to several groups at once.
	 * 
	 * @param s
	 *          A list of Strings containing the group names.
	 */
	public void setGroups(List<String> s) {

		groups = s;

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for assigning this test case to several groups at once.
	 * 
	 * @param s
	 *          Array of group names.
	 */
	public void setGroups(String[] s) {

		for (int i = 0; i < s.length; i++) {
			groups.add(s[i]);
		}
	}

	// -------------------------------------------------------------------------
	/**
	 * Add this test case to a new group. If the test case is already a member of the group, a warning is printed and it is not added
	 * again.
	 * 
	 * @param newGroupName
	 *          The name of the new group.
	 */
	public void addToGroup(String newGroupName) {

		if (!groups.contains(newGroupName)) {
			groups.add(newGroupName);
		} else {
			logger.warning(getTestName() + " is already a member of " + newGroupName + " not added again.");
		}

	} // addToGroup

	// -------------------------------------------------------------------------
	/**
	 * Remove this test case from the specified group. If the test case is not a member of the specified group, a warning is printed.
	 * 
	 * @param groupName
	 *          The name of the group from which this test case is to be removed.
	 */
	public void removeFromGroup(String groupName) {

		if (groups.contains(groupName)) {
			groups.remove(groupName);
		} else {
			logger.warning(getTestName() + " was not a memeber of " + groupName);
		}

	} // removeFromGroup

	// -------------------------------------------------------------------------
	/**
	 * Test if this test case is a member of a particular group.
	 * 
	 * @param group
	 *          The name of the group to check.
	 * @return True if this test case is a member of the named group, false otherwise.
	 */
	public boolean inGroup(String group) {

		return groups.contains(group);

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for checking if this test case belongs to any of several groups.
	 * 
	 * @param checkGroups
	 *          The list of group names to check.
	 * @return True if this test case is in any of the groups, false if it is in none.
	 */
  public boolean inGroups(List<String> checkGroups) {

    boolean result = false;

    Iterator<String> it = checkGroups.iterator();
    while (it.hasNext()) {
      if (inGroup((String) it.next())) {
        result = true;
      }
    }
    return result;

  } // inGroups
	
	/**
	 * Produce an instance of {@link SqlTemplate} from a
	 * {@link DatabaseRegistryEntry}.
	 */
	public SqlTemplate getSqlTemplate(DatabaseRegistryEntry dbre) {
	  return new ConnectionBasedSqlTemplateImpl(dbre);
	}

	/**
	 * Produce an instance of {@link SqlTemplate} from a
	 * {@link Connection}.
	 */
	public SqlTemplate getSqlTemplate(Connection conn) {
	  return new ConnectionBasedSqlTemplateImpl(conn);
	}

	// -------------------------------------------------------------------------
	/**
	 * Count the number of rows in a table.
	 * 
	 * @param con
	 *          The database connection to use. Should have been opened already.
	 * @param table
	 *          The name of the table to analyse.
	 * @return The number of rows in the table.
	 */
	public int countRowsInTable(Connection con, String table) {

		if (con == null) {
			logger.severe("countRowsInTable: Database connection is null");
		}

		return getRowCount(con, "SELECT COUNT(*) FROM " + table);

	} // countRowsInTable

	// -------------------------------------------------------------------------
	/**
	 * Use SELECT COUNT(*) to get a row count.
	 */
	public int getRowCountFast(Connection con, String sql) {

		int result = -1;

		try {
			Statement stmt = con.createStatement();
			// System.out.println("Executing " + sql);
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				if (rs.first()) {
					result = rs.getInt(1);
				} else {
					result = -1; // probably signifies an empty ResultSet
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	} // getRowCountFast

	// -------------------------------------------------------------------------
	/**
	 * Use a row-by-row approach to counting the rows in a table.
	 */
	private int getRowCountSlow(Connection con, String sql) {

		int result = -1;

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				if (rs.last()) {
					result = rs.getRow();
				} else {
					result = 0; // probably signifies an empty ResultSet
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	} // getRowCountSlow

	// -------------------------------------------------------------------------
	/**
	 * Count the rows in a particular table or query.
	 * 
	 * @param con
	 *          A connection to the database. Should already be open.
	 * @param sql
	 *          The SQL to execute. Note that if possible this should begin with <code>SELECT COUNT FROM</code> since this is much
	 *          quicker to execute. If a standard SELECT statement is used, a row-by-row count will be performed, which may be slow if
	 *          the table is large.
	 * @return The number of matching rows, or -1 if the query did not execute for some reason.
	 */
	public int getRowCount(Connection con, String sql) {

		if (con == null) {
			logger.severe("getRowCount: Database connection is null");
		}
		int result = -1;

		// if the query starts with SELECT COUNT and does not include a GROUP
		// BY clause
		// we can execute it and just take the first result, which is the count
		if (sql.toLowerCase().indexOf("select count") >= 0 && sql.toLowerCase().indexOf("group by") < 0) {

			result = getRowCountFast(con, sql);

		} else if (sql.toLowerCase().indexOf("select count") < 0) {
			// otherwise, do it row-by-row

			logger.fine("getRowCount() executing SQL which does not appear to begin with SELECT COUNT - performing row-by-row count, which may take a long time if the table is large.");
			result = getRowCountSlow(con, sql);

		}

		return result;

	} // getRowCount

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the value of one column of one row. Only the FIRST row matched is returned.
	 * 
	 * @param con
	 *          The Connection to use.
	 * @param sql
	 *          The SQL to check; should return ONE value.
	 * @return The value returned by the SQL.
	 */
	public String getRowColumnValue(Connection con, String sql) {

		String result = "";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null && rs.first()) {
				result = rs.getString(1);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	} // getRowColumnValue

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the value of the columns of one row. Only the FIRST row matched is returned.
	 * 
	 * @param con
	 *          The Connection to use.
	 * @param sql
	 *          The SQL to check; can return several values.
	 * @return The value(s) returned by the SQL in an array of Strings.
	 */
	public String[] getRowValues(Connection con, String sql) {

		ArrayList<String> list = new ArrayList<String>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null && rs.first()) {
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					list.add(rs.getString(i));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return (String[]) list.toArray(new String[list.size()]);

	} // getRowValues

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the values of one column of the result.
	 * 
	 * @param con
	 *          The Connection to use.
	 * @param sql
	 *          The SQL to check; should return ONE column.
	 * @return The value(s) making up the column, in the order that they were read.
	 */
	public String[] getColumnValues(Connection con, String sql) {

		ArrayList<String> list = new ArrayList<String>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					list.add(rs.getString(1));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return (String[]) list.toArray(new String[list.size()]);

	} // getColumnValues

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the values of one column of the result.
	 * 
	 * @param con
	 *          The Connection to use.
	 * @param sql
	 *          The SQL to check; should return ONE column.
	 * @return The value(s) making up the column, in the order that they were read.
	 */
	public List<String> getColumnValuesList(Connection con, String sql) {

		return Arrays.asList(getColumnValues(con, sql));

	} // getColumnValues

	// -------------------------------------------------------------------------
	/**
	 * Verify foreign-key relations.
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table1
	 *          With col1, specifies the first key to check.
	 * @param col1
	 *          Column in table1 to check.
	 * @param table2
	 *          With col2, specifies the second key to check.
	 * @param col2
	 *          Column in table2 to check.
	 * @param oneWayOnly
	 *          If false, only a "left join" is performed on table1 and table2. If false, the
	 * @return The number of "orphans"
	 */
	public int countOrphans(Connection con, String table1, String col1, String table2, String col2, boolean oneWayOnly) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
		}

		int resultLeft, resultRight;

		String sql = " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2 + " IS NULL";

		resultLeft = getRowCount(con, "SELECT COUNT(*)" + sql);

		logger.finest("Left: " + resultLeft);

		if (resultLeft > 0) {
			String[] values = getColumnValues(con, "SELECT " + table1 + "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table1 + "." + col1 + " " + values[i] + " is not linked.");
			}
		}

		if (!oneWayOnly) {
			// and the other way ... (a right join?)
			sql = " FROM " + table2 + " LEFT JOIN " + table1 + " ON " + table2 + "." + col2 + " = " + table1 + "." + col1 + " WHERE " + table1 + "." + col1 + " IS NULL";

			resultRight = getRowCount(con, "SELECT COUNT(*)" + sql);

			if (resultRight > 0) {
				String[] values = getColumnValues(con, "SELECT " + table2 + "." + col2 + sql + " LIMIT 20");
				for (int i = 0; i < values.length; i++) {
					ReportManager.info(this, con, table2 + "." + col2 + " " + values[i] + " is not linked.");
				}
			}

			logger.finest("Right: " + resultRight);

		} else {
			resultRight = 0;
		}

		// logger.finest("Left: " + resultLeft + " Right: " + resultRight);

		return resultLeft + resultRight;

	} // countOrphans

	// -------------------------------------------------------------------------
	/**
	 * Verify foreign-key relations.
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table1
	 *          With col1, specifies the first key to check.
	 * @param col1
	 *          Column in table1 to check.
	 * @param table2
	 *          With col2, specifies the second key to check.
	 * @param col2
	 *          Column in table2 to check.
	 * @param constraint1
	 *          additional constraint on a column in table1
	 * @return The number of "orphans"
	 */
	public int countOrphansWithConstraint(Connection con, String table1, String col1, String table2, String col2, String constraint1) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
		}

		int resultLeft;

		String sql = " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2 + " iS NULL";

		sql = sql + " AND " + table1 + "." + constraint1;

		resultLeft = getRowCount(con, "SELECT COUNT(*)" + sql);
		if (resultLeft > 0) {
			String[] values = getColumnValues(con, "SELECT " + table1 + "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table1 + "." + col1 + " " + values[i] + " is not linked.");
			}
		}

		logger.finest("Left: " + resultLeft);

		return resultLeft;

	} // countOrphans

	// -------------------------------------------------------------------------
	/**
	 * Generic way to check for orphan foreign key relationships.
	 * 
	 * @return true If there are no orphans.
	 */
	public boolean checkForOrphans(Connection con, String table1, String col1, String table2, String col2, boolean oneWay) {

		logger.finest("Checking for orphans with:\t" + table1 + "." + col1 + " " + table2 + "." + col2 + ". oneWay is " + oneWay);

		int orphans = countOrphans(con, table1, col1, table2, col2, oneWay);

		boolean result = true;

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " IS NULL";

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> " + table2 + " using FK " + col1 + "(" + col2 + ")" + " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			if (!oneWay) {
				String useful_sql2 = "SELECT " + table2 + "." + col2 + " FROM " + table2 + " LEFT JOIN " + table1 + " ON " + table2 + "." + col2 + " = " + table1 + "." + col1 + " WHERE " + table1 + "."
						+ col1 + " IS NULL";
				ReportManager.problem(this, con, "alternate useful SQL: " + useful_sql2);
			}
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1 + " -> " + table2 + " using FK " + col1 + ", look at the StackTrace if any");
			result = false;
		}

		return result;
		/*
		 * if (orphans > 0) { ReportManager.problem(this, con, table1 + " <-> " + table2 + " has " + orphans + " unlinked entries"); }
		 * else { ReportManager.correct(this, con, "All " + table1 + " <-> " + table2 + " relationships are OK"); }
		 * 
		 * return orphans == 0;
		 */
	} // checkForOrphans

	// -------------------------------------------------------------------------
	/**
	 * Verify multiple appearance of a given foreign key
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table
	 *          With col, specifies the foreign key to check.
	 * @param col
	 *          Column in table to check.
	 * @return The number of "singles"
	 */
	public int countSingles(Connection con, String table, String col) {

		if (con == null) {
			logger.severe("countSingles: Database connection is null");
		}

		int result = 0;

		String sql = " FROM " + table + " GROUP BY (" + col + ") HAVING COUNT(*) = 1";

		result = getRowCount(con, "SELECT *" + sql);

		if (result > 0) {
			String[] values = getColumnValues(con, "SELECT " + table + "." + col + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table + "." + col + " " + values[i] + " is used only once.");
			}
		}

		logger.finest("Singles: " + result);

		return result;

	} // countSingles

	// -------------------------------------------------------------------------
	/**
	 * Verify multiple appearance of a given foreign key
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table
	 *          With col, specifies the foreign key to check.
	 * @param col
	 *          Column in table1 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForSingles(Connection con, String table, String col) {

		int singles = 0;
		boolean result = true;

		singles = countSingles(con, table, col);

		String useful_sql = "SELECT " + table + "." + col + " FROM " + table + " GROUP BY (" + col + ") HAVING COUNT(*) = 1";

		if (singles > 0) {
			ReportManager.problem(this, con, "FAILED " + table + "." + col + " is a FK for a 1 to many (>1) relationship");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + singles + " " + table + "." + col + " entries are used only once");
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (singles < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table + "." + col + " is a FK for a 1 to many (>1) relationship, look at the StackTrace if any");
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		}

		return result;

	} // checkForSingles

	// -------------------------------------------------------------------------
	/**
	 * Check that a particular SQL statement has the same result when executed on more than one database.
	 * 
	 * @return True if all matched databases provide the same result, false otherwise.
	 * @param sql
	 *          The SQL query to execute.
	 * @param regexp
	 *          A regexp matching the database names to check.
	 */
	public boolean checkSameSQLResult(String sql, String regexp, boolean comparingSchema) {

		ArrayList<ResultSet> resultSetGroup = new ArrayList<ResultSet>();
		ArrayList<Statement> statements = new ArrayList<Statement>();

		DatabaseRegistry mainDatabaseRegistry = DBUtils.getMainDatabaseRegistry();

		for (DatabaseRegistryEntry dbre : mainDatabaseRegistry.getMatching(regexp)) {

			Connection con = dbre.getConnection();

			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if (rs != null) {
					resultSetGroup.add(rs);
				}
				logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
				// note that the Statement can't be closed here as we use the
				// ResultSet elsewhere so store a reference to it for closing
				// later
				statements.add(stmt);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
		boolean same = DBUtils.compareResultSetGroup(resultSetGroup, this, comparingSchema);

		for (Iterator<Statement> it = statements.iterator(); it.hasNext();) {
			try {
				it.next().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return same;

	} // checkSameSQLResult

	// -------------------------------------------------------------------------
	/**
	 * Check that a particular SQL statement has the same result when executed on more than one database.
	 * 
	 * @return True if all matched databases provide the same result, false otherwise.
	 * @param sql
	 *          The SQL query to execute.
	 * @param databases
	 *          The DatabaseRegistryEntries on which to execute sql.
	 */
	public boolean checkSameSQLResult(String sql, DatabaseRegistryEntry[] databases, boolean comparingSchema) {

	  List<ResultSet> resultSetGroup = new ArrayList<ResultSet>();
	  List<Statement> statements = new ArrayList<Statement>();

	  for (int i = 0; i < databases.length; i++) {

	    Connection con = databases[i].getConnection();

	    try {
	      Statement stmt = con.createStatement();
	      // System.out.println(databases[i].getName() + " " + sql);
	      ResultSet rs = stmt.executeQuery(sql);
	      if (rs != null) {
	        resultSetGroup.add(rs);
	      }
	      logger.fine("Added ResultSet for " + DBUtils.getShortDatabaseName(con) + ": " + sql);
	      // DBUtils.printResultSet(rs, 100);
	      // note that the Statement can't be closed here as we use the
	      // ResultSet elsewhere
	      // so store a reference to it for closing later
	      statements.add(stmt);
	      // con.close();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

	  logger.finest("Number of ResultSets to compare: " + resultSetGroup.size());
	  boolean same = DBUtils.compareResultSetGroup(resultSetGroup, this, comparingSchema);

	  Iterator<Statement> it = statements.iterator();
	  while (it.hasNext()) {
	    try {
	      ((Statement) it.next()).close();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

	  return same;

	} // checkSameSQLResult

	// -------------------------------------------------------------------------
	/**
	 * Check for the presence of a particular String in a table column.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The name of the table to examine.
	 * @param column
	 *          The name of the column to look in.
	 * @param str
	 *          The string to search for; can use database wildcards (%, _) Note that if you want to search for one of these special
	 *          characters, it must be backslash-escaped.
	 * @return The number of times the string is matched.
	 */
	public int findStringInColumn(Connection con, String table, String column, String str) {

		if (con == null) {
			logger.severe("findStringInColumn: Database connection is null");
		}

		String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " LIKE \"" + str + "\"";
		logger.fine(sql);

		return getRowCount(con, sql);

	} // findStringInColumn

	// -------------------------------------------------------------------------
	/**
	 * Check that all entries in column match a particular pattern.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The name of the table to examine.
	 * @param column
	 *          The name of the column to look in.
	 * @param pattern
	 *          The SQL pattern (can contain _,%) to look for.
	 * @return The number of columns that <em>DO NOT</em> match the pattern.
	 */
	public int checkColumnPattern(Connection con, String table, String column, String pattern) {

		// @todo - what about NULLs?

		// cheat by looking for any rows that DO NOT match the pattern
		String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " NOT LIKE \"" + pattern + "\"";
		logger.fine(sql);

		return getRowCount(con, sql);

	} // checkColumnPattern

	// -------------------------------------------------------------------------
	/**
	 * Check that all entries in column match a particular value.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The name of the table to examine.
	 * @param column
	 *          The name of the column to look in.
	 * @param value
	 *          The string to look for (not a pattern).
	 * @return The number of columns that <em>DO NOT</em> match value.
	 */
	public int checkColumnValue(Connection con, String table, String column, String value) {

		// @todo - what about NULLs?

		// cheat by looking for any rows that DO NOT match the pattern
		String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " != '" + value + "'";
		logger.fine(sql);

		return getRowCount(con, sql);

	} // checkColumnPattern

	// -------------------------------------------------------------------------
	/**
	 * Check if there are any blank entires in a column that is not supposed to be null.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The table to use.
	 * @param column
	 *          The column to examine.
	 * @return An list of the row indices of any blank entries. Will be zero-length if there are none.
	 */
  public List<String> checkBlankNonNull(Connection con, String table, String column) {

    if (con == null) {
      logger.severe("checkBlankNonNull (column): Database connection is null");
      return null;
    }

    List<String> blanks = new ArrayList<String>();
    try {
      String sql = "SELECT " + column + " FROM " + table;
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      ResultSetMetaData rsmd = rs.getMetaData();
      while (rs.next()) {
        String columnValue = rs.getString(1);
        // should it be non-null?
        if (rsmd.isNullable(1) == ResultSetMetaData.columnNoNulls) {
          if (columnValue == null || columnValue.equals("")) {
            blanks.add(Integer.toString(rs.getRow()));
          }
        }
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return blanks;

  } // checkBlankNonNull

	// -------------------------------------------------------------------------
	/**
	 * Check all columns of a table for blank entires in columns that are marked as being NOT NULL.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The table to use.
	 * @return The total number of blank null enums.
	 */
	public int checkBlankNonNull(Connection con, String table) {

		if (con == null) {
			logger.severe("checkBlankNonNull (table): Database connection is null");
			return 0;
		}

		int blanks = 0;

		String sql = "SELECT * FROM " + table;

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columnValue = rs.getString(i);
					String columnName = rsmd.getColumnName(i);
					// should it be non-null?
					if (rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls) {
						if (columnValue == null || columnValue.equals("")) {
							blanks++;
							logger.warning("Found blank non-null value in column " + columnName + " in " + table);
						}
					}
				} // for column
			} // while rs
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blanks;

	} // checkBlankNonNull

	// -------------------------------------------------------------------------
	/**
	 * Check if a particular table exists in a database.
	 * 
	 * @param con
	 *          The database connection to check.
	 * @param table
	 *          The table to check for.
	 * @return true if the table exists in the database.
	 */
	public boolean checkTableExists(Connection con, String table) {

		String tables = getRowColumnValue(con, "SHOW TABLES LIKE '" + table + "'");

		boolean result = false;
		if (tables != null && tables.length() != 0) {
			result = true;
		}

		return result;

	} // checkTableExists

	// -------------------------------------------------------------------------
	/**
	 * Print a warning message about a specific database.
	 * 
	 * @param con
	 *          The database connection involved.
	 * @param message
	 *          The message to print.
	 */
	protected void warn(Connection con, String message) {

		logger.warning("Problem in " + DBUtils.getShortDatabaseName(con));
		logger.warning(message);

	} // warn

	// -------------------------------------------------------------------------
	/**
	 * Get the description.
	 * 
	 * @return The description for this test.
	 */
	public String getDescription() {

		return description;

	} // getDescription

	// -------------------------------------------------------------------------
	/**
	 * Set the text description of this test case.
	 * 
	 * @param s
	 *          The new description.
	 */
	public void setDescription(String s) {

		description = s;

	} // setDescription

	// -------------------------------------------------------------------------
	/**
	 * Get the failure text.
	 * 
	 * @return The failure text for this test.
	 */
	public String getFailureText() {

		return failureText;

	} // getFailureText

	// -------------------------------------------------------------------------
	/**
	 * Set the text failure text of this test case.
	 * 
	 * @param s
	 *          The new failure text.
	 */
	public void setFailureText(String s) {

		failureText = s;

	} // setFailureText

	// -------------------------------------------------------------------------
	/**
	 * Read a database schema from a file and create a temporary database from it.
	 * 
	 * @param fileName
	 *          The name of the schema to read.
	 * @return A connection to a database built from the schema.
	 */
	public Connection importSchema(String fileName) {

		Connection con = null;

		// ----------------------------------------------------
		// Parse the file first in case there are problems
		SQLParser sqlParser = new SQLParser();

		// try {
		// List sqlCommands = sqlParser.parse(fileName);
		// // sqlParser.printLines();
		// } catch (FileNotFoundException fnfe) {
		// fnfe.printStackTrace();
		// }

		// ----------------------------------------------------
		// create the database

		String tempDBName = DBUtils.generateTempDatabaseName();

		// read properties file
		String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "database.properties";
		Utils.readPropertiesFileIntoSystem(propsFile, false);
		logger.fine("Read database properties from " + propsFile);

		try {

			Class.forName(System.getProperty("driver"));
			Connection tmpCon = DriverManager.getConnection(System.getProperty("databaseURL"), System.getProperty("user"), System.getProperty("password"));

			String sql = "CREATE DATABASE " + tempDBName;
			logger.finest(sql);
			Statement stmt = tmpCon.createStatement();
			stmt.execute(sql);
			logger.fine("Database " + tempDBName + " created!");

			// close the temporary connection and create a "real" one
			tmpCon.close();
			con = DriverManager.getConnection(System.getProperty("databaseURL") + tempDBName, System.getProperty("user"), System.getProperty("password"));

		} catch (Exception e) {
			String msg = "Could not create database "+tempDBName;
			logger.severe(msg);
			throw new RuntimeException(msg,e);
		}

		// ----------------------------------------------------
		// Build the schema

		try {

			Statement stmt = con.createStatement();

			// Fill the batch of SQL commands
			stmt = sqlParser.populateBatch(stmt);

			// execute the batch that has been built up previously
			logger.info("Creating temporary database ...");
			stmt.executeBatch();
			logger.info("Done.");

			// close statement
			stmt.close();

		} catch (Exception e) {

			String msg = "Could not load schema for database "+tempDBName;
			logger.severe(msg);
			throw new RuntimeException(msg,e);

		}

		return con;
	}

	// -------------------------------------------------------------------------
	/**
	 * Remove a whole database. Generally should *only* be used with temporary databases. Use at your own risk!
	 * 
	 * @param con
	 *          The connection pointing to the database to remove. Should be connected as a user that has sufficient permissions to
	 *          remove it.
	 */
	public void removeDatabase(Connection con) {

		String dbName = DBUtils.getShortDatabaseName(con);

		try {

			String sql = "DROP DATABASE " + dbName;
			logger.finest(sql);
			Statement stmt = con.createStatement();
			stmt.execute(sql);
			logger.fine("Database " + dbName + " removed!");

		} catch (Exception e) {

			String msg = "Could not drop database "+dbName;
			logger.severe(msg);
			throw new RuntimeException(msg,e);

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the table names.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @return An array of Strings representing the names of the tables, obtained from the SHOW TABLES command.
	 */
	public String[] getTableNames(Connection con) {

		return DBUtils.getTableNames(con);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the table names that match a particular pattern.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param pattern
	 *          The pattern to use - note that this is a <em>SQL</em> pattern, not a regexp.
	 * @return An array of Strings representing the names of the tables that match the pattern.
	 */
	public String[] getTableNames(Connection con, String pattern) {

		return DBUtils.getTableNames(con, pattern);

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for getting a connection to a named schema.
	 * 
	 * @param schema
	 *          The name of the schema to connect to.
	 * @return A connection to schema.
	 */
	public Connection getSchemaConnection(String schema) {

		DatabaseRegistryEntry dbre = DBUtils.getMainDatabaseRegistry().getByExactName(schema);

		return dbre.getConnection();

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a whole table as a ResultSet
	 * 
	 * @param table
	 *          The table to get.
	 * @return A ResultSet containing the contents of the table.
	 */
	public ResultSet getWholeTable(Connection con, String table, String key) {

		ResultSet rs = null;

		try {

			Statement stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + table + " ORDER BY " + key);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get all the rows from certain columns of a table, specifying which ones to ignore.
	 * 
	 * @param table
	 *          The table to query.
	 * @param exceptionColumns
	 *          A list of columns to ignore.
	 * @return A ResultSet containing the contents of the table, minus the columns in question.
	 */
	public ResultSet getWholeTableExceptSomeColumns(Connection con, String table, String key, List<String> exceptionColumns, String whereClause) {

		ResultSet rs = null;

		List<String> allColumns = DBUtils.getColumnsInTable(con, table);
		allColumns.removeAll(exceptionColumns);

		String columns = StringUtils.join(allColumns, ",");

		try {

			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(String.format("SELECT %s FROM %s %s ORDER BY %s", columns, table, whereClause, key));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a connection to a new database given a pattern.
	 * 
	 * @param dbPattern
	 *          - a String pattern to identify the required database
	 * 
	 * @return A DatabaseRegistryEntry.
	 */
	public DatabaseRegistryEntry getDatabaseRegistryEntryByPattern(String dbPattern) {

		// create it
		List<String> list = new ArrayList<String>();
		list.add(dbPattern);
		DatabaseRegistryEntry newDBRE = null;

		DatabaseRegistry newDBR = new DatabaseRegistry(list, null, null, false);

		if (newDBR.getEntryCount() == 0) {

			logger.warning("Can't connect to database " + dbPattern + ". Skipping.");
			return null;

		} else if (newDBR.getEntryCount() > 1) {

			logger.warning("Found " + newDBR.getEntryCount() + " databases matching pattern " + dbPattern + ". Only one expected. Skipping.");
			return null;
		}

		newDBRE = newDBR.getAll()[0];
		logger.finest("Got new db: " + newDBRE.getName());
		return newDBRE;
	}

	// -------------------------------------------------------------------------
	/**
	 * Get a new DatabaseRegistry given a pattern.
	 * 
	 * @param dbPattern
	 *          - a String pattern to identify the required databases
	 * 
	 * @return A DatabaseRegistry.
	 */
	public DatabaseRegistry getDatabaseRegistryByPattern(String dbPattern) {

		// create it
		List<String> list = new ArrayList<String>();
		list.add(dbPattern);

		return new DatabaseRegistry(list, null, null, false);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a connection to the production database.
	 * 
	 * @param table
	 *          The name of the schema to connect to.
	 * @return A ResultSet containing the contents of the table.
	 */
	public DatabaseRegistryEntry getProductionDatabase() {

		// return existing one if we already have it, otherwise use method above
		// to find it
		return productionDBRE != null ? productionDBRE : getDatabaseRegistryEntryByPattern("ensembl_production");

	}

	// -------------------------------------------------------------------------
	/**
	 * Compare the contents of a table in the production database with one in another database.
	 */
	public boolean compareProductionTable(DatabaseRegistryEntry dbre, String tableName, String tableKey, String productionTableName, String productionKey) {

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry productionDBRE = getProductionDatabase();

		return DBUtils.compareResultSets(getWholeTable(con, tableName, tableKey), getWholeTable(productionDBRE.getConnection(), productionTableName, productionKey), this, "", true, false, tableName,
				null, false);

	}

	// -------------------------------------------------------------------------
	/**
	 * Compare the contents of a table in the production database with one in another database, but ignore certain columns.
	 */
	public boolean compareProductionTableWithExceptions(DatabaseRegistryEntry dbre, String tableName, String tableKey, String productionTableName, String productionKey, List<String> exceptionColumns) {

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry productionDBRE = getProductionDatabase();

		return DBUtils.compareResultSets(getWholeTableExceptSomeColumns(con, tableName, tableKey, exceptionColumns, ""),
				getWholeTableExceptSomeColumns(productionDBRE.getConnection(), productionTableName, productionKey, exceptionColumns, "WHERE is_current=1"), this, "", true, false, tableName, null, false);

	}

	// -------------------------------------------------------------------------
	/**
	 * Compare two schemas to see if they have the same tables. The comparison is done in both directions, so will return false if a
	 * table exists in schema1 but not in schema2, <em>or</em> if a table exists in schema2 but not in schema2.
	 * 
	 * @param schema1
	 *          The first schema to compare.
	 * @param schema2
	 *          The second schema to compare.
	 * @return true if all tables in schema1 exist in schema2, and vice-versa.
	 */
	public boolean compareTablesInSchema(Connection schema1, Connection schema2) {
		return compareTablesInSchema(schema1, schema2, false, COMPARE_BOTH);
	}

	/**
	 * Compare two schemas to see if they have the same tables. The comparison can be done in in one direction or both directions.
	 * 
	 * @param schema1
	 *          The first schema to compare.
	 * @param schema2
	 *          The second schema to compare.
	 * @param ignoreBackupTables
	 *          Should backup tables be excluded form this check?
	 * @param directionFlag
	 *          The direction to perform comparison in, either EnsTestCase.COMPARE_RIGHT, EnsTestCase.COMPARE_LEFT or
	 *          EnsTestCase.COMPARE_BOTH
	 * @return for left comparison: all tables in schema1 exist in schema2 for right comparison: all tables in schema1 exist in
	 *         schema2 for both: if all tables in schema1 exist in schema2, and vice-versa
	 */
	public boolean compareTablesInSchema(Connection schema1, Connection schema2, boolean ignoreBackupTables, int directionFlag) {

		boolean result = true;
		if (directionFlag == COMPARE_RIGHT || directionFlag == COMPARE_BOTH) {// perfom
			// right
			// compare
			// if
			// required
			result = compareTablesInSchema(schema2, schema1, ignoreBackupTables, COMPARE_LEFT);
		}

		if (directionFlag == COMPARE_LEFT || directionFlag == COMPARE_BOTH) {// perform
			// left
			// compare
			// if
			// required
			String name1 = DBUtils.getShortDatabaseName(schema1);
			String name2 = DBUtils.getShortDatabaseName(schema2);

			// check each table in turn
			String[] tables = getTableNames(schema1);
			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				if (!ignoreBackupTables || !table.contains(backupIdentifier)) {
					if (!checkTableExists(schema2, table)) {
						ReportManager.problem(this, schema1, "Table " + table + " exists in " + name1 + " but not in " + name2);
						result = false;
					}
				}
			}
		}

		return result;

	}

	// -------------------------------------------------------------------------

	/**
	 * Check if the current test has repair capability. Signified by implementing the Repair interface.
	 * 
	 * @return True if this test implements Repair, false otherwise.
	 */
	public boolean canRepair() {

		return (this instanceof Repair);

	}

	// -------------------------------------------------------------------------
	/**
	 * Check if a table has rows.
	 * 
	 * @param con
	 *          The connection to the database to use.
	 * @param table
	 *          The table to check.
	 * @return true if the table has >0 rows, false otherwise.
	 */
	public boolean tableHasRows(Connection con, String table) {

		return (getRowCount(con, "SELECT COUNT(*) FROM " + table) > 0);

	}

	// -------------------------------------------------------------------------
	/**
	 * See if the "hintLongRunning" flag is set.
	 * 
	 * @return The value of the hintLongRunning flag.
	 */
	public boolean isLongRunning() {

		return hintLongRunning;

	}

	// -------------------------------------------------------------------------
	/**
	 * Set the flag that indicates that this test may take a long time to run.
	 * 
	 * @param b
	 *          The new value of the flag.
	 */
	public void setHintLongRunning(boolean b) {

		hintLongRunning = b;

	}

	// ---------------------------------------------------------------------
	/**
	 * Check if this test case applies to a particular DatabaseType.
	 * 
	 * @param t
	 *          The database type to check against.
	 * @return true if this test applies to databases of type t.
	 */
	public boolean appliesToType(DatabaseType t) {

		Iterator<DatabaseType> it = appliesToTypes.iterator();
		while (it.hasNext()) {
			DatabaseType type = (DatabaseType) it.next();
			if (t.equals(type)) {
				return true;
			}
		}

		return false;

	}

	// -----------------------------------------------------------------
	/**
	 * Add another database type to the list of types that this test case applies to.
	 * 
	 * @param t
	 *          The new type.
	 */
	public void addAppliesToType(DatabaseType t) {

		appliesToTypes.add(t);

	}

	// -----------------------------------------------------------------
	/**
	 * Remove a database type from the list of types that this test case applies to.
	 * 
	 * @param t
	 *          The type to remove.
	 */
	public void removeAppliesToType(DatabaseType t) {

		appliesToTypes.remove(t);

	}

	// -----------------------------------------------------------------
	/**
	 * Specify the database types that a test applies to.
	 * 
	 * @param types
	 *          A List of DatabaseTypes - overwrites the current setting.
	 */
	public void setAppliesToTypes(List<DatabaseType> types) {

		appliesToTypes = types;

	}

//-----------------------------------------------------------------
	/**
	 * Convenience method for specifying that a test only applies to one type.
	 * 
	 * @param type
	 *          A DatabaseType - overwrites the current setting.
	 */
	public void setAppliesToType(DatabaseType type) {

		List<DatabaseType> types = new ArrayList<DatabaseType>();
		types.add(type);
		appliesToTypes = types;

	}
	// -----------------------------------------------------------------
	/**
	 * @return the list of database types that a test applies to.
	 */
	public DatabaseType[] getAppliesToTypes() {

		return (DatabaseType[]) appliesToTypes.toArray(new DatabaseType[appliesToTypes.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * Set the database type(s) that this test applies to based upon the directory name. For directories called "generic", the type is
	 * set to core, otherfeatures, cdna, rnaseq, vega and sanger_vega. For all other directories the type is set based upon the
	 * directory name.
	 * 
	 * @param dirName
	 *          The directory name to check.
	 */
	public void setTypeFromDirName(String dirName) {

		List<DatabaseType> types = new ArrayList<DatabaseType>();

		if (dirName.equals("generic")) {

			types.add(DatabaseType.CORE);
			types.add(DatabaseType.VEGA);
			types.add(DatabaseType.CDNA);
			types.add(DatabaseType.OTHERFEATURES);
			types.add(DatabaseType.SANGER_VEGA);
			types.add(DatabaseType.RNASEQ);

			logger.finest("Set generic types for " + getName());

		} else {

			DatabaseType type = DatabaseType.resolveAlias(dirName);
			if (type != DatabaseType.UNKNOWN) {

				types.add(type);
				logger.finest("Set type to " + type.toString() + " for " + getName());
			} else {
				logger.finest("Cannot deduce test type from directory name " + dirName + " for " + getName());
			}
		}

		setAppliesToTypes(types);

	}

	/**
	 * Helper method to set the applicable types for this test from the parent package. For instance, if the package is
	 * org.ensembl.healthcheck.testcase.core, then core will be set as the type. This method delegates to
	 * {@link #setTypeFromDirName(String)} using the parent package string as an argument
	 */
	public void setTypeFromPackageName() {
		String packageName = this.getClass().getPackage().getName();
		String parent = packageName.substring(packageName.lastIndexOf('.') + 1);
		setTypeFromDirName(parent);
	}

	// -------------------------------------------------------------------------

	/**
	 * This method can be overridden in subclasses to define (via addAppliesToType/removeAppliesToType) which types of databases the
	 * test applies to.
	 */
	public void types() {

	}

	// -------------------------------------------------------------------------
	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if necessary.
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table1
	 *          With col1, specifies the first key to check.
	 * @param col1
	 *          Column in table1 to check.
	 * @param table2
	 *          With col2, specifies the second key to check.
	 * @param col2
	 *          Column in table2 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphans(Connection con, String table1, String col1, String table2, String col2) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphans(con, table1, col1, table2, col2, true);

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " iS NULL";

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> " + table2 + " using FK " + col1 + "(" + col2 + ")" + " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1 + " -> " + table2 + " using FK " + col1 + ", look at the StackTrace if any");
			result = false;
		}

		return result;

	} // checkForOrphans

	// -------------------------------------------------------------------------
	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if necessary.
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table1
	 *          With col1, specifies the first key to check.
	 * @param col1
	 *          Column in table1 to check.
	 * @param table2
	 *          With col2, specifies the second key to check.
	 * @param col2
	 *          Column in table2 to check.
	 * @param constraint1
	 *          additional constraint on a column in table1
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphansWithConstraint(Connection con, String table1, String col1, String table2, String col2, String constraint1) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphansWithConstraint(con, table1, col1, table2, col2, constraint1);

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1 + " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " iS NULL";

		// System.out.println(table1 + "." + col1 + "." + table2 + "." + col2);

		if (!constraint1.equals("")) {
			useful_sql = useful_sql + " AND " + table1 + "." + constraint1;
		}

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> " + table2 + " using FK " + col1 + "(" + col2 + ")" + " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1 + " -> " + table2 + " using FK " + col1 + ", look at the StackTrace if any");
			result = false;
		} else {
			ReportManager.correct(this, con, "SUCCESS: All rows in " + table1 + " (constraint is: " + constraint1 + ") refer to valid " + table2 + "s");
		}

		return result;

	} // checkForOrphansWithConstraint

	// -------------------------------------------------------------------------
	/**
	 * Verify optional foreign-key relations. The methods checks that non-NULL foreign keys point to valid primary keys.
	 * 
	 * @param con
	 *          A connection to the database to be tested. Should already be open.
	 * @param table1
	 *          With col1, specifies the first key to check.
	 * @param col1
	 *          Column in table1 to check.
	 * @param table2
	 *          With col2, specifies the second key to check.
	 * @param col2
	 *          Column in table2 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkOptionalRelation(Connection con, String table1, String col1, String table2, String col2) {
		return checkForOrphansWithConstraint(con, table1, col1, table2, col2, col1 + " IS NOT NULL");
	}

	// ----------------------------------------------------------------------
	/**
	 * Check that a particular column has no null values. Problem or correct reports are generated via ReportManager.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The table name.
	 * @param column
	 *          The column to check.
	 * @return True if no columns are null, false otherwise.
	 */
	public boolean checkNoNulls(Connection con, String table, String column) {

		boolean result = true;

		int nulls = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + " IS NULL");

		if (nulls > 0) {

			ReportManager.problem(this, con, nulls + " NULL values in " + table + "." + column);
			result = false;

		} else {

			ReportManager.correct(this, con, "No NULL values in " + table + "." + column);
		}

		return result;

	} // checkNoNulls
	
	/**
	 * Check a column for zero values. Problem or correct reports are generated via ReportManager.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @param table
	 *          The table name.
	 * @param column
	 *          The column to check.
	 * @return True if no columns have zero values, false otherwise.
	 */
	public boolean checkNoZeroes(Connection con, String table, String column) {
		
		boolean result = true;
		
		int zeroes = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE" + column + "= 0");
		
		if (zeroes > 0) {
			
			ReportManager.problem(this, con, "Zeroes found in " + table + "." + column);
			result = false;
		} else {
			ReportManager.correct(this, con, "No zeroes found in " + table + "." + column);
		}
		
		return result;
	}
	
	

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a core schema that conform to various characteristics and count as "feature" tables.
	 * 
	 * @return An array of feature tables.
	 */
	public String[] getCoreFeatureTables() {

		return featureTables;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a core schema that have an analysis_id colmun.
	 * 
	 * @return An array of table names.
	 */
	public String[] getCoreTablesWithAnalysisID() {

		return tablesWithAnalysisID;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a funcgen schema that conform to various characteristics and count as "feature" tables.
	 * 
	 * @return An array of feature tables.
	 */
	public String[] getFuncgenFeatureTables() {

		return funcgenFeatureTables;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a funcgen schema that have an analysis_id colmun.
	 * 
	 * @return An array of table names.
	 */
	public String[] getFuncgenTablesWithAnalysisID() {

		return funcgenTablesWithAnalysisID;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get the equivalent database from the secondary database server. "equivalent" means: same database type and species. If more
	 * than one database on the secondary server has the same type and species, then the one with the highest version number is used.
	 * 
	 * @param dbre
	 *          The database to find the equivalent for.
	 * @return The database on the secondary server with the same type and species, and the highest version number, or null if none is
	 *         found.
	 */
	public DatabaseRegistryEntry getEquivalentFromSecondaryServer(DatabaseRegistryEntry dbre) {

		DatabaseRegistry secondaryDatabaseRegistry = DBUtils.getSecondaryDatabaseRegistry();

		// find any databases matching type and species
		TreeSet<DatabaseRegistryEntry> matchingDBs = new TreeSet<DatabaseRegistryEntry>(); // get
		// sorting
		// for
		// free

		for (DatabaseRegistryEntry secDBRE : secondaryDatabaseRegistry.getAll()) {
			if (dbre.getSpecies() == Species.UNKNOWN) {
				// EG where we don't know the species, use type and alias
				// matching instead
				if (dbre.getType() == secDBRE.getType() && dbre.getAlias().equals(secDBRE.getAlias())) {
					matchingDBs.add(secDBRE);
					logger.finest("added " + secDBRE.getName() + " to list of databases to check for equivalent to " + dbre.getName());
				}
			} else {
				// nulls will set type automatically
				if (dbre.getType() == secDBRE.getType() && dbre.getSpecies() == secDBRE.getSpecies()) {
					matchingDBs.add(secDBRE);
					logger.finest("added " + secDBRE.getName() + " to list of databases to check for equivalent to " + dbre.getName());
				}
			}
		}

		if (matchingDBs.size() == 0) {
			logger.severe("Could not find equivalent database to " + dbre.getName() + " on secondary server");
		}

		// take the highest one that doesn't have the same version number as our
		// current one, if available
		DatabaseRegistryEntry result = null;

		matchingDBs.remove(dbre); // remove the current database from the list
		// to avoid comparisons with itself

		if (matchingDBs.size() > 0) {

			result = (DatabaseRegistryEntry) matchingDBs.last();

		}

		return result;

	}

	// ------------------------------------------------------------------------------------------
	
	public boolean checkDatabaseExistsByType(DatabaseRegistryEntry dbre, DatabaseType dbType) {
		
		List<String> regexps = new ArrayList<String>();
		regexps.add(".*");

		DatabaseRegistry allDBR = new DatabaseRegistry(regexps, null, null, false);

		for (DatabaseRegistryEntry eachDBRE : allDBR.getAll(dbType)) {
			if ( dbre.getSpecies().equals(Species.UNKNOWN) && dbre.getAlias().equals(eachDBRE.getAlias()) )  {
				// EG where we don't know the species, use type and alias
				// matching instead
					return true;
			}
			else {
				if (dbre.getSpecies().equals(eachDBRE.getSpecies())) {
					return true;
				}
			}
		}
		return false;
	}
	
	// ----------------------------------------------------------------------
	/**
	 * Get a list of the logic names and analysis IDs from the analysis table.
	 * 
	 * @param con
	 *          The connection to use.
	 * @return A map of analysis IDs (keys) and logic names (values).
	 */
	public Map<String, String> getLogicNamesFromAnalysisTable(Connection con) {

		Map<String, String> map = new HashMap<String, String>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT analysis_id, logic_name FROM analysis");
			while (rs.next()) {
				map.put(rs.getString("analysis_id"), rs.getString("logic_name"));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}

		return map;

	}

	// ----------------------------------------------------------------------
	/**
	 * Define how severe the effect of a test's failure would be. Note that this is a test-level priority; within a testcase the
	 * ReportManager methods (problem, correct etc) should be used.
	 * 
	 * @param p
	 *          The new priority to set.
	 */
	public void setPriority(Priority p) {

		priority = p;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get the priority.
	 */
	public Priority getPriority() {

		return priority;

	}

	// ----------------------------------------------------------------------
	/**
	 * Define how what will happen if databases which fail this healthcheck are left unfixed.
	 * 
	 * @param e
	 *          The effect to set.
	 */
	public void setEffect(String e) {

		effect = e;

	}

	// ----------------------------------------------------------------------
	/**
	 * Return what will happen if databases which fail this healthcheck are left unfixed.
	 */
	public String getEffect() {

		return effect;

	}

	// ----------------------------------------------------------------------
	/**
	 * Describe (as text) a possible fix for the problem causing this healthcheck to fail.
	 * 
	 * @param f
	 *          The fix to set.
	 */
	public void setFix(String f) {

		fix = f;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get (as text) a possible fix for the problem causing this healthcheck to fail.
	 */
	public String getFix() {

		return fix;

	}

	// ----------------------------------------------------------------------

	public Team getTeamResponsible() {
		return teamResponsible;
	}

	// ----------------------------------------------------------------------

	public void setTeamResponsible(Team teamResponsible) {
		this.teamResponsible = teamResponsible;
	}

	// ----------------------------------------------------------------------

	public Team getSecondTeamResponsible() {
		return secondTeamResponsible;
	}

	// ----------------------------------------------------------------------

	public void setSecondTeamResponsible(Team secondTeamResponsible) {
		this.secondTeamResponsible = secondTeamResponsible;
	}


	public void removeSecondTeamResponsible(){
		this.secondTeamResponsible = null;
	}


	// ----------------------------------------------------------------------

	public String getPrintableTeamResponsibleString() {

		if (getTeamResponsible()==null) {
			
			return "The team responsible has not been set.";
		}
		
		String team = getTeamResponsible().toString();

		if (getSecondTeamResponsible() != null) {
			team += " and " + getSecondTeamResponsible();
		}

		return team;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get the names of the top level seq_regions.
	 */
	public List<String> getTopLevelNames(Connection con) {

		List<String> names = new ArrayList<String>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT sr.name FROM seq_region sr, seq_region_attrib sra, attrib_type at WHERE sra.seq_region_id=sr.seq_region_id AND sra.attrib_type_id=at.attrib_type_id AND at.code='toplevel'");
			while (rs.next()) {
				names.add(rs.getString(1));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}

		return names;

	}

	/**
	 * Get the names of the top level seq_regions that are called chromosomes.
	 */
	public List<String> getTopLevelChromosomeNames(Connection con) {

		List<String> names = new ArrayList<String>();

		try {
			Statement stmt = con.createStatement();
			// ResultSet rs = stmt
			// .executeQuery("SELECT sr.name FROM seq_region sr, seq_region_attrib sra, attrib_type at, coord_system cs WHERE cs.coord_system_id=sr.coord_system_id AND sra.seq_region_id=sr.seq_region_id AND sra.attrib_type_id=at.attrib_type_id AND at.code='toplevel' AND cs.name='chromosome' AND cs.attrib LIKE '%default_version%'");
			ResultSet rs = stmt
					.executeQuery("SELECT sr.name FROM seq_region sr, seq_region_attrib sra, attrib_type at, coord_system cs WHERE cs.coord_system_id=sr.coord_system_id AND sra.seq_region_id=sr.seq_region_id AND sra.attrib_type_id=at.attrib_type_id AND at.code='toplevel' AND cs.name='chromosome' AND cs.attrib LIKE '%default_version%' and sr.seq_region_id not in (select sr2.seq_region_id from seq_region sr2, seq_region_attrib sra1, attrib_type at1 where sr2.seq_region_id = sra1.seq_region_id and sra1.attrib_type_id = at1.attrib_type_id and at1.code = 'non_ref')");
			while (rs.next()) {
				names.add(rs.getString(1));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}

		return names;

	}

	/**
	 * Return the list of views and tables that are required to be present in the funcgen database before Biomart can run, but should
	 * be removed afterwards.
	 */
	public String[] getBiomartFuncgenTablesAndViews() {

		String[] t = { "cs_sr_view", "fs_displayable_view", "regulatory_feature_view", "external_feature_ox_view", "external_feature_view", "annotated_feature_view", "feature_set_view",
				"probestuff_helper_tmp" };

		return t;

	}
	
	
	public long getChecksum(Connection con, String tableName) {

		long checksumValue = 0;
		
		try {

			Statement stmt = con.createStatement();
	
			String sqlQuery = "CHECKSUM TABLE " + tableName; 
			
			ResultSet rs = stmt.executeQuery(sqlQuery);
			
			if (rs != null && rs.first()) {
				checksumValue = rs.getLong(2);
			}	
	
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return checksumValue;
	}

	// ----------------------------------------------------------------------

} // EnsTestCase
