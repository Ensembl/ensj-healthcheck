/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check whether tables need to be analysed.
 */
public class AnalyseTables extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalyseTables
	 */
	public AnalyseTables() {

		addToGroup("release");
		setDescription("Check whether tables need to be analysed.");
		setPriority(Priority.AMBER);
		setEffect("Causes indices not to be used, making queries slow or unresponsive.");
		setFix("Run ANALYSE TABLE x.");
		setTeamResponsible("ReleaseCoordinator");
		
	}

	/**
	 * Run the test.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		String[] tables = DBUtils.getTableNames(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			
			int results = getRowCount(con, " SHOW INDEX FROM " + table + " WHERE CARDINALITY IS NULL");
			
			if (results > 0) {
				ReportManager.problem(this, con, table + " needs to be analysed");
				result = false;
			}
		}
		
		return result;

	} // run

	// -----------------------------------------------------------------------

} // AnalyseTables

