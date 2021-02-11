/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlTemplate.ResultSetCallback;

abstract public class AbstractControlledRows extends AbstractTemplatedTestCase {

	protected DatabaseRegistryEntry masterDbRe;
	protected Connection masterDbConn;
	protected SqlTemplate masterSqlTemplate;

	protected void init(Connection conn) {
		masterDbRe = getComparaMasterDatabase();

		if (masterDbRe == null) {
			ReportManager.problem(
				this,
				conn,
				"Can't get connection to master database! Perhaps it has not been configured?"
			);
			return;
		}

		masterDbConn = masterDbRe.getConnection();
		masterSqlTemplate = getSqlTemplate(masterDbConn);		
	}

	protected boolean checkRangeOfRowsInTable(
			final String controlledTableToTest,
			final String masterTable,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry refDbre,
			int limit,
			int offset
		) {
		return checkRangeOfRowsInTable(
				controlledTableToTest,
				masterTable,
				testDbre,
				refDbre,
				"",
				limit,
				offset
			);
	}
	
	/**
	 * For every row of the table controlledTableToTest in the database 
	 * testDbre this checks, if this row also exists in the table 
	 * masterTable of refDbre.
	 * 
	 */
	protected boolean checkRangeOfRowsInTable(
			final String controlledTableToTest,
			final String masterTable,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry refDbre,
			String whereClause,
			int limit,
			int offset
		) {

		final Connection testDbConn = testDbre.getConnection();
		final Connection masterconn = refDbre.getConnection();
		
		final SqlTemplate sqlTemplateTestDb        = getSqlTemplate(testDbConn);  
		final SqlTemplate sqlTemplateComparaMaster = getSqlTemplate(masterconn);
		
		String fetchAllRowsFromTableSql = generateFetchAllRowsFromTableSql(testDbConn, controlledTableToTest, whereClause, limit, offset);

		final EnsTestCase thisTest = this;
		final List<String> testTableColumns = getColumnsOfTable(testDbConn, controlledTableToTest);
		
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
	 * Creates a where clause for a sql statement of the form column_1=? and 
	 * column_2=? ... column_n=?. The listOfValues parameter is used to 
	 * determine whether a value will be compared with "=" or with "is". By
	 * default "=" is used, but "is" will be used for null values like 
	 * "... and column_i is null".  
	 * 
	 * @param listOfColumns
	 * @param listOfValues
	 * @return where clause
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
	 * 
	 * For the given ResultSet object this will return a stringified version 
	 * of the current row. Useful to print in error or debug messages.
	 * 
	 * @param rs
	 * @return row as string
	 * @throws SQLException
	 */
	protected String resultSetRowAsString(ResultSet rs)
			throws SQLException {
		int numColumns = rs.getMetaData().getColumnCount();
		List<String> columnValuesStringy = new ArrayList<String>(numColumns);
		for(int currentColIndex=0; currentColIndex<numColumns; currentColIndex++) {
			
			Object value = rs.getObject(currentColIndex+1);
			String label = rs.getMetaData().getColumnName(currentColIndex+1);
			
			String convertedValue = label + "=";
			if (value==null) {
				convertedValue += "<null>";
			} else {
				convertedValue += value.toString();
			}
			columnValuesStringy.add(currentColIndex, convertedValue);							
		}
		return asCommaSeparatedString(columnValuesStringy);
	}
	
	/**
	 * Joins the list of strings into one comma (and space) separated string.
	 * 
	 * @param listOfStrings
	 * @return list as string
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
	 * @return list as string
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
	 * Generates a sql statement that will fetch all columns of all rows from
	 * the given table.
	 * 
	 * @param conn
	 * @param tableName
	 * @return SQL statement
	 */
	protected String generateFetchAllRowsFromTableSql(
			Connection conn, 
			String tableName,
			int limit,
			int offset
		) {
		return generateFetchAllRowsFromTableSql(conn, tableName, "", limit, offset);
	}

	protected String generateFetchAllRowsFromTableSql(
			Connection conn, 
			String tableName,
			String whereClause,
			int limit,
			int offset
		) {
		
		List<String> columns = getColumnsOfTable(conn, tableName);			
		String sql = fetchAllRowsFromTableSql(conn, tableName, columns, whereClause, limit, offset);
			
		return sql;
	}

	/**
	 * 
	 * Generates a sql statement that will fetch the given columns of all rows
	 * of the table.
	 * 
	 * @param conn
	 * @param tableName
	 * @param columns
	 * @return SQL statement
	 */
	protected String fetchAllRowsFromTableSql(
			Connection conn, 
			String tableName, 
			List<String> columns,
			int limit,
			int offset

		) {		
		return fetchAllRowsFromTableSql(conn, tableName, columns, limit, offset);			
	}

	protected String fetchAllRowsFromTableSql(
			Connection conn, 
			String tableName, 
			List<String> columns,
			String whereClause,
			int limit,
			int offset

		) {		
		return "select " + asCommaSeparatedString(columns) + " from " + tableName + " " + whereClause + " limit " + limit + " offset " + offset;			
	}

	/**
	 * Maximum number of rows to be fetched in one iteration;
	 */
	protected final int batchSize = 1000;

	protected boolean numReportedRowsExceedsMaximum() {
		return numReportedRows>getMaxReportedMismatches();
	}

	/**
	 * If the ComparisonStrategy RowByRow
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
	 * ComparisonStrategy RowByRow is being used. If numReportedRows 
	 * exceeds getMaxReportedMismatches(), the test will terminate.
	 * 
	 */
	protected int numReportedRows;

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

	/**
	 * 
	 * Returns the names of all columns for a given table.
	 * 
	 * @param conn
	 * @param table
	 * @return names of columns
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

	/**
	 * <p>
	 * 	List of method names in the compara master that mean DNA compara is
	 * run.
	 * </p>
	 * <p>
	 * 	If you modify this list, please make sure that all method names are 
	 * quoted.
	 * </p> 
	 */
	protected final List<String> dnaComparaMethods = 
		Arrays.asList(
			new String[] { 
				"'GenomicAlignBlock.pairwise_alignment'",
			    "'GenomicAlignBlock.multiple_alignment'",
			    "'GenomicAlignTree.tree_alignment'",
			    "'GenomicAlignBlock.constrained_element'"
			}
		);
	
	/**
	 * <p>
	 * 	Given the production name of a species, checks, if it is linked to a 
	 * compara method that involves dna comparisons.  
	 * </p>
	 *  
	 * @param speciesName
	 * @return true if linked to DNA compara
	 */
	protected boolean speciesConfiguredForDnaCompara(String speciesName) {
		
		String dnaComparaMethodsCommaSep = StringUtils.join(dnaComparaMethods, ", ");
		
		// We use "distinct", but if a species is configured for more than one
		// dna compara method, this will still return more than one row.
		//
		String sql = "select distinct genome_db.genome_db_id, genome_db.name, method_link.class "
				+ "from "
				+ "	genome_db " 
				+ "	join species_set using (genome_db_id) " 
				+ "	join method_link_species_set using (species_set_id) " 
				+ "	join method_link using (method_link_id) "
				+ "where "
				+ " genome_db.name='" + speciesName + "' " 
				+ " and method_link.class in ( "
				+ dnaComparaMethodsCommaSep
				+ " ) "; 
		
		List<Integer> dnaMethodsConfigured = masterSqlTemplate.queryForDefaultObjectList(
				sql, 
				Integer.class
				);

		if (dnaMethodsConfigured.size()>=1) {
			return true; 
		}
		if (dnaMethodsConfigured.size()==0) {
			return false;
		}
		throw new RuntimeException(
			"Unexpected number of rows returned!\n"
			+ "The sql used was:\n\n"
			+ sql
		);		
	}
}
