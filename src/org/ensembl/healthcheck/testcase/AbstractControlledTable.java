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

package org.ensembl.healthcheck.testcase;

import java.sql.Connection;

import org.ensembl.healthcheck.testcase.eg_compara.AbstractControlledRows;
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
public abstract class AbstractControlledTable extends AbstractControlledRows {
	
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

	public AbstractControlledTable() {
		setTypeFromPackageName();
		setTeamResponsible(Team.ENSEMBL_GENOMES);
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		String controlledTableToTest = getControlledTableName();
		
		Connection testDbConn = dbre.getConnection();
		init(testDbConn);
		if (masterDbRe == null) {
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
			DatabaseRegistryEntry refDbre
		) {
		
		List<String> tablesToChecksum = new ArrayList<String>();
		tablesToChecksum.add(controlledTableToTest);
		
		String checksumValueMaster = calculateChecksumForTable(
				refDbre, tablesToChecksum);
		
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
	 * controlledTableToTest of refDbre.
	 * 
	 * @param controlledTableToTest
	 * @param testDbre
	 * @param refDbre
	 * @return true if rows exist
	 */
	protected boolean checkAllRowsInTable(
			final String controlledTableToTest,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry refDbre
		) {
		return checkAllRowsInTable(controlledTableToTest, controlledTableToTest, testDbre, refDbre);
	}
	
	/**
	 * For every row of the table controlledTableToTest in the database 
	 * testDbre this checks, if this row also exists in the table 
	 * masterTable of refDbre.
	 * 
	 * @param controlledTableToTest
	 * @param masterTable
	 * @param testDbre
	 * @param refDbre
	 * @return true if rows exist
	 */
	protected boolean checkAllRowsInTable(
			final String controlledTableToTest,
			final String masterTable,
			DatabaseRegistryEntry testDbre,
			DatabaseRegistryEntry refDbre
		) {
		
		final Logger logger = getLogger();
		
		final Connection testDbConn = testDbre.getConnection();
		final Connection masterconn = refDbre.getConnection();

		final SqlTemplate sqlTemplateTestDb        = getSqlTemplate(testDbConn);  
		
		int rowCount = sqlTemplateTestDb.queryForDefaultObject(
			"select count(*) from " + controlledTableToTest,
			Integer.class
		);
		
		logger.info("Number of rows in table: " + rowCount);
		
		final List<String> testTableColumns = getColumnsOfTable(testDbConn, controlledTableToTest);		
		final List<String> masterColumns    = getColumnsOfTable(masterconn, masterTable);		
		
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
				refDbre,
				limit,
				currentOffset
			);			
		}
		return allRowsInMaster;
	}

	/**
	 * 
	 * Returns the names of all tables in the database.
	 * 
	 * @param conn
	 * @return names of tables
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

}
