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
 * Check for rows that contain the *string* NULL - should probably be the
 * database primitive NULL.
 */

public class NullStrings extends SingleDatabaseTestCase {

	/**
	 * Create a new NullStrings testcase.
	 */
	public NullStrings() {

		addToGroup("post_genebuild");
		addToGroup("id_mapping");
		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("funcgen");

		setDescription("Check for rows that contain the *string* NULL - should probably be the database primitive NULL.");
		
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
			boolean thisTableProblem = false;
			
			List columnsAndTypes = DBUtils.getTableInfo(con, table, "varchar");
			Iterator it = columnsAndTypes.iterator();
			while (it.hasNext()) {

				String[] columnAndType = (String[]) it.next();
				String column = columnAndType[0];
				
				int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + "='NULL'");
				
				if (rows > 0) {
					
					ReportManager.problem(this, con, rows + " rows in " + table + "." + column + " have the string value NULL, not a proper NULL value");
					result = false;
					thisTableProblem = true;
					
				} 
				
			}
			
			// only store one correct report per table to avoid swamping output
			if (!thisTableProblem) {
				ReportManager.correct(this, con, "No columns with NULL strings in " + table);
			}
			
		}

		return result;

	} // run
	
} // NullStrings
