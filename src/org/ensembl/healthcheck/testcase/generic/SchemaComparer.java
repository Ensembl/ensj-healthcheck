/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import static org.ensembl.healthcheck.util.CollectionUtils.createArrayList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet; 
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultObjectRowMapper;
import org.ensembl.healthcheck.util.PoorLruMap;
import org.ensembl.healthcheck.util.RowMapper;

/**
 * Abstraction of code needed by compare schema HCs
 * 
 * @author ayates
 */
public class SchemaComparer {

	/**
	 * Internal class used to represent a column
	 */
	private static class Column {

		private String name;
		private int dataType;
		private int columnSize;
		private int decimalDigits;
		private boolean nullable;
		private String columnDefault;
		private int charOctetLength;
		private boolean autoIncrement;

		public Column(String name, int dataType, int columnSize, int decimalDigits, boolean nullable,
				String columnDefault, int charOctetLength, boolean autoIncrement) {
			super();
			this.name = name;
			this.dataType = dataType;
			this.columnSize = columnSize;
			this.decimalDigits = decimalDigits;
			this.nullable = nullable;
			this.columnDefault = columnDefault;
			this.charOctetLength = charOctetLength;
			this.autoIncrement = autoIncrement;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Column other = (Column) obj;
			if (autoIncrement != other.autoIncrement)
				return false;
			if (charOctetLength != other.charOctetLength)
				return false;
			if (columnDefault == null) {
				if (other.columnDefault != null)
					return false;
			} else if (!columnDefault.equals(other.columnDefault))
				return false;
			if (columnSize != other.columnSize)
				return false;
			if (dataType != other.dataType)
				return false;
			if (decimalDigits != other.decimalDigits)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (nullable != other.nullable)
				return false;
			return true;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (autoIncrement ? 1231 : 1237);
			result = prime * result + charOctetLength;
			result = prime * result + ((columnDefault == null) ? 0 : columnDefault.hashCode());
			result = prime * result + columnSize;
			result = prime * result + dataType;
			result = prime * result + decimalDigits;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (nullable ? 1231 : 1237);
			return result;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	/**
	 * Represents an Index with an equality and hashcode method which does not take
	 * into account name which is why a List would not suffice
	 */
	private static class Index {

		private String name;
		private List<String> columns = new ArrayList<String>();
		private boolean nonUnique;
		private int type;

		public Index(String name, boolean nonUnique, int type) {
			super();
			this.name = name;
			this.nonUnique = nonUnique;
			this.type = type;
		}

		public void addColumn(String col) {
			columns.add(col);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Index other = (Index) obj;
			if (columns == null) {
				if (other.columns != null)
					return false;
			} else if (!columns.equals(other.columns))
				return false;
			if (nonUnique != other.nonUnique)
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((columns == null) ? 0 : columns.hashCode());
			result = prime * result + (nonUnique ? 1231 : 1237);
			result = prime * result + type;
			return result;
		}

		@Override
		public String toString() {
			return name + "=[" + StringUtils.join(columns, ',') + "]";
		}
	}

	/**
	 * An enum to contain the types of tests we allow a compare schema to perform.
	 * All should be self-explanatory.
	 * 
	 */
	public static enum TestTypes {
		IGNORE_AUTOINCREMENT_OPTION, AVG_ROW_LENGTH, MAX_ROWS, CHARSET, ENGINE, CHECK_UNEQUAL, IGNORE_BACKUP
	}
	private static final int MAX_CACHE_SIZE = 3;
	/* comparison flags */
	private static final int COMPARE_LEFT = 0;

	private static final int COMPARE_RIGHT = 1;;

	private static final int COMPARE_BOTH = 2;

	protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private final Set<TestTypes> testTypes = new HashSet<>();
	
	private final Set<String> ignoreTables = new HashSet<>();

	private Set<String> requiredTables = new HashSet<>();

	public Set<String> getRequiredTables() {
		return requiredTables;
	}

	private Map<String, Set<String>> views = new PoorLruMap<String, Set<String>>(MAX_CACHE_SIZE);
	

	private Map<String, Map<String, Set<Column>>> columns = new PoorLruMap<String, Map<String, Set<Column>>>(
			MAX_CACHE_SIZE);
	private Map<String, String> createTables = new PoorLruMap<String, String>(MAX_CACHE_SIZE);
	private Map<String, Set<String>> tables = new PoorLruMap<String, Set<String>>(MAX_CACHE_SIZE);

	/**
	 * @param tables additional tables that may be in the schema but not the master
	 */
	public void addIgnoreTables(String... tables) {
		for (String table : tables) {
			ignoreTables.add(table);
		}
	}
	/**
	 * @param tables additional that must be present in the schema but not in the master
	 */
	public void addRequiredTables(String... tables) {
		for (String table : tables) {
			requiredTables.add(table);
		}
	}
	public void addTestTypes(TestTypes... types) {
		for (TestTypes type : types) {
			testTypes.add(type);
		}
	}

	private boolean applyTest(TestTypes testType) {
		return getTestTypes().contains(testType);
	}

	/**
	 * Compare each database with the master.
	 * 
	 * @param dbr
	 *            The database registry containing all the specified databases.
	 * @return true if the test passed.
	 */
	public boolean compare(EnsTestCase testcase, DatabaseRegistryEntry master, DatabaseRegistryEntry dbre) {

		boolean result = true;
		Connection masterCon = master.getConnection();
		Connection checkCon = dbre.getConnection();
		logger.info("Comparing " + master.getName() + " with " + dbre.getName());
		// check that both schemas have the same tables

		// method will generate a report
		try {
			if (!compareTableEquality(testcase, master, dbre, COMPARE_BOTH)) {
				result = false;

				if (getTestTypes().contains(TestTypes.CHECK_UNEQUAL)) {
					String msg;
					if (searchForTemporaryTables(checkCon)) {
						msg = String.format("Table name discrepancy detected but temporary tables "
								+ "were found in the schema '%s'. Try running "
								+ "ensembl/misc-scripts/db/cleanup_tmp_tables.pl", dbre.getName());
					} else {
						msg = "Table name discrepancy detected, skipping rest of checks";
					}
					ReportManager.problem(testcase, checkCon, msg);
				} else {
					ReportManager.problem(testcase, checkCon,
							"Table name discrepancy detected but continuing with table checks");
				}
			}

			for (String table : DBUtils.getTableNames(masterCon)) {
				if(!getIgnoreTables().contains(table)) {
					result &= compareTable(testcase, master, dbre, table);
				}
			}
		} catch (SQLException e) {
			logger.severe(e.getMessage());
		}

		return result;
	}

	/**
	 * Performs tests on the equivalent sets of columns in the given table, the
	 * indexes on the given table, the type of the table (view or not) as well as
	 * other creation parameters e.g. <em>AVG_ROW_LENGTH</em>. Many of these are
	 * controlled by the {@link TestTypes} enum.
	 * 
	 * To help with speed we first compare a <em>SHOW CREATE TABLE</em> with some
	 * post modification.
	 */
	protected boolean compareTable(EnsTestCase test, DatabaseRegistryEntry master, DatabaseRegistryEntry targetDbre,
			String table) throws SQLException {

		String masterName = master.getName();
		String targetName = targetDbre.getName();
		logger.info("Comparing "+table+" between "+masterName+" and "+targetName);
		Connection target = targetDbre.getConnection();
		Connection masterCon = master.getConnection();


		// If either schema did not contain this table then just return early
		// because we will have warned about it earlier on. This could only happen
		// if the skipCheckingIfTablesAreUnequal() method was returning false
		if (!getTables(masterCon).contains(table)) { 
			ReportManager.problem(test, target, "Table "+table+" not found in "+masterName);
			return false;			
		}
		if (!getTables(target).contains(table)) { 
			ReportManager.problem(test, target, "Table "+table+" not found in "+targetName);
			return false;			
		}

		// - test show create table as it's the fastest ... apparently
		if (getCreateTable(masterCon, table).equals(getCreateTable(target, table))) {
			logger.info("Table "+table+" identical between schemata");
			return true;
		}

		boolean okay = true;
		// Compare table structure
		Set<Column> masterMinusTargetColumns = getColumns(masterCon, table);
		masterMinusTargetColumns.removeAll(getColumns(target, table));
		Set<Column> columnIssuesCalled = new HashSet<Column>();
		// report that the target is missing columns deinfod in the master
		if (!masterMinusTargetColumns.isEmpty()) {
			for (Column col : masterMinusTargetColumns) {
				String message = String.format(
						"`%s` `%s` does not have the same definition as `%s`. Column `%s` was different. Check table structures",
						targetName, table, masterName, col);
				ReportManager.problem(test, target, message);
				columnIssuesCalled.add(col);
			}
			okay = false;
		}

		Set<Column> targetMinusMasterColumns = getColumns(target, table);
		Set<Column> localMaster = getColumns(masterCon, table);
		localMaster.removeAll(columnIssuesCalled);
		targetMinusMasterColumns.removeAll(localMaster);
		// report that a target table columns which the master lacks
		if (!targetMinusMasterColumns.isEmpty()) {
			for (Column col : targetMinusMasterColumns) {
				if (columnIssuesCalled.contains(col)) {
					continue;
				}
				String message = String.format(
						"`%s` `%s` does not have the same definition as `%s`. Column `%s` was different. Check table structures",
						masterCon, table, targetName, col);
				ReportManager.problem(test, targetDbre.getConnection(), message);
			}
			okay = false;
		}

		boolean masterView = getViews(masterCon).contains(table);
		boolean targetView = getViews(target).contains(table);
		if (masterView != targetView) {
			String masterType = (masterView) ? "VIEW" : "TABLE";
			String targetType = (targetView) ? "VIEW" : "TABLE";
			String msg = String.format("`%s` is a %s in `%s` but a %s in `%s`", table, masterType, masterName,
					targetType, targetName);
			ReportManager.problem(test, target, msg);
			okay = false;
		}

		// Compare index structure if it wasn't a view
		if (!getViews(target).contains(table)) {
			Set<Index> masterIndexes = getIndexes(masterCon, table);
			Set<Index> targetIndexes = getIndexes(target, table);

			Set<Index> masterMinusTargetIndexes = new HashSet<Index>(masterIndexes);
			masterMinusTargetIndexes.removeAll(targetIndexes);
			// report that target is missing indexes deinfod in master
			if (!masterMinusTargetIndexes.isEmpty()) {
				for (Index index : masterMinusTargetIndexes) {
					String message = String.format(
							"`%s` `%s` does not have the index `%s` which is present in `%s`. Check table structures",
							targetName, table, index, masterName);
					ReportManager.problem(test, target, message);
				}
				okay = false;
			}

			Set<Index> targetMinusMasterIndexes = new HashSet<Index>(targetIndexes);
			targetMinusMasterIndexes.removeAll(masterIndexes);
			// report that target has indexes not deinfod in master
			if (!targetMinusMasterIndexes.isEmpty()) {
				for (Index index : targetMinusMasterIndexes) {
					String message = String.format(
							"`%s` `%s` does not have the index `%s` which is present in `%s`. Check table structures",
							targetDbre.getName(), table, index, targetName);
					ReportManager.problem(test, targetDbre.getConnection(), message);
				}
				okay = false;
			}
		}

		// Compare avg_row_length
		if (applyTest(TestTypes.AVG_ROW_LENGTH)) {
			boolean result = regexCreateTable(test, master, targetDbre, table, "AVG_ROW_LENGTH=(\\d+)", Integer.class,
					TestTypes.AVG_ROW_LENGTH);
			if (!result) {
				okay = false;
			}
		}

		// Compare max rows
		if (applyTest(TestTypes.MAX_ROWS)) {
			boolean result = regexCreateTable(test, master, targetDbre, table, "MAX_ROWS=(\\d+)", Integer.class,
					TestTypes.MAX_ROWS);
			if (!result) {
				okay = false;
			}
		}

		// Compare charset
		if (applyTest(TestTypes.CHARSET)) {
			boolean result = regexCreateTable(test, master, targetDbre, table, "DEFAULT CHARSET=([a-zA-Z0-9]+)",
					String.class, TestTypes.CHARSET);
			if (!result) {
				okay = false;
			}
		}

		// Compare engine
		if (applyTest(TestTypes.ENGINE)) {
			boolean result = regexCreateTable(test, master, targetDbre, table, "ENGINE=([a-zA-Z0-9]+)", String.class,
					TestTypes.ENGINE);
			if (!result) {
				okay = false;
			}
		}

		return okay;
	}

	/**
	 * Currently delegates onto
	 * {@link #compareTablesInSchema(Connection, Connection, boolean, int)} but can
	 * be over-ridden if required.
	 */
	protected boolean compareTableEquality(EnsTestCase testcase, DatabaseRegistryEntry master,
			DatabaseRegistryEntry target, int directionFlag) {
		return compareTablesInSchema(testcase, target, master, directionFlag);
	}

	/**
	 * Compare two schemas to see if they have the same tables. The comparison can
	 * be done in in one direction or both directions.
	 * 
	 * @param schema1
	 *            The first schema to compare.
	 * @param schema2
	 *            The second schema to compare.
	 * @param directionFlag
	 *            The direction to perform comparison in, either
	 *            EnsTestCase.COMPARE_RIGHT, EnsTestCase.COMPARE_LEFT or
	 *            EnsTestCase.COMPARE_BOTH,
	 * @return for left comparison: all tables in schema1 exist in schema2 for right
	 *         comparison: all tables in schema1 exist in schema2 for both: if all
	 *         tables in schema1 exist in schema2, and vice-versa
	 */
	public boolean compareTablesInSchema(EnsTestCase testcase, DatabaseRegistryEntry schema1,
			DatabaseRegistryEntry schema2, int directionFlag) {

		boolean result = true;
		if (directionFlag == COMPARE_RIGHT || directionFlag == COMPARE_BOTH) {

			// perform right compare if required
			//
			result = compareTablesInSchema(testcase, schema2, schema1, COMPARE_LEFT);
		}

		if (directionFlag == COMPARE_LEFT || directionFlag == COMPARE_BOTH) {

			Connection reportConnection = (directionFlag == COMPARE_LEFT) ? schema2.getConnection()
					: schema1.getConnection();

			// check each table in turn
			for(String table: DBUtils.getTableNames(schema1.getConnection())) {
				if (!getIgnoreTables().contains(table) && !DBUtils.checkTableExists(schema2.getConnection(), table)) {
					ReportManager.problem(testcase, reportConnection,
							"Table " + table + " exists in " + schema1.getName() + " but not in " + schema2.getName());
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * Used to cache the columns from a single schema.
	 * 
	 * @param conn
	 *            Connection to query with
	 * @return {@link Set} of all columns in a given connection keyed by the table
	 *         name
	 * @throws SQLException
	 *             Thrown if there is an issue with MetaData retrieval
	 */
	protected Map<String, Set<Column>> getColumns(Connection conn) throws SQLException {
		String dbmsUrl = conn.getMetaData().getURL();
		if (!columns.containsKey(dbmsUrl)) {
			Map<String, Set<Column>> dbColumns = new LinkedHashMap<String, Set<Column>>();
			ResultSet rs = conn.getMetaData().getColumns(null, DBUtils.getShortDatabaseName(conn), "%", "%");
			try {

				boolean processAutoIncrement = DBUtils.resultSetContainsColumn(rs, "IS_AUTOINCREMENT");

				while (rs.next()) {
					String table = rs.getString("TABLE_NAME");
					// Get columns list
					Set<Column> tableColumns = dbColumns.get(table);
					if (tableColumns == null) {
						tableColumns = new LinkedHashSet<Column>();
						dbColumns.put(table, tableColumns);
					}

					boolean autoIncrement = (processAutoIncrement) ? rs.getBoolean("IS_AUTOINCREMENT") : false;
					Column columnInstance = new Column(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"),
							rs.getInt("COLUMN_SIZE"), rs.getInt("DECIMAL_DIGITS"), rs.getBoolean("NULLABLE"),
							rs.getString("COLUMN_DEF"), rs.getInt("CHAR_OCTET_LENGTH"), autoIncrement);
					tableColumns.add(columnInstance);
				}
			} finally {
				DBUtils.closeQuietly(rs);
			}
			columns.put(dbmsUrl, dbColumns);
		}
		return columns.get(dbmsUrl);
	}

	/**
	 * Returns a copy of the columns we currently hold for this table
	 */
	protected Set<Column> getColumns(Connection conn, String table) throws SQLException {
		return new HashSet<Column>(getColumns(conn).get(table));
	}

	protected String getCreateTable(Connection conn, String table) throws SQLException {
		String key = conn.getMetaData().getURL() + ":" + table;
		if (createTables.containsKey(key)) {
			return createTables.get(key);
		}
		String sql = "SHOW CREATE TABLE " + table;
		RowMapper<String> mapper = new DefaultObjectRowMapper<String>(String.class, 2);
		String createTable = new ConnectionBasedSqlTemplateImpl(conn).queryForObject(sql, mapper);
		if (applyTest(TestTypes.IGNORE_AUTOINCREMENT_OPTION)) {
			createTable = createTable.replaceFirst("AUTO_INCREMENT=\\d+\\s*", "");
		}
		createTables.put(key, createTable);
		return createTable;
	}

	public Set<String> getIgnoreTables() {
		return ignoreTables;
	}

	/**
	 * Used to return all known indexed columns for a database
	 * 
	 * @param conn
	 *            Connection to query
	 * @param table
	 *            Table to query
	 * @return {@link Set} of index objects ordered by their discovery
	 * @throws SQLException
	 *             Thrown if there is a problem with MetaData
	 */
	protected Set<Index> getIndexes(Connection conn, String table) throws SQLException {
		Map<String, Index> indexes = new HashMap<String, Index>();
		ResultSet rs = conn.getMetaData().getIndexInfo(null, DBUtils.getShortDatabaseName(conn), table, false, false);
		try {
			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");
				Index index = indexes.get(indexName);
				if (index == null) {
					index = new Index(indexName, rs.getBoolean("NON_UNIQUE"), rs.getInt("TYPE"));
					indexes.put(indexName, index);
				}
				index.addColumn(rs.getString("COLUMN_NAME"));
			}
		} finally {
			DBUtils.closeQuietly(rs);
		}

		return new LinkedHashSet<Index>(indexes.values());
	}

	/**
	 * Returns a locally cached Set of table names in the given schema
	 */
	protected Set<String> getTables(Connection conn) throws SQLException {
		String url = conn.getMetaData().getURL();
		if (!tables.containsKey(url)) {
			String[] array = DBUtils.getTableNames(conn);
			tables.put(url, new HashSet<String>(Arrays.asList(array)));
		}
		return tables.get(url);
	}

	public Set<TestTypes> getTestTypes() {
		return testTypes;
	}

	/**
	 * Used to cache the current known set of views for each distinct JDBC URL
	 * retrieved from {@link Connection#getMetaData()}.
	 * 
	 * @param conn
	 *            Connection to query with
	 * @return {@link Set} of all views known of in the given connection
	 * @throws SQLException
	 *             Thrown if there is an issue with MetaData retrieval
	 */
	private Set<String> getViews(Connection conn) throws SQLException {
		String dbmsUrl = conn.getMetaData().getURL();
		if (!views.containsKey(dbmsUrl)) {
			List<String> dbViews = DBUtils.getViews(conn);
			views.put(dbmsUrl, new HashSet<String>(dbViews));
		}
		return views.get(dbmsUrl);
	}

	protected Object regex(Pattern p, CharSequence target, Class<?> type) {
		final Object o;
		Matcher matcher = p.matcher(target);
		if (matcher.find()) {
			if (Integer.class.equals(type)) {
				o = Integer.valueOf(matcher.group(1));
			} else {
				o = matcher.group(1);
			}
		} else {
			o = StringUtils.EMPTY;
		}
		return o;
	}

	protected boolean regexCreateTable(EnsTestCase test, DatabaseRegistryEntry master, DatabaseRegistryEntry target,
			String table, String regex, Class<?> type, TestTypes testing) throws SQLException {
		Pattern p = Pattern.compile(regex);
		Object masterValue = regex(p, getCreateTable(master.getConnection(), table), type);
		Object targetValue = regex(p, getCreateTable(target.getConnection(), table), type);
		if (masterValue.equals(targetValue)) {
			return true;
		}

		String message = String.format("%s in `%s` had different values. `%s` contained '%s'. `%s` contained '%s'",
				testing.toString(), table, master.getName(), masterValue, target.getName(), targetValue);

		ReportManager.problem(test, target.getConnection(), message);

		return false;
	}

	private boolean searchForTemporaryTables(Connection conn) throws SQLException {
		boolean temporaryTables = false;
		Set<String> tables = getTables(conn);
		List<String> searchValues = createArrayList("MTMP_", "tmp", "temp", "bak", "backup");
		for (String table : tables) {
			for (String search : searchValues) {
				if (table.contains(search)) {
					temporaryTables = true;
					break;
				}
			}
		}
		return temporaryTables;
	}

}
