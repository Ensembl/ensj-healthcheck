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
package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that certain views/tables required for the Biomart build are not present.
 */

public class PostMartTables extends SingleDatabaseTestCase {
	
	/**
	 * Constructor.
	 */
	public PostMartTables() {

		// TODO - group specifically for this?
		setDescription("Check that certain views/tables required for the Biomart build are not present.");

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
		
		for (String table : getBiomartFuncgenTablesAndViews()) {
			
			if (DBUtils.checkTableExists(con, table)) {
				
				ReportManager.problem(this, con,  table + " exists but should have been removed after the Biomart build");
				result = false;

			} else {
				
				ReportManager.correct(this, con, table + " has been removed.");
				
			}
			
		}
		
		return result;

	} // run

} // PostMartTables
