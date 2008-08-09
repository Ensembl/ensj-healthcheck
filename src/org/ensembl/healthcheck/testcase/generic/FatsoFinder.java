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
 * Find occurrences of the string fatso.
 */

public class FatsoFinder extends SingleDatabaseTestCase {

	public FatsoFinder() {

		addToGroup("post_genebuild");
		addToGroup("id_mapping");
		addToGroup("release");
		setDescription("Find occurrences of the string fatso");
		
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
			
			List columnsAndTypes = DBUtils.getColumnsAndTypesInTable(con, table, "varchar");
			Iterator it = columnsAndTypes.iterator();
			while (it.hasNext()) {

				String[] columnAndType = (String[]) it.next();
				String column = columnAndType[0];
				
				int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + " LIKE '%fatso%'");
				
				if (rows > 0) {
					
					ReportManager.problem(this, con, rows + " rows in " + table + "." + column + " have the string fatso");
					result = false;
					
				} 
				
			}
			
		}

		return result;

	} // run
	
} // FatsoFinder
