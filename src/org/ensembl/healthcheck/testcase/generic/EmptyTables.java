/*
 Copyright (C) 2004 EBI, GRL
 
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

import java.sql.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Check that all tables have data.
 */
public class EmptyTables extends EnsTestCase {

	/**
	 * Creates a new instance of EmptyTablesTestCase
	 */
	public EmptyTables() {

		//addToGroup("post_genebuild");
		setDescription("Checks that all tables have data");

	}

	//---------------------------------------------------------------------

	public DatabaseType[] databaseTypes() {

		DatabaseType[] types = { DatabaseType.CORE, DatabaseType.VEGA };
		return types;

	}

	//---------------------------------------------------------------------
	
	public boolean isMultiDatabaseTest() {
	
		return false;
		
	}
	
	//---------------------------------------------------------------------

	/**
	 * Define what tables are to be checked. Can be overridden in subclasses
	 * that want to check a subset of these.
	 * 
	 * @param con A database connection in case this method needs to query the
	 *            list of tables.
	 * @return An array of Strings representing the table names to be checked.
	 */
	private String[] getTablesToCheck(Connection con) {

		return getTableNames(con);

	}

	//---------------------------------------------------------------------

	/**
	 * Check that every table has more than 0 rows.
	 */
	public boolean run(Connection con) {

		boolean result = true;

		String[] tables = getTablesToCheck(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			logger.finest("Checking that " + table + " has rows");

			if (!tableHasRows(con, table)) {

				ReportManager.problem(this, con, table + " has zero rows");
				result = false;

			}
		}

		return result;

	} // run

} // EmptyTablesTestCase
