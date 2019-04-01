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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Performs checks between the member table and the gene tree member table
 * ensuring all applicable entities are linked
 * 
 * @author ayates
 */
public class EGForeignKeyGeneTreeMemberId extends AbstractEGForeignKeyMemberId {

	/**
	 * <p>
	 * 	Overridden behaviour. In this class we want to check for orphans, but 
	 * allow the linking column of the target table (table1) to be null.
	 * </p>
	 * 
	 * <p>
	 * 	This is important for the gene_tree_node table, in which the member_id
	 * is null for internal nodes of trees. Only leaf nodes link to member.
	 * Therefore we are only interested in rows having a member_id that does 
	 * not link to any member.
	 * </p>
	 */
	public boolean checkForOrphans(
				Connection con, 
				String table1, 
				String col1,
				String table2, 
				String col2) {
		
		return checkForOrphans(con, table1, col1, table2, col2, true);
	}

	/**
	 * checkForOrphans method with an optional parameter for skipping rows 
	 * with a column that doesn't link, because it is null.
	 *  
	 */
	public boolean checkForOrphans(
			Connection con, 
			String table1, 
			String col1,
			String table2, 
			String col2, 
			boolean ignoreTargetNulls
		) {		
			if (ignoreTargetNulls) {
				return checkForOrphansNotNull(con, table1, col1, table2, col2);
			} else {
				return super.checkForOrphans(con, table1, col1, table2, col2);
			}			
	}

	/**
	 * An implementation similar to
	 * 
	 * @see #checkForOrphans(Connection, String, String, String, String)
	 * 
	 * but this one only searches for orphans in which the key column of the 
	 * target table is not null.
	 * 
	 */
	public boolean checkForOrphansNotNull(
				Connection con, 
				String table1, 
				String col1,
				String table2, 
				String col2
			) {

		int orphans = 0;
		boolean result = true;

		orphans = countOrphansIgnoreTable1Nulls(con, table1, col1, table2, col2);

		String useful_sql = "SELECT " + table1 + "." + col1 + generateSqlFromClause(table1, col1, table2, col2);

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
	}
	
	public int countOrphansIgnoreTable1Nulls(
				Connection con, 
				String table1, 
				String col1,
				String table2, 
				String col2) {

		String sql = "SELECT COUNT(*)" + generateSqlFromClause(table1, col1, table2, col2);
		
		//logger.info(sql);

		int resultLeft = DBUtils.getRowCount(con, sql);

		if (resultLeft > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT " + table1
					+ "." + col1 + sql + " LIMIT 20");
			for (int i = 0; i < values.length; i++) {
				ReportManager.info(this, con, table1 + "." + col1 + " "
						+ values[i] + " is not linked.");
			}
		}
		return resultLeft;
	}

	protected String generateSqlFromClause(
			String table1, 
			String col1,
			String table2, 
			String col2) {

		return " FROM " + table1 + " LEFT JOIN " + table2 + " ON "
				+ table1 + "." + col1 + " = " + table2 + "." + col2 + " WHERE "
				+ table2 + "." + col2 + " IS NULL and "+ table1+"."+col1+" is not null";		
	}
	
	@Override
	protected String getTargetTable() {
		return "gene_tree_node";
	}
  
	protected String getSourceTable() {
		return "seq_member";
	}
	
	protected String getSourceField() {
		return "seq_member_id";
	}
	
}
