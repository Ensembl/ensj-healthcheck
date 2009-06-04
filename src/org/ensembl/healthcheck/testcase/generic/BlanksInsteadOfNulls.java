/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
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

		addToGroup("post_genebuild");
		addToGroup("id_mapping");
		addToGroup("release");
		setDescription("Check for text columns that have the default NULL but which actually contain blanks ('') which is probably wrong");

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
				String allowedNull = columnInfo[2];
				String columnDefault = columnInfo[4];
				if (columnDefault != null && !columnDefault.toLowerCase().equals("null")) {
					continue;
				}
				int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=''");

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
