/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.configurationmanager.ConfigurationException;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SQLParser;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;
import org.ensembl.healthcheck.util.Utils;

/**
 * Base class for all healthcheck tests.
 */

public abstract class EnsTestCase {

	/** the string that is contained in the name of backup tables */
	public static final String backupIdentifier = "backup_";


	/** The TestRunner associated with this EnsTestCase */
	protected TestRunner testRunner;

	/**
	 * A list of Strings representing the groups that this test is a member of.
	 * Every test is (at least) a member of a group with the same name as the
	 * test.
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
	protected static Logger logger = Logger.getLogger(EnsTestCase.class
			.getCanonicalName());

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		EnsTestCase.logger = logger;
	}

	/**
	 * Boolean variable that can be set if the test case is likely to take a
	 * long time to run
	 */
	protected boolean hintLongRunning = false;

	/**
	 * Store a list of which types of database this test applies to.
	 */
	protected List<DatabaseType> appliesToTypes = new ArrayList<DatabaseType>();

	/**
	 * Names of tables in core schema that count as "feature" tables. Used in
	 * various healthchecks.
	 */

	private String[] featureTables = { "assembly_exception", "gene", "exon",
			"dna_align_feature", "protein_align_feature", "repeat_feature",
			"simple_feature", "marker_feature", "misc_feature",
			"karyotype", "transcript", "density_feature", "prediction_exon",
			"prediction_transcript", "ditag_feature" };

	/**
	 * Tables that have an analysis ID.
	 */

	private String[] tablesWithAnalysisID = { "gene", "protein_feature",
			"dna_align_feature", "protein_align_feature", "repeat_feature",
			"prediction_transcript", "simple_feature", "marker_feature",
			"density_type", "object_xref", "transcript",
                        "intron_supporting_evidence", "operon",  "operon_transcript", 
			"unmapped_object", "ditag_feature", "data_file" };

	/**
	 * Names of tables in funcgen schema that count as "feature" tables. Used in
	 * various healthchecks.
	 */

	private String[] funcgenFeatureTables = { "probe_feature",
			"peak", "regulatory_feature", "external_feature",
			"motif_feature", "mirna_target_feature" };

	/**
	 * Funcgen tables that have an analysis ID.
	 */

	private String[] funcgenTablesWithAnalysisID = { "probe_feature",
			"object_xref", "unmapped_object", "feature_set", "result_set" }; // also feature_type, but this is marked for removal.

	protected boolean setSystemProperties = true;

	// do we need to add analysis_description here?

	public boolean isSetSystemProperties() {
		return setSystemProperties;
	}

	public void setSetSystemProperties(boolean setSystemProperties) {
		this.setSystemProperties = setSystemProperties;
	}

	/**
	 * A DatabaseRegistryEntry pointing to the production database.
	 */
	DatabaseRegistryEntry productionDBRE = null;
	
	/**
	 * A DatabaseRegistryEntry pointing to the Compara Master database.
	 */
	DatabaseRegistryEntry comparaMasterDbre = null;
	

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
	 *            The TestRunner to associate with this test. Usually just
	 *            <CODE>
	 *          this</CODE> if being called from the TestRunner.
	 */
	public void init(TestRunner tr) {

		this.testRunner = tr;

	} // init

	// -------------------------------------------------------------------------

	/**
	 * Gets the full name of this test.
	 * 
	 * @return The full name of the test, e.g.
	 *         org.ensembl.healthcheck.EnsTestCase
	 */
	public String getTestName() {

		return this.getClass().getName();

	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the full name of this test.
	 * 
	 * @return The full name of the test, e.g.
	 *         org.ensembl.healthcheck.EnsTestCase
	 */
	public String getName() {

		return this.getClass().getName();

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the short form of the test name, ie the name of the test class
	 * without the package qualifier.
	 * 
	 * @return The short test name, e.g. EnsTestCase
	 */
	public String getShortTestName() {

		String longName = getTestName();

		return longName.substring(longName.lastIndexOf('.') + 1);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get the very short form of the test name; ie that returned by
	 * getShortTestName() without the trailing "TestCase"
	 * 
	 * @return The very short test name, e.g. CheckMetaTables
	 */
	public String getVeryShortTestName() {

		String name = getShortTestName();

		return name.substring(0, name.lastIndexOf("TestCase"));

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the names of the groups which this test case is a member
	 * of.
	 * 
	 * @return The list of names as Strings.
	 */
	public List<String> getGroups() {

		return groups;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the groups that this test case is a member of, formatted
	 * for easy printing.
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
	 * Convenience method for assigning this test case to several groups at
	 * once.
	 * 
	 * @param s
	 *            A list of Strings containing the group names.
	 */
	public void setGroups(List<String> s) {

		groups = s;

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for assigning this test case to several groups at
	 * once.
	 * 
	 * @param s
	 *            Array of group names.
	 */
	public void setGroups(String[] s) {

		for (int i = 0; i < s.length; i++) {
			groups.add(s[i]);
		}
	}

	// -------------------------------------------------------------------------
	/**
	 * Add this test case to a new group. If the test case is already a member
	 * of the group, a warning is printed and it is not added again.
	 * 
	 * @param newGroupName
	 *            The name of the new group.
	 */
	public void addToGroup(String newGroupName) {

		if (!groups.contains(newGroupName)) {
			groups.add(newGroupName);
		} else {
			logger.warning(getTestName() + " is already a member of "
					+ newGroupName + " not added again.");
		}

	} // addToGroup

	// -------------------------------------------------------------------------
	/**
	 * Remove this test case from the specified group. If the test case is not a
	 * member of the specified group, a warning is printed.
	 * 
	 * @param groupName
	 *            The name of the group from which this test case is to be
	 *            removed.
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
	 *            The name of the group to check.
	 * @return True if this test case is a member of the named group, false
	 *         otherwise.
	 */
	public boolean inGroup(String group) {

		return groups.contains(group);

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for checking if this test case belongs to any of
	 * several groups.
	 * 
	 * @param checkGroups
	 *            The list of group names to check.
	 * @return True if this test case is in any of the groups, false if it is in
	 *         none.
	 */
	public boolean inGroups(List<String> checkGroups) {

		boolean result = false;

		Iterator<String> it = checkGroups.iterator();
		while (it.hasNext()) {
			if (inGroup((String) it.next())) {
				result = true;
				break;
			}
		}
		return result;

	} // inGroups

	// -------------------------------------------------------------------------
	/**
	 * Print a warning message about a specific database.
	 * 
	 * @param con
	 *            The database connection involved.
	 * @param message
	 *            The message to print.
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
	 *            The new description.
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
	 *            The new failure text.
	 */
	public void setFailureText(String s) {

		failureText = s;

	} // setFailureText

	protected void setConfiguredProperties() {
		// read properties file
		String propsFile = System.getProperty("user.dir")
				+ System.getProperty("file.separator")
				+ TestRunner.getPropertiesFile();
		Utils.readPropertiesFileIntoSystem(propsFile, false);
		logger.fine("Read database properties from " + propsFile);
	}

	// -------------------------------------------------------------------------
	/**
	 * Read a database schema from a file and create a temporary database from
	 * it.
	 * 
	 * @param fileName
	 *            The name of the schema to read.
	 * @return A connection to a database built from the schema.
	 * @throws FileNotFoundException
	 */
	public Connection importSchema(String fileName)
			throws FileNotFoundException {
		
		if (setSystemProperties) {
			setConfiguredProperties();
		}
		
		String databaseURL = System.getProperty("databaseURL");
		String user = System.getProperty("user");
		String password = System.getProperty("password");

		return DBUtils.importSchema(fileName, databaseURL, user, password);

	}

	// -------------------------------------------------------------------------
	/**
	 * Remove a whole database. Generally should *only* be used with temporary
	 * databases. Use at your own risk!
	 * 
	 * @param con
	 *            The connection pointing to the database to remove. Should be
	 *            connected as a user that has sufficient permissions to remove
	 *            it.
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

			String msg = "Could not drop database " + dbName;
			logger.severe(msg);
			throw new RuntimeException(msg, e);

		} finally {
			// closeQuietly(stmt);
		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of all the table names.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @return An array of Strings representing the names of the tables,
	 *         obtained from the SHOW TABLES command.
	 */
	public String[] getTableNames(Connection con) {

		return DBUtils.getTableNames(con);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the table names that match a particular pattern.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param pattern
	 *            The pattern to use - note that this is a <em>SQL</em> pattern,
	 *            not a regexp.
	 * @return An array of Strings representing the names of the tables that
	 *         match the pattern.
	 */
	public String[] getTableNames(Connection con, String pattern) {

		return DBUtils.getTableNames(con, pattern);

	}

	// -------------------------------------------------------------------------
	/**
	 * Convenience method for getting a connection to a named schema.
	 * 
	 * @param schema
	 *            The name of the schema to connect to.
	 * @return A connection to schema.
	 */
	public Connection getSchemaConnection(String schema) {

		DatabaseRegistryEntry dbre = DBUtils.getMainDatabaseRegistry()
				.getByExactName(schema);

		return dbre.getConnection();

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a whole table as a ResultSet
	 * 
	 * @param table
	 *            The table to get.
	 * @return A ResultSet containing the contents of the table.
	 */
	public ResultSet getWholeTable(Connection con, String table, String key) {

		ResultSet rs = null;
		try {

			Statement stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + table + " ORDER BY "
					+ key);

		} catch (Exception e) {
			throw new SqlUncheckedException("Could not retrieve whole table", e);
		} finally {
		}
		return rs;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get all the rows from certain columns of a table, specifying which ones
	 * to ignore.
	 * 
	 * @param table
	 *            The table to query.
	 * @param exceptionColumns
	 *            A list of columns to ignore.
	 * @return A ResultSet containing the contents of the table, minus the
	 *         columns in question.
	 */
	public ResultSet getWholeTableExceptSomeColumns(Connection con,
			String table, String key, List<String> exceptionColumns,
			String whereClause) {

		ResultSet rs = null;

		List<String> allColumns = DBUtils.getColumnsInTable(con, table);
		allColumns.removeAll(exceptionColumns);

		String columns = StringUtils.join(allColumns, ",");

		try {

			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(String.format(
					"SELECT %s FROM %s %s ORDER BY %s", columns, table,
					whereClause, key));

		} catch (Exception e) {
			throw new SqlUncheckedException("Could not retrieve whole table "
					+ table, e);
		} finally {
		}

		return rs;

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a connection to a new database given a pattern.
	 * 
	 * @param dbPattern
	 *            - a String pattern to identify the required database
	 * 
	 * @return A DatabaseRegistryEntry.
	 */
	public DatabaseRegistryEntry getDatabaseRegistryEntryByPattern(
			String dbPattern) {

		// create it
		List<String> list = new ArrayList<String>();
		list.add(dbPattern);
		DatabaseRegistryEntry newDBRE = null;

		DatabaseRegistry newDBR = new DatabaseRegistry(list, null, null, false);

		if (newDBR.getEntryCount() == 0) {

			logger.warning("Can't connect to database " + dbPattern
					+ ". Skipping.");
			return null;

		} else if (newDBR.getEntryCount() > 1) {

			logger.warning("Found " + newDBR.getEntryCount()
					+ " databases matching pattern " + dbPattern
					+ ". Only one expected. Skipping.");
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
	 *            - a String pattern to identify the required databases
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
	 * @return A DatabaseRegistryEntry representing the database
	 */
	public DatabaseRegistryEntry getProductionDatabase() {

		// return existing one if we already have it, otherwise use method above
		// to find it
		return productionDBRE != null ? productionDBRE
				: getDatabaseRegistryEntryByPattern(System.getProperty("production.database"));

	}
	
	protected String getDataFileBasePath() {
		return System.getProperty("dataFileBasePath");
	}
	
	public void setProductionDatabase(DatabaseRegistryEntry productionDBRE ) {
		this.productionDBRE = productionDBRE;
	}

	/**
	 * Get a connection to the Compara master database.
	 * 
	 * @return A DatabaseRegistryEntry representing the database
	 */
	public DatabaseRegistryEntry getComparaMasterDatabase() {

		// return existing one if we already have it, otherwise use method above
		// to find it
		return comparaMasterDbre != null ? comparaMasterDbre
				: getDatabaseRegistryEntryByPattern(System.getProperty("compara_master.database"));

	}
	
	public void setComparaMasterDatabase(DatabaseRegistryEntry comparaMasterDbre) {

		this.comparaMasterDbre = comparaMasterDbre;
		
	}

	// -------------------------------------------------------------------------
	/**
	 * Compare the contents of a table in the production database with one in
	 * another database.
	 */
	public boolean compareProductionTable(DatabaseRegistryEntry dbre,
			String tableName, String tableKey, String productionTableName,
			String productionKey) {

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry productionDBRE = getProductionDatabase();

		return DBUtils.compareResultSets(
				getWholeTable(con, tableName, tableKey),
				getWholeTable(productionDBRE.getConnection(),
						productionTableName, productionKey), this, "", true,
				false, tableName, null, false);

	}

        // -------------------------------------------------------------------------
        /**
         * test if a species is merged
         * connect to the production database using the species production_name
         */
        public boolean isMerged(String species) {
          boolean result = false;
          int rows = DBUtils.getRowCount(getProductionDatabase().getConnection(), "SELECT count(*) FROM species s, attrib_type at WHERE at.attrib_type_id = s.attrib_type_id AND code = 'merged' AND production_name = '" + species + "'");
          if (rows > 0) {
            result = true;
          }
          return result;

        }

	// -------------------------------------------------------------------------
	/**
	 * Compare the contents of a table in the production database with one in
	 * another database, but ignore certain columns.
	 */
	public boolean compareProductionTableWithExceptions(
			DatabaseRegistryEntry dbre, String tableName, String tableKey,
			String productionTableName, String productionKey,
			List<String> exceptionColumns) {

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry productionDBRE = getProductionDatabase();
		
		if(productionDBRE==null || productionDBRE.getConnection()==null) {
		    throw new ConfigurationException("Production database not found");
		}

		return DBUtils.compareResultSets(
				getWholeTableExceptSomeColumns(con, tableName, tableKey,
						exceptionColumns, ""),
				getWholeTableExceptSomeColumns(productionDBRE.getConnection(),
						productionTableName, productionKey, exceptionColumns,
						"WHERE is_current=1"), this, "", true, false,
				tableName, null, false);

	}



	// -------------------------------------------------------------------------

	/**
	 * Check if the current test has repair capability. Signified by
	 * implementing the Repair interface.
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
	 *            The connection to the database to use.
	 * @param table
	 *            The table to check.
	 * @return true if the table has >0 rows, false otherwise.
	 */
	public boolean tableHasRows(Connection con, String table) {

		return (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table) > 0);

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
	 *            The new value of the flag.
	 */
	public void setHintLongRunning(boolean b) {

		hintLongRunning = b;

	}

	// ---------------------------------------------------------------------
	/**
	 * Check if this test case applies to a particular DatabaseType.
	 * 
	 * @param t
	 *            The database type to check against.
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
	 * Add another database type to the list of types that this test case
	 * applies to.
	 * 
	 * @param t
	 *            The new type.
	 */
	public void addAppliesToType(DatabaseType t) {

		appliesToTypes.add(t);

	}

	// -----------------------------------------------------------------
	/**
	 * Remove a database type from the list of types that this test case applies
	 * to.
	 * 
	 * @param t
	 *            The type to remove.
	 */
	public void removeAppliesToType(DatabaseType t) {

		appliesToTypes.remove(t);

	}

	// -----------------------------------------------------------------
	/**
	 * Specify the database types that a test applies to.
	 * 
	 * @param types
	 *            A List of DatabaseTypes - overwrites the current setting.
	 */
	public void setAppliesToTypes(List<DatabaseType> types) {

		appliesToTypes = types;

	}

	// -----------------------------------------------------------------
	/**
	 * Convenience method for specifying that a test only applies to one type.
	 * 
	 * @param type
	 *            A DatabaseType - overwrites the current setting.
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

		return (DatabaseType[]) appliesToTypes
				.toArray(new DatabaseType[appliesToTypes.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * Set the database type(s) that this test applies to based upon the
	 * directory name. For directories called "generic", the type is set to
	 * core, otherfeatures, cdna, rnaseq, presite, vega and sangervega. For all other
	 * directories the type is set based upon the directory name.
	 * 
	 * @param dirName
	 *            The directory name to check.
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
                        types.add(DatabaseType.PRE_SITE);

			logger.finest("Set generic types for " + getName());

		} else {

			DatabaseType type = DatabaseType.resolveAlias(dirName);
			if (type != DatabaseType.UNKNOWN) {

				types.add(type);
				logger.finest("Set type to " + type.toString() + " for "
						+ getName());
			} else {
				logger.finest("Cannot deduce test type from directory name "
						+ dirName + " for " + getName());
			}
		}

		setAppliesToTypes(types);

	}

	/**
	 * Helper method to set the applicable types for this test from the parent
	 * package. For instance, if the package is
	 * org.ensembl.healthcheck.testcase.core, then core will be set as the type.
	 * This method delegates to {@link #setTypeFromDirName(String)} using the
	 * parent package string as an argument
	 */
	public void setTypeFromPackageName() {
		String packageName = this.getClass().getPackage().getName();
		String parent = packageName.substring(packageName.lastIndexOf('.') + 1);
		setTypeFromDirName(parent);
	}

	// -------------------------------------------------------------------------

	/**
	 * This method can be overridden in subclasses to define (via
	 * addAppliesToType/removeAppliesToType) which types of databases the test
	 * applies to.
	 */
	public void types() {

	}

	// -------------------------------------------------------------------------
	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if
	 * necessary.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table1
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            Column in table1 to check.
	 * @param table2
	 *            With col2, specifies the second key to check.
	 * @param col2
	 *            Column in table2 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphans(Connection con, String table1, String col1,
			String table2, String col2) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphans(con, table1, col1, table2, col2, true);

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1
				+ " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = "
				+ table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " iS NULL";

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> "
					+ table2 + " using FK " + col1 + "(" + col2 + ")"
					+ " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans
					+ " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1
					+ " -> " + table2 + " using FK " + col1
					+ ", look at the StackTrace if any");
			result = false;
		}

		return result;

	} // checkForOrphans
		// -------------------------------------------------------------------------

	/**
	 * Verify foreign-key relations.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table1
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            Column in table1 to check.
	 * @param table2
	 *            With col2, specifies the second key to check.
	 * @param col2
	 *            Column in table2 to check.
	 * @param oneWayOnly
	 *            If false, only a "left join" is performed on table1 and
	 *            table2. If false, the
	 * @return The number of "orphans"
	 */
	public int countOrphans(Connection con, String table1, String col1,
			String table2, String col2, boolean oneWayOnly) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
		}

		int resultLeft, resultRight;

		String sql = " FROM " + table1 + " LEFT JOIN " + table2 + " ON "
				+ table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE "
				+ table2 + "." + col2 + " IS NULL";

		resultLeft = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

		logger.finest("Left: " + resultLeft);

		if (resultLeft > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT " + table1
					+ "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table1 + "." + col1 + " "
						+ values[i] + " is not linked.");
			}
		}

		if (!oneWayOnly) {
			// and the other way ... (a right join?)
			sql = " FROM " + table2 + " LEFT JOIN " + table1 + " ON " + table2
					+ "." + col2 + " = " + table1 + "." + col1 + " WHERE "
					+ table1 + "." + col1 + " IS NULL";

			resultRight = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

			if (resultRight > 0) {
				String[] values = DBUtils.getColumnValues(con, "SELECT "
						+ table2 + "." + col2 + sql + " LIMIT 20");
				for (int i = 0; i < values.length; i++) {
					ReportManager.info(this, con, table2 + "." + col2 + " "
							+ values[i] + " is not linked.");
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
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table1
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            Column in table1 to check.
	 * @param table2
	 *            With col2, specifies the second key to check.
	 * @param col2
	 *            Column in table2 to check.
	 * @param constraint1
	 *            additional constraint on a column in table1
	 * @return The number of "orphans"
	 */
	public int countOrphansWithConstraint(Connection con, String table1,
			String col1, String table2, String col2, String constraint1) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
		}

		int resultLeft;

		String sql = " FROM " + table1 + " LEFT JOIN " + table2 + " ON "
				+ table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE "
				+ table2 + "." + col2 + " iS NULL";

		sql = sql + " AND " + table1 + "." + constraint1;

		resultLeft = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);
		if (resultLeft > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT " + table1
					+ "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table1 + "." + col1 + " "
						+ values[i] + " is not linked.");
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
	public boolean checkForOrphans(Connection con, String table1, String col1,
			String table2, String col2, boolean oneWay) {

		logger.finest("Checking for orphans with:\t" + table1 + "." + col1
				+ " " + table2 + "." + col2 + ". oneWay is " + oneWay);

		int orphans = countOrphans(con, table1, col1, table2, col2, oneWay);

		boolean result = true;

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1
				+ " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = "
				+ table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " IS NULL";

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> "
					+ table2 + " using FK " + col1 + "(" + col2 + ")"
					+ " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans
					+ " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			if (!oneWay) {
				String useful_sql2 = "SELECT " + table2 + "." + col2 + " FROM "
						+ table2 + " LEFT JOIN " + table1 + " ON " + table2
						+ "." + col2 + " = " + table1 + "." + col1 + " WHERE "
						+ table1 + "." + col1 + " IS NULL";
				ReportManager.problem(this, con, "alternate useful SQL: "
						+ useful_sql2);
			}
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1
					+ " -> " + table2 + " using FK " + col1
					+ ", look at the StackTrace if any");
			result = false;
		}

		return result;
		/*
		 * if (orphans > 0) { ReportManager.problem(this, con, table1 + " <-> "
		 * + table2 + " has " + orphans + " unlinked entries"); } else {
		 * ReportManager.correct(this, con, "All " + table1 + " <-> " + table2 +
		 * " relationships are OK"); }
		 * 
		 * return orphans == 0;
		 */
	} // checkForOrphans

	// -------------------------------------------------------------------------
	/**
	 * Verify multiple appearance of a given foreign key
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col, specifies the foreign key to check.
	 * @param col
	 *            Column in table to check.
	 * @param constraint
	 *            Subset of the rows to be tested. This SQL constraint must
	 *            include the WHERE keyword. Leave empty for no filtering
	 * @return The number of "singles"
	 */
	public int countSingles(Connection con, String table, String col, String constraint) {

		if (con == null) {
			logger.severe("countSingles: Database connection is null");
		}

		int result = 0;

		String sql = " FROM " + table + " " + constraint + " GROUP BY (" + col
				+ ") HAVING COUNT(*) = 1";

		result = DBUtils.getRowCount(con, "SELECT *" + sql);

		if (result > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT " + table
					+ "." + col + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table + "." + col + " "
						+ values[i] + " is used only once.");
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
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col, specifies the foreign key to check.
	 * @param col
	 *            Column in table1 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForSingles(Connection con, String table, String col) {
		return checkForSinglesWithConstraint(con, table, col, "");
	}

	/**
	 * Verify multiple appearance of a given foreign key
	 *
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col, specifies the foreign key to check.
	 * @param col
	 *            Column in table1 to check.
	 * @param constraint
	 *            Subset of the rows to be tested. This SQL constraint must
	 *            include the WHERE keyword. Leave empty for no filtering
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForSinglesWithConstraint(Connection con, String table, String col, String constraint) {

		int singles = 0;
		boolean result = true;

		singles = countSingles(con, table, col, constraint);

		String useful_sql = "SELECT " + table + "." + col + " FROM " + table + " " + constraint
				+ " GROUP BY (" + col + ") HAVING COUNT(*) = 1";

		if (singles > 0) {
			ReportManager.problem(this, con, "FAILED " + table + "." + col
					+ " is a FK for a 1 to many (>1) relationship");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + singles
					+ " " + table + "." + col + " entries are used only once");
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (singles < 0) {
			ReportManager
					.problem(
							this,
							con,
							"TEST NOT COMPLETED "
									+ table
									+ "."
									+ col
									+ " is a FK for a 1 to many (>1) relationship, look at the StackTrace if any");
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		}

		return result;

	} // checkForSingles
		// -------------------------------------------------------------------------

	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if
	 * necessary.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table1
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            Column in table1 to check.
	 * @param table2
	 *            With col2, specifies the second key to check.
	 * @param col2
	 *            Column in table2 to check.
	 * @param constraint1
	 *            additional constraint on a column in table1
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphansWithConstraint(Connection con, String table1,
			String col1, String table2, String col2, String constraint1) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphansWithConstraint(con, table1, col1, table2, col2,
				constraint1);

		String useful_sql = "SELECT " + table1 + "." + col1 + " FROM " + table1
				+ " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = "
				+ table2 + "." + col2 + " WHERE " + table2 + "." + col2
				+ " iS NULL";

		// System.out.println(table1 + "." + col1 + "." + table2 + "." + col2);

		if (!constraint1.equals("")) {
			useful_sql = useful_sql + " AND " + table1 + "." + constraint1;
		}

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table1 + " -> "
					+ table2 + " using FK " + col1 + "(" + col2 + ")"
					+ " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans
					+ " " + table1 + " entries are not linked to " + table2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1
					+ " -> " + table2 + " using FK " + col1
					+ ", look at the StackTrace if any");
			result = false;
		}

		return result;

	} // checkForOrphansWithConstraint

	// -------------------------------------------------------------------------
	/**
	 * Verify optional foreign-key relations. The methods checks that non-NULL
	 * foreign keys point to valid primary keys.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table1
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            Column in table1 to check.
	 * @param table2
	 *            With col2, specifies the second key to check.
	 * @param col2
	 *            Column in table2 to check.
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkOptionalRelation(Connection con, String table1,
			String col1, String table2, String col2) {
		return checkForOrphansWithConstraint(con, table1, col1, table2, col2,
				col1 + " IS NOT NULL");
	}

	// ----------------------------------------------------------------------
	/**
	 * Check that a particular column has no null values. Problem or correct
	 * reports are generated via ReportManager.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The table name.
	 * @param column
	 *            The column to check.
	 * @return True if no columns are null, false otherwise.
	 */
	public boolean checkNoNulls(Connection con, String table, String column) {

		boolean result = true;

		String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IS NULL",
				table, column);
		int nulls = DBUtils.getRowCount(con, sql);

		if (nulls > 0) {

			ReportManager.problem(this, con, nulls + " NULL values in " + table
					+ "." + column);
			result = false;
		}

		return result;

	} // checkNoNulls

	/**
	 * Check a column for zero values. Problem or correct reports are generated
	 * via ReportManager.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The table name.
	 * @param column
	 *            The column to check.
	 * @return True if no columns have zero values, false otherwise.
	 */
	public boolean checkNoZeroes(Connection con, String table, String column) {

		boolean result = true;
		String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = 0",
				table, column);
		int zeroes = DBUtils.getRowCount(con, sql);

		if (zeroes > 0) {

			ReportManager.problem(this, con, "Zeroes found in " + table + "."
					+ column);
			result = false;
		}

		return result;
	}
	
	/**
	 * Check a column for odd characters. Problem or correct reports are generated
	 * via ReportManager. Applicable to display names, and other presentation strings
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The table name.
	 * @param column
	 *            The column to check.
	 * @return True if column is devoid of bad characters, false otherwise.
	 */
	public boolean checkNoBadCharacters(Connection con, String table, String column) {

		boolean result = true;
		String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s REGEXP '%s'",
				table, column, "^\\[\\:\\;\\n\\r\\t\\~\\]|\\[\\:\\;\\n\\r\\t\\~\\]$");
		// MOAR slashes
		int badrows = DBUtils.getRowCount(con, sql);

		if (badrows > 0) {

			ReportManager.problem(this, con, "Forbidden characters found in "+badrows+" rows of " + table + "."
					+ column);
			result = false;
		}

		return result;
	}

	// -------------------------------------------------------------------------
	/**
	 * Check that a particular SQL statement has the same result when executed
	 * on more than one database.
	 * 
	 * @return True if all matched databases provide the same result, false
	 *         otherwise.
	 * @param sql
	 *            The SQL query to execute.
	 * @param regexp
	 *            A regexp matching the database names to check.
	 * @deprecated moved to
	 *             {@link DBUtils#checkSameSQLResult(EnsTestCase, String, String, boolean)}
	 */
	@Deprecated
	public boolean checkSameSQLResult(String sql, String regexp,
			boolean comparingSchema) {
		return DBUtils.checkSameSQLResult(this, sql, regexp, comparingSchema);
	} // checkSameSQLResult

	// -------------------------------------------------------------------------
	/**
	 * Check that a particular SQL statement has the same result when executed
	 * on more than one database.
	 * 
	 * @return True if all matched databases provide the same result, false
	 *         otherwise.
	 * @param sql
	 *            The SQL query to execute.
	 * @param databases
	 *            The DatabaseRegistryEntries on which to execute sql.
	 * @deprecated moved to
	 *             {@link DBUtils#checkSameSQLResult(EnsTestCase, String, DatabaseRegistryEntry[], boolean)}
	 */
	@Deprecated
	public boolean checkSameSQLResult(String sql,
			DatabaseRegistryEntry[] databases, boolean comparingSchema) {

		return DBUtils
				.checkSameSQLResult(this, sql, databases, comparingSchema);

	} // checkSameSQLResult

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a core schema that conform to various
	 * characteristics and count as "feature" tables.
	 * 
	 * @return An array of feature tables.
	 */
	public String[] getCoreFeatureTables() {

		return featureTables;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a core schema that have an analysis_id
	 * colmun.
	 * 
	 * @return An array of table names.
	 */
	public String[] getCoreTablesWithAnalysisID() {

		return tablesWithAnalysisID;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a funcgen schema that conform to various
	 * characteristics and count as "feature" tables.
	 * 
	 * @return An array of feature tables.
	 */
	public String[] getFuncgenFeatureTables() {

		return funcgenFeatureTables;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the tables in a funcgen schema that have an analysis_id
	 * colmun.
	 * 
	 * @return An array of table names.
	 */
	public String[] getFuncgenTablesWithAnalysisID() {

		return funcgenTablesWithAnalysisID;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get the equivalent database from the secondary database server.
	 * "equivalent" means: same database type and species. If more than one
	 * database on the secondary server has the same type and species, then the
	 * one with the highest version number is used.
	 * 
	 * @param dbre
	 *            The database to find the equivalent for.
	 * @return The database on the secondary server with the same type and
	 *         species, and the highest version number, or null if none is
	 *         found.
	 */
	public DatabaseRegistryEntry getEquivalentFromSecondaryServer(
			DatabaseRegistryEntry dbre) {

		DatabaseRegistry secondaryDatabaseRegistry = DBUtils
				.getSecondaryDatabaseRegistry();

		// find any databases matching type and species
		TreeSet<DatabaseRegistryEntry> matchingDBs = new TreeSet<DatabaseRegistryEntry>(); // get
		// sorting
		// for
		// free

		for (DatabaseRegistryEntry secDBRE : secondaryDatabaseRegistry.getAll()) {
                        if (DBUtils.getSecondaryDatabase() != null) {
                                if (secDBRE.getName().equals(DBUtils.getSecondaryDatabase())) {
                                        return secDBRE;
                                }
                        }
			if (dbre.getSpecies() == DatabaseRegistryEntry.UNKNOWN) {
				// EG where we don't know the species, use type and alias
				// matching instead
				if (dbre.getType().equals(secDBRE.getType())
						&& dbre.getAlias().equals(secDBRE.getAlias())) {
					matchingDBs.add(secDBRE);
					logger.finest("added "
							+ secDBRE.getName()
							+ " to list of databases to check for equivalent to "
							+ dbre.getName());
				}
			} else {
				// nulls will set type automatically
				if (dbre.getType().equals(secDBRE.getType())
						&& dbre.getSpecies().equals(secDBRE.getSpecies())) {
					matchingDBs.add(secDBRE);
					logger.finest("added "
							+ secDBRE.getName()
							+ " to list of databases to check for equivalent to "
							+ dbre.getName());
				}
			}
		}

		if (matchingDBs.size() == 0) {
			logger.finest("Could not find equivalent database to "
					+ dbre.getName() + " on secondary server");
		}

		// take the highest one that doesn't have the same version number as our
		// current one, if available
                DatabaseRegistryEntry result = null;

		if (matchingDBs.size() > 0) {

			result = (DatabaseRegistryEntry) matchingDBs.last();

		}

		return result;

	}

	// ------------------------------------------------------------------------------------------

	public boolean checkDatabaseExistsByType(DatabaseRegistryEntry dbre,
			DatabaseType dbType) {

		// figure out the corresponding database
		String targetName = dbre.getName().replace(dbre.getType().getName(), dbType.getName());
		
		// work out if we have one
		return DBUtils.getSqlTemplate(dbre).queryForDefaultObject("select count(*) from information_schema.tables where table_schema=?", Integer.class, targetName) > 0;
	
	}

	// ----------------------------------------------------------------------
	/**
	 * Get a list of the logic names and analysis IDs from the analysis table.
	 * 
	 * @param con
	 *            The connection to use.
	 * @return A map of analysis IDs (keys) and logic names (values).
	 */
	public Map<Integer, String> getLogicNamesFromAnalysisTable(Connection con) {

		return DBUtils.getSqlTemplate(con).queryForMap(
				"SELECT analysis_id, logic_name FROM analysis",
				new MapRowMapper<Integer, String>() {

					@Override
					public String mapRow(ResultSet resultSet, int position)
							throws SQLException {
						return resultSet.getString("logic_name");
					}

					@Override
					public Map<Integer, String> getMap() {
						return CollectionUtils.createHashMap();
					}

					@Override
					public Integer getKey(ResultSet resultSet)
							throws SQLException {
						return resultSet.getInt("analysis_id");
					}

					@Override
					public void existingObject(String currentValue,
							ResultSet resultSet, int position)
							throws SQLException {
						throw new SqlUncheckedException(
								"Duplicate analysis row found for ID "
										+ currentValue);
					}
				});

	}

	// ----------------------------------------------------------------------
	/**
	 * Define how severe the effect of a test's failure would be. Note that this
	 * is a test-level priority; within a testcase the ReportManager methods
	 * (problem, correct etc) should be used.
	 * 
	 * @param p
	 *            The new priority to set.
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
	 * Define how what will happen if databases which fail this healthcheck are
	 * left unfixed.
	 * 
	 * @param e
	 *            The effect to set.
	 */
	public void setEffect(String e) {

		effect = e;

	}

	// ----------------------------------------------------------------------
	/**
	 * Return what will happen if databases which fail this healthcheck are left
	 * unfixed.
	 */
	public String getEffect() {

		return effect;

	}

	// ----------------------------------------------------------------------
	/**
	 * Describe (as text) a possible fix for the problem causing this
	 * healthcheck to fail.
	 * 
	 * @param f
	 *            The fix to set.
	 */
	public void setFix(String f) {

		fix = f;

	}

	// ----------------------------------------------------------------------
	/**
	 * Get (as text) a possible fix for the problem causing this healthcheck to
	 * fail.
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

	public void removeSecondTeamResponsible() {
		this.secondTeamResponsible = null;
	}

	// ----------------------------------------------------------------------

	public String getPrintableTeamResponsibleString() {

		if (getTeamResponsible() == null) {

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

		String sql = "SELECT sr.name FROM seq_region sr, seq_region_attrib sra, attrib_type at, coord_system cs " +
				"WHERE cs.coord_system_id=sr.coord_system_id AND sra.seq_region_id=sr.seq_region_id " +
				"AND sra.attrib_type_id=at.attrib_type_id AND at.code='toplevel' AND cs.name='chromosome' " +
				"AND cs.attrib LIKE '%default_version%' and sr.seq_region_id not in " +
				"(select sr2.seq_region_id from seq_region sr2, seq_region_attrib sra1, attrib_type at1 " +
				"where sr2.seq_region_id = sra1.seq_region_id and sra1.attrib_type_id = at1.attrib_type_id and at1.code = 'non_ref')";
		return DBUtils.getSqlTemplate(con).queryForDefaultObjectList(sql, String.class);
	}

	/**
	 * Return the list of views and tables that are required to be present in
	 * the funcgen database before Biomart can run, but should be removed
	 * afterwards.
	 */
	public String[] getBiomartFuncgenTablesAndViews() {

		String[] t = { "cs_sr_view", "fs_displayable_view",
				"regulatory_feature_view", "external_feature_ox_view",
				"external_feature_view", "annotated_feature_view",
				"feature_set_view", "probestuff_helper_tmp" };

		return t;

	}

	@Deprecated
	public long getChecksum(Connection con, String tableName) {

		return DBUtils.getChecksum(con, tableName);
	}

	/**
	 * Produce an instance of {@link SqlTemplate} from a
	 * {@link DatabaseRegistryEntry}.
	 */
	public SqlTemplate getSqlTemplate(DatabaseRegistryEntry dbre) {
		return DBUtils.getSqlTemplate(dbre);
	}

	/**
	 * Produce an instance of {@link SqlTemplate} from a {@link Connection}.
	 */
	public SqlTemplate getSqlTemplate(Connection conn) {
		return DBUtils.getSqlTemplate(conn);
	}

	// -------------------------------------------------------------------------
	/**
	 * Count the number of rows in a table.
	 * 
	 * @param con
	 *            The database connection to use. Should have been opened
	 *            already.
	 * @param table
	 *            The name of the table to analyse.
	 * @return The number of rows in the table.
	 * @deprecated use {@link DBUtils#countRowsInTable(Connection, String)}
	 */
	@Deprecated
	public int countRowsInTable(Connection con, String table) {

		return DBUtils.countRowsInTable(con, table);

	} // countRowsInTable

	// -------------------------------------------------------------------------
	/**
	 * Use SELECT COUNT(*) to get a row count.
	 * 
	 * @deprecated use {@link DBUtils#getRowCountFast(Connection, String)}
	 */
	@Deprecated
	public int getRowCountFast(Connection con, String sql) {
		return DBUtils.getRowCountFast(con, sql);
	} // getRowCountFast

	// -------------------------------------------------------------------------
	/**
	 * Use a row-by-row approach to counting the rows in a table.
	 * 
	 * @deprecated use {@link DBUtils#getRowCountSlow(Connection, String)}
	 */
	@Deprecated
	public int getRowCountSlow(Connection con, String sql) {
		return DBUtils.getRowCountSlow(con, sql);
	} // getRowCountSlow

	// -------------------------------------------------------------------------
	/**
	 * Count the rows in a particular table or query.
	 * 
	 * @param con
	 *            A connection to the database. Should already be open.
	 * @param sql
	 *            The SQL to execute. Note that if possible this should begin
	 *            with <code>SELECT COUNT FROM</code> since this is much quicker
	 *            to execute. If a standard SELECT statement is used, a
	 *            row-by-row count will be performed, which may be slow if the
	 *            table is large.
	 * @return The number of matching rows, or -1 if the query did not execute
	 *         for some reason.
	 * @deprecated use {@link DBUtils#getRowCount(Connection, String)}
	 */
	@Deprecated
	public int getRowCount(Connection con, String sql) {
		return DBUtils.getRowCount(con, sql);
	} // getRowCount

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the value of one column of one row.
	 * Only the FIRST row matched is returned.
	 * 
	 * @param con
	 *            The Connection to use.
	 * @param sql
	 *            The SQL to check; should return ONE value.
	 * @return The value returned by the SQL.
	 * @deprecated use {@link DBUtils#getRowColumnValue(Connection, String)}
	 */
	@Deprecated
	public String getRowColumnValue(Connection con, String sql) {
		return DBUtils.getRowColumnValue(con, sql);
	} // DBUtils.getRowColumnValue

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the value of the columns of one row.
	 * Only the FIRST row matched is returned.
	 * 
	 * @param con
	 *            The Connection to use.
	 * @param sql
	 *            The SQL to check; can return several values.
	 * @return The value(s) returned by the SQL in an array of Strings.
	 * @deprecated use {@link DBUtils#getRowValues(Connection, String)}
	 */
	@Deprecated
	public String[] getRowValues(Connection con, String sql) {
		return DBUtils.getRowValues(con, sql);
	} // getRowValues

	@Deprecated
	public List<String[]> getRowValuesList(Connection con, String sql) {
		return DBUtils.getRowValuesList(con, sql);
	}

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the values of one column of the
	 * result.
	 * 
	 * @param con
	 *            The Connection to use.
	 * @param sql
	 *            The SQL to check; should return ONE column.
	 * @return The value(s) making up the column, in the order that they were
	 *         read.
	 * @deprecated use {@link DBUtils#getColumnValues(Connection, String)}
	 */
	@Deprecated
	public String[] getColumnValues(Connection con, String sql) {
		return DBUtils.getColumnValues(con, sql);
	} // getColumnValues

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the values of one column of the
	 * result.
	 * 
	 * @param con
	 *            The Connection to use.
	 * @param sql
	 *            The SQL to check; should return ONE column.
	 * @return The value(s) making up the column, in the order that they were
	 *         read.
	 * @deprecated use {@link DBUtils#getColumnValuesList(Connection, String)}
	 */
	@Deprecated
	public List<String> getColumnValuesList(Connection con, String sql) {
		return DBUtils.getColumnValuesList(con, sql);
	} // getColumnValues

	// -------------------------------------------------------------------------
	/**
	 * Check for the presence of a particular String in a table column.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The name of the table to examine.
	 * @param column
	 *            The name of the column to look in.
	 * @param str
	 *            The string to search for; can use database wildcards (%, _)
	 *            Note that if you want to search for one of these special
	 *            characters, it must be backslash-escaped.
	 * @return The number of times the string is matched.
	 * @deprecated use
	 *             {@link DBUtils#findStringInColumn(Connection, String, String, String)}
	 */
	@Deprecated
	public int findStringInColumn(Connection con, String table, String column,
			String str) {
		return DBUtils.findStringInColumn(con, table, column, str);
	} // findStringInColumn

	// -------------------------------------------------------------------------
	/**
	 * Check that all entries in column match a particular pattern.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The name of the table to examine.
	 * @param column
	 *            The name of the column to look in.
	 * @param pattern
	 *            The SQL pattern (can contain _,%) to look for.
	 * @return The number of columns that <em>DO NOT</em> match the pattern.
	 * @deprecated use
	 *             {@link DBUtils#checkColumnPattern(Connection, String, String, String)}
	 */
	@Deprecated
	public int checkColumnPattern(Connection con, String table, String column,
			String pattern) {
		return DBUtils.checkColumnPattern(con, table, column, pattern);
	} // checkColumnPattern

	// -------------------------------------------------------------------------
	/**
	 * Check that all entries in column match a particular value.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The name of the table to examine.
	 * @param column
	 *            The name of the column to look in.
	 * @param value
	 *            The string to look for (not a pattern).
	 * @return The number of columns that <em>DO NOT</em> match value.
	 * @deprecated use
	 *             {@link DBUtils#checkColumnValue(Connection, String, String, String)}

	 */
	@Deprecated
	public int checkColumnValue(Connection con, String table, String column,
			String value) {
		return DBUtils.checkColumnValue(con, table, column, value);
	} // checkColumnPattern

	// -------------------------------------------------------------------------
	/**
	 * Check if there are any blank entires in a column that is not supposed to
	 * be null.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The table to use.
	 * @param column
	 *            The column to examine.
	 * @return An list of the row indices of any blank entries. Will be
	 *         zero-length if there are none.
	 * @deprecated use
	 *             {@link DBUtils#checkBlankNonNull(Connection, String, String)}
	 */
	@Deprecated
	public List<String> checkBlankNonNull(Connection con, String table,
			String column) {
		return DBUtils.checkBlankNonNull(con, table, column);
	} // checkBlankNonNull

	// -------------------------------------------------------------------------
	/**
	 * Check all columns of a table for blank entires in columns that are marked
	 * as being NOT NULL.
	 * 
	 * @param con
	 *            The database connection to use.
	 * @param table
	 *            The table to use.
	 * @return The total number of blank null enums.
	 * @deprecated Use {@link DBUtils#checkBlankNonNull(Connection, String)}
	 */
	@Deprecated
	public int checkBlankNonNull(Connection con, String table) {
		return DBUtils.checkBlankNonNull(con, table);
	} // checkBlankNonNull

	// -------------------------------------------------------------------------
	/**
	 * Check if a particular table exists in a database.
	 * 
	 * @param con
	 *            The database connection to check.
	 * @param table
	 *            The table to check for.
	 * @return true if the table exists in the database.
	 * @deprecated use {@link DBUtils#checkTableExists}
	 */
	@Deprecated
	public boolean checkTableExists(Connection con, String table) {

		return DBUtils.checkTableExists(con, table);

	} // checkTableExists

	// -------------------------------------------------------------------------

	// ----------------------------------------------------------------------

	// ---------------------------------------------------------------------

	/**
	 * Run different queries in two databases and compare the results
	 *
	 * @param con1
	 *          Connection to database1
	 * @param sql1
	 *          SQL query to run in database1
	 * @param con2
	 *          Connection to database2
	 * @param sql2
	 *          SQL query to run in database2
	 * @return true if both queries return the same rows.
	 */
	public boolean compareQueries(Connection con1, String sql1, Connection con2, String sql2) {
		boolean result = true;
		String dbName1 = (con1 == null) ? "no_database" : DBUtils.getShortDatabaseName(con1);
		String dbName2 = (con2 == null) ? "no_database" : DBUtils.getShortDatabaseName(con2);
		Map values1 = runQuery(con1, sql1);
		Map values2 = runQuery(con2, sql2);
		Iterator it1 = values1.keySet().iterator();
		while (it1.hasNext()) {
			String thisValue = (String) it1.next();
			if (values2.get(thisValue) == null) {
				result = false;
				ReportManager.problem(this, dbName1, thisValue + " is not in " + dbName2);
			}
		} // foreach it1

		Iterator it2 = values2.keySet().iterator();
		while (it2.hasNext()) {
			String thisValue = (String) it2.next();
			if (values1.get(thisValue) == null) {
				result = false;
				ReportManager.problem(this, dbName2, thisValue + " is not in " + dbName1);
			}
		} // foreach it1

		return result;
	}

	/**
	 * Run a query in a database and return the results as a HashMap where the keys are the rows (cols are concatenated with "::").
	 *
	 * @param con
	 *          Connection to database
	 * @param sql
	 *          SQL query to run in database
	 * @return Map where the keys are the rows (cols are concatenated with "::").
	 */
	private Map<String,String> runQuery(Connection con, String sql) {

		Map<String,String> values = new HashMap<String,String>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				StringBuffer buf = new StringBuffer(rs.getString(1));
				for (int a = 2; a <= rs.getMetaData().getColumnCount(); a++) {
					buf.append("::");
					buf.append(rs.getString(a));
				}
				values.put(buf.toString(), "1");
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;

	}

} // EnsTestCase
