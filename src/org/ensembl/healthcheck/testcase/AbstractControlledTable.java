package org.ensembl.healthcheck.testcase;

import java.sql.Connection;

import org.ensembl.healthcheck.util.ChecksumDatabase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlTemplate.ResultSetCallback;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;

/**
 * <p>
 * 	Base class for comparing a table of a database to a reference table in a 
 * production database.
 * </p>
 * 
 * <p>
 * 	Use this by subclassing and overriding the following methods: 		
 * </p>
 * 
 * <ul>
 * 	<li>
 * 		{@link AbstractControlledTable#getControlledTableName}
 * 	</li>
 * 	<li>
 * 		{@link AbstractControlledTable#getComparisonStrategy} (optional)
 * 	</li>
 * 	<li>
 * 		{@link AbstractControlledTable#getMaxReportedMismatches} (optional)
 * 	</li>
 * </ul>
 * 
 * @author mnuhn
 *
 */
public abstract class AbstractControlledTable extends AbstractTemplatedTestCase {
	
	/**
	 * The name of the table that will be compared to the master. 
	 */
	protected abstract String getControlledTableName();

	/**
	 * How the tables should be compared.
	 * 
	 * @return {@link ComparisonStrategy}
	 */
	protected ComparisonStrategy getComparisonStrategy() {
		return ComparisonStrategy.RowByRow;
	}
	
	/**
	 * Enum of strategy names of how to compare two tables.
	 */
	protected enum ComparisonStrategy { 
		/**
		 * Compares two tables row by row. Rows are fetched iteratively in 
		 * batches of size {@link AbstractControlledTable#batchSize}
		 */
		RowByRow, 

		/**
		 * Compares two tables using checksums.
		 */
		Checksum 
	};

	/**
	 * If the {@link ComparisonStrategy} {@link ComparisonStrategy#RowByRow} 
	 * is used, we want to make sure not to report excessive an amount of 
	 * errors.
	 * 
	 * The maximum amount of mismatches that this test is allowed to report
	 * when using ComparisonStrategy.RowByRow.
	 * 
	 */
	protected int getMaxReportedMismatches() {
		return 50;
	}
	
	/**
	 * Number of rows that have been reported by this test, if 
	 * {@link ComparisonStrategy#RowByRow} is being used. If numReportedRows 
	 * exceeds getMaxReportedMismatches(), the test will terminate.
	 * 
	 */
	private int numReportedRows;

	/**
	 * Maximum number of rows to be fetched in one iteration;
	 */
	protected final int batchSize = 1000;
	
	/**
	 * DatabaseRegistryEntry of the master database.
	 */
	protected DatabaseRegistryEntry getMasterDatabase() {
		return getComparaMasterDatabase();
	}

	public AbstractControlledTable() {
		setTypeFromPackageName();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		String controlledTableToTest = getControlledTableName();
		
		DatabaseRegistryEntry masterDbRe = getMasterDatabase();
		Connection testDbConn = dbre.getConnection();
		
		if (masterDbRe==null) {
			ReportManager.problem(
				this, 
				testDbConn, 
				"Can't get connection to master database! Perhaps it has not been "
				+"configured?"
			);
			return false;
		}
		
		boolean passed;
		
		if (getComparisonStrategy() == ComparisonStrategy.RowByRow) {
			
			getLogger().log(Level.INFO, "Checking row by row");
			
			numReportedRows = 0;
			
			passed = checkAllRowsInTable(
					controlledTableToTest, 
					dbre,
					masterDbRe				
			);
			
		} else {
			
			getLogger().log(Level.INFO, "Checking by using checksums");
			
			passed = checkByChecksum(
					controlledTableToTest,
					dbre,
					masterDbRe				
			);
			
			if (!passed) {
				ReportManager.problem(
					this, 
					dbre.getConnection(), 
					"The table " + controlledTableToTest + " differs from the one in the master database. This was established by using checksums so the rows in question are not shown."
				);
			}			
		}		
		return passed;
	}
	
	/** 
	 * Checks whether a table that exists in two databases has the same 
	 * content. This is done using checksums. 
	 */
	protected boolean checkByChecksum(
			final String controlledTableToTest,
			DatabaseRegistryEntry testDbRe,
			DatabaseRegistryEntry masterDbRe
		) {
		
		List<String> tablesToChecksum = new ArrayList<String>();
		tablesToChecksum.add(controlledTableToTest);
		
		String checksumValueMaster = calculateChecksumForTable(
				masterDbRe, tablesToChecksum);
		
		String checksumValueTest = calculateChecksumForTable(
				testDbRe, tablesToChecksum);
		
		return checksumValueMaster.equals(checksumValueTest);
	}

	/**
	 * 
	 * Calculates the checksum of a list of tables in a given database.
	 * 
	 */
	protected String calculateChecksumForTable(
			DatabaseRegistryEntry dbre, 
			List<String> tablesToChecksum
		) {
		ChecksumDatabase cd = new ChecksumDatabase(dbre, tablesToChecksum);
		
		// Should be something like this:
		// {ensembl_compara_master.dnafrag=635082403}
		//
		Properties checksumMaster = cd.getChecksumFromDatabase();
		
		Set<String> entrySet = checksumMaster.stringPropertyNames();
		if (entrySet.size()!=1) {
			throw new RuntimeException("Unexpected result from checksumming (expected only one element): ");
		}
		// Will be prefixed with the database name like: 
		// "ensembl_compara_master.dnafrag"
		//
		String tableName = entrySet.iterator().next();
		
		String checksumValueMaster = (String) checksumMaster.get(tableName);
		return checksumValueMaster;
	}

	/**
	 * For every row of the table controlledTableToTest in the database 
	 * testDbre this checks, if this row also exists in the table 
	 * controlledTableToTest of masterDbRe.
	 * 
	 * @param controlledTableToTest
	 * @param testDbre
	 * @param masterDbRe
	 * @return
	 */
	protected boolean checkAllRowsInTable(
			final String controlledTableToTest,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry masterDbRe
		) {
		return checkAllRowsInTable(controlledTableToTest, controlledTableToTest, testDbre, masterDbRe);		
	}
	
	/**
	 * For every row of the table controlledTableToTest in the database 
	 * testDbre this checks, if this row also exists in the table 
	 * masterTable of masterDbRe.
	 * 
	 * @param controlledTableToTest
	 * @param masterTable
	 * @param testDbre
	 * @param masterDbRe
	 * @return
	 */
	protected boolean checkAllRowsInTable(
			final String controlledTableToTest,
			final String masterTable,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry masterDbRe
		) {
		
		final Logger logger = getLogger();
		
		final Connection testDbConn = testDbre.getConnection();
		final Connection masterconn = masterDbRe.getConnection();

		final SqlTemplate sqlTemplateTestDb        = getSqlTemplate(testDbConn);  
		
		int rowCount = sqlTemplateTestDb.queryForDefaultObject(
			"select count(*) from dnafrag",
			Integer.class
		);
		
		logger.info("Number of rows in table: " + rowCount);
		
		final List<String> testTableColumns = getColumnsOfTable(testDbConn, controlledTableToTest);		
		final List<String> masterColumns    = getColumnsOfTable(masterconn, controlledTableToTest);		
		
		boolean masterHasAllNecessaryColumns = columnsAreSubset(
				testDbConn,
				masterconn,
				controlledTableToTest
		);		
		
		logger.log(Level.INFO, "Checking if columns are compatible");
		
		if (!masterHasAllNecessaryColumns) {
			
			testTableColumns.removeAll(masterColumns);			
			ReportManager.problem(
				this, 
				testDbConn, 
				"The following columns are not present in the master database: "
				+ testTableColumns + "\n"
				+ "The schemas are not compatible.\n"
			);
			return false;
		} else {
			logger.log(Level.INFO, "Columns are ok.");
		}
		
		int limit = batchSize;
		boolean allRowsInMaster = true;
		
		for(int currentOffset = 0; currentOffset<rowCount && !numReportedRowsExceedsMaximum(); currentOffset+=limit) {
			
			logger.info("Checking rows " + currentOffset + " out of " + rowCount);
			
			allRowsInMaster &= checkRangeOfRowsInTable(
				controlledTableToTest,
				masterTable,
				testDbre,
				masterDbRe,
				limit,
				currentOffset
			);			
		}
		return allRowsInMaster;
	}
	
	protected boolean columnsAreSubset(
			final Connection testDbConn,
			final Connection masterconn,
			final String controlledTableToTest
		) {
		
		final List<String> testTableColumns = getColumnsOfTable(testDbConn, controlledTableToTest);		
		final List<String> masterColumns    = getColumnsOfTable(masterconn, controlledTableToTest);
		
		boolean masterHasAllNecessaryColumns = masterColumns.containsAll(testTableColumns);

		return masterHasAllNecessaryColumns;
	}
	
	private boolean numReportedRowsExceedsMaximum() {
		return numReportedRows>getMaxReportedMismatches();
	}
	
	/**
	 * For every row of the table controlledTableToTest in the database 
	 * testDbre this checks, if this row also exists in the table 
	 * masterTable of masterDbRe.
	 * 
	 */
	protected boolean checkRangeOfRowsInTable(
			final String controlledTableToTest,
			final String masterTable,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry masterDbRe,
			int limit,
			int offset
		) {

		final Connection testDbConn = testDbre.getConnection();
		final Connection masterconn = masterDbRe.getConnection();
		
		final SqlTemplate sqlTemplateTestDb        = getSqlTemplate(testDbConn);  
		final SqlTemplate sqlTemplateComparaMaster = getSqlTemplate(masterconn);
		
		String fetchAllRowsFromTableSql = generateFetchAllRowsFromTableSql(testDbConn, controlledTableToTest, limit, offset);

		final List<String> testTableColumns = getColumnsOfTable(testDbConn, controlledTableToTest);		

		final EnsTestCase thisTest = this;
		
		boolean result = sqlTemplateTestDb.execute(
			fetchAllRowsFromTableSql,
			new ResultSetCallback<Boolean>() {

				@Override public Boolean process(ResultSet rs) throws SQLException {
					
					rs.setFetchSize(batchSize);					
							
					boolean allRowsPresentInMasterDb = true;				
					
					while (rs.next() && !numReportedRowsExceedsMaximum()) {
						
						boolean currentRowPresentInMasterDb = isCurrentRowInMaster(
							rs,
							sqlTemplateComparaMaster, 
							masterTable,
							testTableColumns 
						);
						
						allRowsPresentInMasterDb &= currentRowPresentInMasterDb;
						
						if (!currentRowPresentInMasterDb) {
							
							numReportedRows++;
							
							if (numReportedRowsExceedsMaximum()) {
								ReportManager.problem(
										thisTest, 
										testDbConn, 
										"The maximum of " + getMaxReportedMismatches() + " reported rows has been reached, no further rows will be tested."
								);
							} else {							
								ReportManager.problem(
									thisTest, 
									testDbConn, 
									"Row not found in master: " + resultSetRowAsString(rs)
								);
							}
						}
					}					
					return allRowsPresentInMasterDb;
				}
			},
			// No bound parameters
			//
			new Object[0]
		);
		return result;
	}
	
	/**
	 * 
	 * Will check, if the current for of the ResultSet is present in the master database.
	 * 
	 * The columns are passed in each time so this doesn't have to be generated for each
	 * call.
	 * 
	 * @param masterTableName
	 * @param sqlTemplateComparaMaster
	 * @param columns
	 * @param rsFromTestDb
	 * @throws SQLException
	 */
	protected boolean isCurrentRowInMaster(
			final ResultSet rsFromTestDb,
			final SqlTemplate sqlTemplateComparaMaster,
			final String masterTableName,
			final List<String> columns 
	) throws SQLException {
		
		int numColumns = rsFromTestDb.getMetaData().getColumnCount();
		List<Object> columnValuesObjects = new ArrayList<Object>(numColumns);						

		for(int currentColIndex=0; currentColIndex<numColumns; currentColIndex++) {
			
			Object value = rsFromTestDb.getObject(currentColIndex+1);
			columnValuesObjects.add(currentColIndex, value);						
		}
		
		String countMatchingRowsSql = "select count(*) from " + masterTableName + " where " + asParameterisedWhereClause(columns, columnValuesObjects);
		
		final EnsTestCase thisTest = this;
		
		boolean isInMasterDb = sqlTemplateComparaMaster.execute(
			countMatchingRowsSql, 
			new ResultSetCallback<Boolean>() {

				@Override public Boolean process(ResultSet rsFromMaster) throws SQLException {
					
					int numColumns = rsFromMaster.getMetaData().getColumnCount();
					
					if (numColumns!=1) {
						throw new RuntimeException(
							"Expected one column, but got " + numColumns + 
							" instead!"	+ resultSetRowAsString(rsFromMaster)
						);
					}
					
					rsFromMaster.next();
					
					int numberOfMatchingRowsInMaster = rsFromMaster.getInt(1);
					
					if (numberOfMatchingRowsInMaster==1) {
						return true;
					}
					if (numberOfMatchingRowsInMaster==0) {
						return false;
					}
					
					ReportManager.problem(thisTest, rsFromMaster.getStatement().getConnection(), 
						"Found " + numberOfMatchingRowsInMaster + " "
						+ "matching rows in the master database!\n"
						+ "The row searched for was:\n"
						+ resultSetRowAsString(rsFromTestDb)
					);
					
					// We return true, because there is a row in the master 
					// database. The tested database has passed for this row,
					// it is the master database that has the problem.
					//
					return true;
				}
			},
			columnValuesObjects.toArray()
		);
		return isInMasterDb;
	}
	
	/**
	 * 
	 * For the given ResultSet object this will return a stringified version 
	 * of the current row. Useful to print in error or debug messages.
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String resultSetRowAsString(ResultSet rs)
			throws SQLException {
		int numColumns = rs.getMetaData().getColumnCount();
		List<String> columnValuesStringy = new ArrayList<String>(numColumns);
		for(int currentColIndex=0; currentColIndex<numColumns; currentColIndex++) {
			
			Object value = rs.getObject(currentColIndex+1);
			String convertedValue;
			if (value==null) {
				convertedValue = "<null>";
			} else {
				convertedValue = value.toString();
			}
			columnValuesStringy.add(currentColIndex, convertedValue);							
		}
		return asCommaSeparatedString(columnValuesStringy);
	}

	/**
	 * 
	 * Generates a sql statement that will fetch the given columns of all rows
	 * of the table.
	 * 
	 * @param conn
	 * @param tableName
	 * @param columns
	 * @return
	 */
	protected String fetchAllRowsFromTableSql(
			Connection conn, 
			String tableName, 
			List<String> columns,
			int limit,
			int offset

		) {		
		return "select " + asCommaSeparatedString(columns) + " from " + tableName + " limit " + limit + " offset " + offset;			
	}
	
	/**
	 * 
	 * Generates a sql statement that will fetch all columns of all rows from
	 * the given table.
	 * 
	 * @param conn
	 * @param tableName
	 * @return
	 */
	protected String generateFetchAllRowsFromTableSql(
			Connection conn, 
			String tableName,
			int limit,
			int offset
		) {
		
		List<String> columns = getColumnsOfTable(conn, tableName);			
		String sql = fetchAllRowsFromTableSql(conn, tableName, columns, limit, offset);
			
		return sql;
	}
	
	/**
	 * 
	 * Creates a where clause for a sql statement of the form column_1=? and 
	 * column_2=? ... column_n=?. The listOfValues parameter is used to 
	 * determine whether a value will be compared with "=" or with "is". By
	 * default "=" is used, but "is" will be used for null values like 
	 * "... and column_i is null".  
	 * 
	 * @param listOfColumns
	 * @param listOfValues
	 * @return
	 */
	protected String asParameterisedWhereClause(List<String> listOfColumns, List<Object> listOfValues) {
		
		int numColumns = listOfColumns.size();
		int numValues  = listOfValues.size();
		
		if (numColumns != numValues) {
			throw new IllegalArgumentException(
				"listOfColumns ("+listOfColumns.size()+") does not have the "
				+"same size as listOfValues ("+listOfValues.size()+")!"
			);
		}		
		
		StringBuffer whereClause = new StringBuffer();
		for(int i=0; i<numColumns; i++) {
			
			// Join the individual conditions with "and", but don't start the
			// where clause with an "and".
			//
			String joiner;
			if (i==0) {
				joiner="";
			} else {
				joiner=" and ";
			}
			
			// Tests for null values have to be done with "is" and not with
			// "=?". The latter would always evaluate to false.
			//
			if (listOfValues.get(i) == null) {
				whereClause.append(joiner + listOfColumns.get(i) + " is ?");
			} else {
				whereClause.append(joiner + listOfColumns.get(i) + "=?");
			}			
		}
		return whereClause.toString();
	}
	
	/**
	 * Joins the list of strings into one comma (and space) separated string.
	 * 
	 * @param listOfStrings
	 * @return
	 */
	protected String asCommaSeparatedString(List<String> listOfStrings) {		
		return joinListOfStrings(listOfStrings, ", ");
	}
	
	/**
	 * 
	 * Joins a list of strings with a separator.
	 * 
	 * @param listOfStrings
	 * @param separator
	 * @return
	 */
	protected String joinListOfStrings(List<String> listOfStrings, String separator) {
		
		int numStrings = listOfStrings.size();
		
		StringBuffer commaSeparated = new StringBuffer();			
		
		commaSeparated.append(listOfStrings.get(0));
		for(int i=1; i<numStrings; i++) {
			commaSeparated.append(separator + listOfStrings.get(i));
		}
		return commaSeparated.toString();

	}
	
	/**
	 * 
	 * Returns the names of all tables in the database.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	protected List<String> getTablesOfDb(Connection conn) throws SQLException {
		
		DatabaseMetaData md = conn.getMetaData();
		
		List<String> tablesOfDb = new ArrayList<String>(); 
		
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			tablesOfDb.add(rs.getString(3));
		}
		
		return tablesOfDb;
	}

	/**
	 * 
	 * Returns the names of all columns for a given table.
	 * 
	 * @param conn
	 * @param table
	 * @return
	 */
	protected List<String> getColumnsOfTable(Connection conn, String table) {
		
		List<String> columnsOfTable;

		try {
			DatabaseMetaData md = conn.getMetaData();
			columnsOfTable = new ArrayList<String>();
			ResultSet rs = md.getColumns(null, null, table, null);
			
			while (rs.next()) {
				columnsOfTable.add(rs.getString(4));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (columnsOfTable.size()==0) {
			throw new RuntimeException("Got no columns for table " + table);
		}
		return columnsOfTable;
	}
}
