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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * An extension of the SingleDatabaseTestCase to test for foreign-key
 * consistency within the same table
 */

public abstract class AbstractInternalForeignKey extends SingleDatabaseTestCase {


	/**
	 * Verify foreign-key relations.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            The table in which to check
	 * @param col1
	 *            First column in table to check.
	 * @param col2
	 *            Second column in table to check.
	 * @param col1_can_be_null
	 *            Whether NULLs in the first column are allowed
	 * @param oneWayOnly
	 *            If true, only a "left join" is performed. Otherwise,
	 *            both directions are checked
	 * @return The number of "orphans"
	 */
	public int countOrphansSameTable(Connection con, String table, String col1,
			String col2, boolean col1_can_be_null, boolean oneWayOnly) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
		}

		int resultLeft, resultRight;

		String sql = " FROM " + table + " hc_table1 LEFT JOIN " + table + " hc_table2 ON "
				+ "hc_table1." + col1 + " = hc_table2." + col2 + " WHERE "
				+ "hc_table2." + col2 + " IS NULL";
		if (col1_can_be_null) {
			sql = sql + " AND hc_table1." + col1 + " IS NOT NULL";
		}

		resultLeft = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

		logger.finest("Left: " + resultLeft);

		if (resultLeft > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT hc_table1"
					+ "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table + "." + col1 + " "
						+ values[i] + " is not linked.");
			}
		}

		if (!oneWayOnly) {
			// and the other way ... (a right join?)
			sql = " FROM " + table + "hc_table2 LEFT JOIN " + table + " hc_table1 ON "
					+ "hc_table2." + col2 + " = hc_table1." + col1 + " WHERE "
					+ "hc_table1." + col1 + " IS NULL";

			resultRight = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

			if (resultRight > 0) {
				String[] values = DBUtils.getColumnValues(con, "SELECT hc_table2"
						+ "." + col2 + sql + " LIMIT 20");
				for (int i = 0; i < values.length; i++) {
					ReportManager.info(this, con, table + "." + col2 + " "
							+ values[i] + " is not linked.");
				}
			}

			logger.finest("Right: " + resultRight);

		} else {
			resultRight = 0;
		}

		// logger.finest("Left: " + resultLeft + " Right: " + resultRight);

		return resultLeft + resultRight;

	} // countOrphansSameTable


	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if
	 * necessary.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            First column in table to check.
	 * @param col2
	 *            Second column in table to check.
	 * @param col1_can_be_null
	 *            Whether NULLs in the first column are allowed
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphansSameTable(Connection con, String table, String col1, String col2, boolean col1_can_be_null) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphansSameTable(con, table, col1, col2, col1_can_be_null, true);

		String useful_sql = "SELECT hc_table1." + col1 + " FROM " + table
				+ " hc_table1 LEFT JOIN " + table + " hc_table2 ON hc_table1." + col1 + " = "
				+ "hc_table2." + col2 + " WHERE hc_table2." + col2
				+ " IS NULL";
		if (col1_can_be_null) {
			useful_sql = useful_sql + " AND hc_table1." + col1 + " IS NOT NULL";
		}

		if (orphans > 0) {
			ReportManager.problem(this, con, "FAILED " + table + " : " + col1
					+ " -> " + col2 + " using FK "
					+ " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans
					+ " " + table + " entries are not linked from " + col1 + " to " + col2);
			ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			result = false;
		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table
					+ " : " + col1 + " -> " + col2 + " using FK "
					+ ", look at the StackTrace if any");
			result = false;
		}

		return result;

	} // checkForOrphans


} // AbstractInternalForeignKey
