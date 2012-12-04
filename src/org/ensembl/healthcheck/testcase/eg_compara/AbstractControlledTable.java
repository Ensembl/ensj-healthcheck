package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlTemplate.ResultSetCallback;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.EnsTestCase;

public abstract class AbstractControlledTable extends AbstractTemplatedTestCase {
	
	public AbstractControlledTable() {
		appliesToType(DatabaseType.COMPARA);
	}

	protected abstract String getControlledTableName();
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		String controlledTableToTest = getControlledTableName();
		
		DatabaseRegistryEntry masterDbRe = getComparaMasterDatabase();
		Connection testDbConn = dbre.getConnection();
		
		if (masterDbRe==null) {
			ReportManager.problem(
				this, 
				testDbConn, 
				"Can't get connection to master database! Perhaps it is not "
				+"configured?"
			);
			return false;
		}

		Connection comparaMasterconn = masterDbRe.getConnection();
		
		boolean result = checkAllRowsInTable(
				controlledTableToTest, 
				testDbConn,
				comparaMasterconn
		);		
		return result;
	}
	
	protected int getMaxReportedMismatches() {
		return 50;
	}

	/**
	 * @param controlledTableToTest
	 * @param testDbConn
	 * @param sqlTemplateTestDb
	 * @param sqlTemplateComparaMaster
	 * @return
	 */
	protected boolean checkAllRowsInTable(
			final String controlledTableToTest,
			final Connection testDbConn,
			final Connection comparaMasterconn
		) {
		
		final SqlTemplate sqlTemplateTestDb        = getSqlTemplate(testDbConn);  
		final SqlTemplate sqlTemplateComparaMaster = getSqlTemplate(comparaMasterconn);
		
		String fetchAllRowsFromTableSql = generateFetchAllRowsFromTableSql(testDbConn, controlledTableToTest);

		final List<String> columns = getColumnsOfTable(testDbConn, controlledTableToTest);
		
		final EnsTestCase thisTest = this;
		
		final int maxReportedMismatches = getMaxReportedMismatches();		
		
		boolean result = sqlTemplateTestDb.execute(
			fetchAllRowsFromTableSql,
			new ResultSetCallback<Boolean>() {

				@Override public Boolean process(ResultSet rs) throws SQLException {					
					
					boolean allRowsPresentInMasterDb = true;
					
					int numReportedRows = 0;
					boolean numReportedRowsExceedsMaximum = false;
					
					while (rs.next() && !numReportedRowsExceedsMaximum) {

						boolean currentRowPresentInMasterDb = isCurrentRowInMaster(
							rs,
							sqlTemplateComparaMaster, 
							controlledTableToTest,
							columns 
						);
						
						allRowsPresentInMasterDb &= currentRowPresentInMasterDb;
						
						if (!currentRowPresentInMasterDb) {
							
							numReportedRows++;
							numReportedRowsExceedsMaximum = numReportedRows>maxReportedMismatches;
							
							if (numReportedRowsExceedsMaximum) {
								ReportManager.problem(
										thisTest, 
										testDbConn, 
										"The maximum of " + maxReportedMismatches + " reported rows has been reached, no further rows will be tested."
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
			new Object[0]
		);
		return result;
	}
	
	/**
	 * @param controlledTableToTest
	 * @param sqlTemplateComparaMaster
	 * @param columns
	 * @param rsFromTestDb
	 * @throws SQLException
	 */
	protected boolean isCurrentRowInMaster(
			final ResultSet rsFromTestDb,
			final SqlTemplate sqlTemplateComparaMaster,
			final String controlledTableToTest,
			final List<String> columns 
	) throws SQLException {
		
		int numColumns = rsFromTestDb.getMetaData().getColumnCount();
		List<Object> columnValuesObjects = new ArrayList<Object>(numColumns);						

		for(int currentColIndex=0; currentColIndex<numColumns; currentColIndex++) {
			
			Object value = rsFromTestDb.getObject(currentColIndex+1);
			columnValuesObjects.add(currentColIndex, value);						
		}
		
		String countMatchingRowsSql = "select count(*) from " + controlledTableToTest + " where " + asParameterisedWhereClause(columns, columnValuesObjects);
		
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
					
					ReportManager.problem(thisTest, getComparaMasterDatabase().getConnection(), 
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

	protected String fetchAllRowsFromTableSql(
			Connection conn, 
			String tableName, 
			List<String> columns
		) {
		return "select " + asCommaSeparatedString(columns) + " from " + tableName;			
	}
	
	protected String generateFetchAllRowsFromTableSql(Connection conn, String tableName) {

		List<String> columns = getColumnsOfTable(conn, tableName);			
		String sql = fetchAllRowsFromTableSql(conn, tableName, columns);
			
		return sql;
	}
	
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
	
	protected String asCommaSeparatedString(List<String> listOfStrings) {		
		return joinListOfStrings(listOfStrings, ", ");
	}
	
	protected String joinListOfStrings(List<String> listOfStrings, String separator) {
		
		int numStrings = listOfStrings.size();
		
		StringBuffer commaSeparated = new StringBuffer();			
		
		commaSeparated.append(listOfStrings.get(0));
		for(int i=1; i<numStrings; i++) {
			commaSeparated.append(separator + listOfStrings.get(i));
		}
		return commaSeparated.toString();

	}
	
	protected List<String> getTablesOfDb(Connection conn) throws SQLException {
		
		DatabaseMetaData md = conn.getMetaData();
		
		List<String> tablesOfDb = new ArrayList<String>(); 
		
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			tablesOfDb.add(rs.getString(3));
		}
		
		return tablesOfDb;
	}
	
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
