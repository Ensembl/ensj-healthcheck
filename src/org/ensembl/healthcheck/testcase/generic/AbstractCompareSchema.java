package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultObjectRowMapper;
import org.ensembl.healthcheck.util.PoorLruMap;
import org.ensembl.healthcheck.util.RowMapper;

/**
 * A re-implementation of the {@link CompareSchema} health-check code but the
 * intention is to allow for post modifications in the various extension classes
 * to support all types of schema comparisons.
 * 
 * It has several ways of deciding which schema to use as the "master" to
 * compare all the others against:
 * <p>
 * <ol>
 * <li>If the property {@link #getDefinitionFileKey()} in database.properties
 * exists, the table.sql file it points to</li>
 * <li>If this is not present, the schema named by the property
 * {@link #getMasterSchemaKey()} is used</li>
 * <li>If neither of the above properties are present, an arbitrary first schema
 * is used as the master</li>
 * </ol>
 * 
 * @author ayates
 */
public abstract class AbstractCompareSchema extends MultiDatabaseTestCase {

	private static final int MAX_CACHE_SIZE = 2;

	/**
	 * An enum to contain the types of tests we allow a compare schema to perform.
	 * All should be self-explanatory.
	 * 
	 */
	public static enum TestTypes {
		IGNORE_AUTOINCREMENT_OPTION, AVG_ROW_LENGTH, MAX_ROWS, CHARSET, ENGINE
	};

	private Set<TestTypes> testTypes = new HashSet<TestTypes>();
	private Map<String, Set<String>> views = new PoorLruMap<String, Set<String>>(
	    MAX_CACHE_SIZE);
	private Map<String, Map<String, Set<Column>>> columns = new PoorLruMap<String, Map<String, Set<Column>>>(
	    MAX_CACHE_SIZE);
	private Map<String, String> createTables = new PoorLruMap<String, String>(
	    MAX_CACHE_SIZE);

	public AbstractCompareSchema() {
		addGroups();
		addDescription();
		addResponsible();
		addTestTypes();
	}

	protected void addDescription() {
		setDescription("Compare two databases (table names, column names " +
				"and types, and indexes. Note that, in the case of core " +
				"databases, there are occasionally tables (such as runnable, " +
				"job, job_status etc) that are still present after the " +
				"genebuild handover because pipelines are still running. The " +
				"genebuilders are responsible for deleting these before the release.");
	}

	protected abstract void addGroups();

	protected abstract void addResponsible();

	protected abstract void addTestTypes();

	public abstract void types();

	/**
	 * Should return the property key used to locate a target schema file
	 */
	protected abstract String getDefinitionFileKey();

	/**
	 * Should return the property key used to locate a target master schema
	 */
	protected abstract String getMasterSchemaKey();

	/**
	 * Returns the Set which controls the extra tests run
	 */
	public Set<TestTypes> getTestTypes() {
		return testTypes;
	}

	/**
	 * States if we want to run a paritcular type of test
	 */
	public boolean applyTest(TestTypes type) {
		return testTypes.contains(type);
	}

	/**
	 * Compare each database with the master.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;
		String testName = getClass().getSimpleName();

		Connection masterCon = null;

		String definitionFile = null;
		String masterSchema = null;

		String definitionFileKey = getDefinitionFileKey();
		String masterSchemaKey = getMasterSchemaKey();

		// Make sure that something is tested at some point. Don't allow a
		// database to weasel through this healthcheck without having been
		// checked.
		boolean somethingWasCompared = false;

		DatabaseRegistryEntry[] databases = dbr.getAll();

		definitionFile = System.getProperty(definitionFileKey);
		if (definitionFile == null) {
			logger
			    .info(testName
			        + ": No schema definition file found! Set "
			        + definitionFileKey
			        + " property in database.properties if you want to use a table.sql file or similar. This is not an error if you are using "
			        + masterSchemaKey);

			masterSchema = System.getProperty(masterSchemaKey);
			if (masterSchema != null) {
				// add the named master schema to the master registry so that it can be
				// accessed
				List<String> regexps = new ArrayList<String>();
				regexps.add(masterSchema);
				DatabaseRegistry masterDBR = new DatabaseRegistry(regexps, null, null,
				    false);
				DatabaseRegistryEntry masterDBRE = masterDBR
				    .getByExactName(masterSchema);
				if (masterDBRE == null) {
					logger.warning("Couldn't find database matching " + masterSchema);
				}
				dbr.add(masterDBRE);

				logger.info("Will use " + masterSchema
				    + " as specified master schema for comparisons.");

			}
			else {
				logger
				    .info(testName
				        + ": No master schema defined file found! Set "
				        + masterSchemaKey
				        + " property in database.properties if you want to use a master schema.");
			}
		}
		else {
			logger.fine("Will use schema definition from " + definitionFile);
		}

		try {

			if (definitionFile != null) {
				logger.info("About to import " + definitionFile);
				masterCon = importSchema(definitionFile);
				logger.info("Got connection to "
				    + DBUtils.getShortDatabaseName(masterCon));
			}
			else if (masterSchema != null) {
				masterCon = getSchemaConnection(masterSchema);
				logger.fine("Opened connection to master schema in "
				    + DBUtils.getShortDatabaseName(masterCon));
			}
			else {
				if (databases.length > 0) {
					masterCon = databases[0].getConnection();
					logger.info("Using " + DBUtils.getShortDatabaseName(masterCon)
					    + " as 'master' for comparisons.");
				}
				else {
					logger.warning("Can't find any databases to check against");
				}
			}

			for (DatabaseRegistryEntry dbre : databases) {
				DatabaseType type = dbre.getType();

				if (appliesToType(type)) {
					Connection checkCon = dbre.getConnection();
					if (checkCon != masterCon) {
						logger.info("Comparing " + DBUtils.getShortDatabaseName(checkCon)
						    + " with " + DBUtils.getShortDatabaseName(masterCon));
						// check that both schemas have the same tables
						somethingWasCompared = true;
						int directionFlag = EnsTestCase.COMPARE_BOTH;
						boolean ignoreBackupTables = false;
						if (type == DatabaseType.SANGER_VEGA) {
							directionFlag = EnsTestCase.COMPARE_RIGHT;
							ignoreBackupTables = true;
						}

						if (!compareTableEquality(masterCon, dbre, ignoreBackupTables,
						    directionFlag)) {
							// for sanger_vega, ignore backup tables. If not the same, this
							// method will generate a report
							ReportManager.problem(this, checkCon,
							    "Table name discrepancy detected, skipping rest of checks");
							result = false;
							continue;
						}

						for (String table : getTableNames(masterCon)) {
							result &= compareTable(masterCon, dbre, table);
						}
					} // if checkCon != masterCon

				} // if appliesToType

			} // for database

		}
		catch (SQLException e) {
			logger.severe(e.getMessage());
		}
		finally {
			// avoid leaving temporary DBs lying around if something bad happens
			if (definitionFile == null && masterCon != null) {
				// double-check to make sure the DB we're going to remove is a
				// temp one
				String dbName = DBUtils.getShortDatabaseName(masterCon);
				if (dbName.indexOf("_temp_") > -1) {
					removeDatabase(masterCon);
					logger.info("Removed " + DBUtils.getShortDatabaseName(masterCon));
				}
			}

		}

		if (!somethingWasCompared) {
			ReportManager
			    .problem(
			        this,
			        (Connection) null,
			        "No schema was compared. Please make sure you have configured this test correctly.");
			return false;
		}

		return result;
	}

	/**
	 * Currently delegates onto 
	 * {@link #compareTablesInSchema(Connection, Connection, boolean, int)} but
	 * can be over-ridden if required.
	 */
	protected boolean compareTableEquality(Connection masterConn,
	    DatabaseRegistryEntry target, boolean ignoreBackupTables,
	    int directionFlag) {
		Connection targetConn = target.getConnection();
		return compareTablesInSchema(targetConn, masterConn, ignoreBackupTables,
		    directionFlag);
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
	protected boolean compareTable(Connection master, DatabaseRegistryEntry targetDbre,
	    String table) throws SQLException {
		
		Connection target = targetDbre.getConnection();
		
		// - test show create table as it's the fastest ... apparently
		if (getCreateTable(master, table).equals(getCreateTable(target, table))) {
			return true;
		}

		boolean okay = true;
		String masterName = DBUtils.getShortDatabaseName(master);
		String targetName = DBUtils.getShortDatabaseName(target);

		// Compare table structure
		Set<Column> masterMinusTargetColumns = getColumns(master, table);
		masterMinusTargetColumns.removeAll(getColumns(target, table));
		Set<String> columnIssuesCalled = new HashSet<String>();
		// report that the target is missing columns defined in the master
		if (!masterMinusTargetColumns.isEmpty()) {
			for (Column col : masterMinusTargetColumns) {
				String message = String
				    .format(
				        "`%s` `%s` does not have the same definition as `%s`. Column `%s` was different. Check table structures",
				        targetName, table, masterName, col);
				ReportManager.problem(this, target, message);
				columnIssuesCalled.add(col.getName());
			}
			okay = false;
		}

		Set<Column> targetMinusMasterColumns = getColumns(target, table);
		Set<Column> localMaster = getColumns(master, table);
		localMaster.removeAll(columnIssuesCalled);
		targetMinusMasterColumns.removeAll(localMaster);
		// report that a target table columns which the master lacks
		if (!targetMinusMasterColumns.isEmpty()) {
			for (Column col : targetMinusMasterColumns) {
				if (columnIssuesCalled.contains(col.getName())) {
					continue;
				}
				String message = String
				    .format(
				        "`%s` `%s` does not have the same definition as `%s`. Column `%s` was different. Check table structures",
				        masterName, table, targetName, col);
				ReportManager.problem(this, master, message);
			}
			okay = false;
		}

		boolean masterView = getViews(master).contains(table);
		boolean targetView = getViews(target).contains(table);
		if (masterView != targetView) {
			String masterType = (masterView) ? "VIEW" : "TABLE";
			String targetType = (targetView) ? "VIEW" : "TABLE";
			String msg = String.format("`%s` is a %s in `%s` but a %s in `%s`",
			    table, masterType, masterName, targetType, targetName);
			ReportManager.problem(this, target, msg);
		}

		// Compare index structure if it wasn't a view
		if (!getViews(target).contains(table)) {
			Set<Index> masterIndexes = getIndexes(master, table);
			Set<Index> targetIndexes = getIndexes(target, table);

			Set<Index> masterMinusTargetIndexes = new HashSet<Index>(masterIndexes);
			masterMinusTargetIndexes.removeAll(targetIndexes);
			// report that target is missing indexes defined in master
			if (!masterMinusTargetIndexes.isEmpty()) {
				for (Index index : masterMinusTargetIndexes) {
					String message = String
					    .format(
					        "`%s` `%s` does not have the index `%s` which is present in `%s`. Check table structures",
					        targetName, table, index, masterName);
					ReportManager.problem(this, target, message);
				}
				okay = false;
			}

			Set<Index> targetMinusMasterIndexes = new HashSet<Index>(targetIndexes);
			targetMinusMasterIndexes.removeAll(masterIndexes);
			// report that target has indexes not defined in master
			if (!targetMinusMasterIndexes.isEmpty()) {
				for (Index index : targetMinusMasterIndexes) {
					String message = String
					    .format(
					        "`%s` `%s` does not have the index `%s` which is present in `%s`. Check table structures",
					        masterName, table, index, targetName);
					ReportManager.problem(this, master, message);
				}
				okay = false;
			}
		}

		// Compare avg_row_length
		if (applyTest(TestTypes.AVG_ROW_LENGTH)) {
			boolean result = regexCreateTable(master, target, table,
			    "AVG_ROW_LENGTH=(\\d+)", Integer.class, TestTypes.AVG_ROW_LENGTH);
			if (!result) {
				okay = false;
			}
		}

		// Compare max rows
		if (applyTest(TestTypes.MAX_ROWS)) {
			boolean result = regexCreateTable(master, target, table,
			    "MAX_ROWS=(\\d+)", Integer.class, TestTypes.MAX_ROWS);
			if (!result) {
				okay = false;
			}
		}

		// Compare charset
		if (applyTest(TestTypes.CHARSET)) {
			boolean result = regexCreateTable(master, target, table,
			    "DEFAULT CHARSET=([a-zA-Z0-9]+)", String.class, TestTypes.CHARSET);
			if (!result) {
				okay = false;
			}
		}

		// Compare engine
		if (applyTest(TestTypes.ENGINE)) {
			boolean result = regexCreateTable(master, target, table,
			    "ENGINE=([a-zA-Z0-9]+)", String.class, TestTypes.ENGINE);
			if (!result) {
				okay = false;
			}
		}

		return okay;
	}

	protected boolean regexCreateTable(Connection master, Connection target,
	    String table, String regex, Class<?> type, TestTypes testing)
	    throws SQLException {
		Pattern p = Pattern.compile(regex);
		Object masterValue = regex(p, getCreateTable(master, table), type);
		Object targetValue = regex(p, getCreateTable(target, table), type);
		if (masterValue.equals(targetValue)) {
			return true;
		}

		String message = String
		    .format(
		        "%s in `%s` had different values. `%s` contained '%s'. `%s` contained '%s'",
		        testing.toString(), table, DBUtils.getShortDatabaseName(master),
		        masterValue, DBUtils.getShortDatabaseName(target), targetValue);

		ReportManager.problem(this, target, message);

		return false;
	}

	protected Object regex(Pattern p, CharSequence target, Class<?> type) {
		final Object o;
		Matcher matcher = p.matcher(target);
		if (matcher.find()) {
			if (Integer.class.equals(type)) {
				o = Integer.valueOf(matcher.group(1));
			}
			else {
				o = matcher.group(1);
			}
		}
		else {
			o = "";
		}
		return o;
	}

	protected String getCreateTable(Connection conn, String table)
	    throws SQLException {
		String key = conn.getMetaData().getURL() + ":" + table;
		if (createTables.containsKey(key)) {
			return createTables.get(key);
		}
		String sql = "SHOW CREATE TABLE " + table;
		RowMapper<String> mapper = new DefaultObjectRowMapper<String>(String.class,
		    2);
		String createTable = new ConnectionBasedSqlTemplateImpl(conn)
		    .queryForObject(sql, mapper);
		if (applyTest(TestTypes.IGNORE_AUTOINCREMENT_OPTION)) {
			createTable = createTable.replaceFirst("AUTO_INCREMENT=\\d+\\s*", "");
		}
		createTables.put(key, createTable);
		return createTable;
	}

	/**
	 * Used to cache the current known set of views for each distinct JDBC URL
	 * retrieved from {@link Connection#getMetaData()}.
	 * 
	 * @param conn
	 *          Connection to query with
	 * @return {@link Set} of all views known of in the given connection
	 * @throws SQLException
	 *           Thrown if there is an issue with MetaData retrieval
	 */
	private Set<String> getViews(Connection conn) throws SQLException {
		String dbmsUrl = conn.getMetaData().getURL();
		if (!views.containsKey(dbmsUrl)) {
			List<String> dbViews = DBUtils.getViews(conn);
			views.put(dbmsUrl, new HashSet<String>(dbViews));
		}
		return views.get(dbmsUrl);
	}

	/**
	 * Returns a copy of the columns we currently hold for this table
	 */
	protected Set<Column> getColumns(Connection conn, String table)
	    throws SQLException {
		return new HashSet<Column>(getColumns(conn).get(table));
	}

	/**
	 * Used to cache the columns from a single schema.
	 * 
	 * @param conn
	 *          Connection to query with
	 * @return {@link Set} of all columns in a given connection keyed by the table
	 *         name
	 * @throws SQLException
	 *           Thrown if there is an issue with MetaData retrieval
	 */
	protected Map<String, Set<Column>> getColumns(Connection conn)
	    throws SQLException {
		String dbmsUrl = conn.getMetaData().getURL();
		if (!columns.containsKey(dbmsUrl)) {
			Map<String, Set<Column>> dbColumns = new LinkedHashMap<String, Set<Column>>();
			ResultSet rs = conn.getMetaData().getColumns(null,
			    DBUtils.getShortDatabaseName(conn), "%", "%");
			try {

				boolean processAutoIncrement = DBUtils.resultSetContainsColumn(rs,
				    "IS_AUTOINCREMENT");

				while (rs.next()) {
					String table = rs.getString("TABLE_NAME");
					// Get columns list
					Set<Column> tableColumns = dbColumns.get(table);
					if (tableColumns == null) {
						tableColumns = new LinkedHashSet<Column>();
						dbColumns.put(table, tableColumns);
					}

					boolean autoIncrement = (processAutoIncrement) ? rs
					    .getBoolean("IS_AUTOINCREMENT") : false;
					Column columnInstance = new Column(rs.getString("COLUMN_NAME"),
					    rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE"),
					    rs.getInt("DECIMAL_DIGITS"), rs.getBoolean("NULLABLE"),
					    rs.getString("COLUMN_DEF"), rs.getInt("CHAR_OCTET_LENGTH"),
					    autoIncrement);
					tableColumns.add(columnInstance);
				}
			}
			finally {
				DBUtils.closeQuietly(rs);
			}
			columns.put(dbmsUrl, dbColumns);
		}
		return columns.get(dbmsUrl);
	}

	/**
	 * Used to return all known indexed columns for a database
	 * 
	 * @param conn
	 *          Connection to query
	 * @param table
	 *          Table to query
	 * @return {@link Set} of index objects ordered by their discovery
	 * @throws SQLException
	 *           Thrown if there is a problem with MetaData
	 */
	protected Set<Index> getIndexes(Connection conn, String table)
	    throws SQLException {
		Map<String, Index> indexes = new HashMap<String, Index>();
		ResultSet rs = conn.getMetaData().getIndexInfo(null,
		    DBUtils.getShortDatabaseName(conn), table, false, false);
		try {
			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");
				Index index = indexes.get(indexName);
				if (index == null) {
					index = new Index(indexName, rs.getBoolean("NON_UNIQUE"),
					    rs.getInt("TYPE"));
					indexes.put(indexName, index);
				}
				index.addColumn(rs.getString("COLUMN_NAME"));
			}
		}
		finally {
			DBUtils.closeQuietly(rs);
		}

		return new LinkedHashSet<Index>(indexes.values());
	}

	/**
	 * Internal class used to represent a column
	 */
	private static class Column {

		private String name;
		private int dataType;
		private int columnSize;
		private int decimalDigits;
		private boolean nullable;
		private String columnDef;
		private int charOctetLength;
		private boolean autoIncrement;

		public Column(String name, int dataType, int columnSize, int decimalDigits,
		    boolean nullable, String columnDef, int charOctetLength,
		    boolean autoIncrement) {
			super();
			this.name = name;
			this.dataType = dataType;
			this.columnSize = columnSize;
			this.decimalDigits = decimalDigits;
			this.nullable = nullable;
			this.columnDef = columnDef;
			this.charOctetLength = charOctetLength;
			this.autoIncrement = autoIncrement;
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
			result = prime * result
			    + ((columnDef == null) ? 0 : columnDef.hashCode());
			result = prime * result + columnSize;
			result = prime * result + dataType;
			result = prime * result + decimalDigits;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (nullable ? 1231 : 1237);
			return result;
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
			if (columnDef == null) {
				if (other.columnDef != null)
					return false;
			}
			else if (!columnDef.equals(other.columnDef))
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
			}
			else if (!name.equals(other.name))
				return false;
			if (nullable != other.nullable)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Represents an Index with an equality and hashcode method which does not
	 * take into account name which is why a List would not suffice
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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((columns == null) ? 0 : columns.hashCode());
			result = prime * result + (nonUnique ? 1231 : 1237);
			result = prime * result + type;
			return result;
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
			}
			else if (!columns.equals(other.columns))
				return false;
			if (nonUnique != other.nonUnique)
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return name + "=[" + StringUtils.join(columns, ',') + "]";
		}
	}
}
