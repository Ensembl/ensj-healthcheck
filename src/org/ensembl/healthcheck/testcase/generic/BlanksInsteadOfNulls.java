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

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for text columns that have the default NULL but which actually contain blanks ("") which is probably wrong.
 */

public class BlanksInsteadOfNulls extends SingleDatabaseTestCase {

	/**
	 * Create a new BlanksInsteadOfNulls testcase.
	 */
	public BlanksInsteadOfNulls() {

		setDescription("Check for text columns that have the default NULL but which actually contain blanks ('') which is probably wrong");
		setTeamResponsible(Team.GENEBUILD);

	}
	
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] tables = DBUtils.getTableNames(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];

			List<String[]> columnsAndTypes = DBUtils.getTableInfo(con, table, "varchar");
			Iterator<String[]> it = columnsAndTypes.iterator();
			while (it.hasNext()) {

				String[] columnInfo = (String[]) it.next();
				String column = columnInfo[0];

				// display_labels should be neither blank nor null
				if (column.equals("display_label")) {
					continue;
				}

				String allowedNull = columnInfo[2];
				String columnDefault = columnInfo[4];
				if (columnDefault != null && !columnDefault.toLowerCase().equals("null")) {
					continue;
				}
				int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=''");

				if (rows > 0) {

					String str = rows + " rows in " + table + "." + column + " have blank values";

					if (allowedNull.toUpperCase().equals("YES")) {
						str += ", should probably be NULL";
						str += "\n   Useful SQL: UPDATE " + table + " SET " + column + "=null WHERE " + column + "='';";
					} else {
						str += ", these should probably be changed to something more meaningful, however NULLs are not allowed by the column definition";
					}

					ReportManager.problem(this, con, str);

					result = false;

				}

			}

		}

		return result;

	} // run

} // BlanksInsteadOfNulls
