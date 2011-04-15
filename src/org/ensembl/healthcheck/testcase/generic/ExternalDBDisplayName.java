package org.ensembl.healthcheck.testcase.generic;

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that there are no blank db_display_name fields in external_db.
 */

public class ExternalDBDisplayName extends SingleDatabaseTestCase {

	/**
   * Create a new PredictedXrefs testcase.
   */
	public ExternalDBDisplayName() {

		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		setDescription("Check that there are no blank db_display_name fields in external_db");
                setTeamResponsible("Release Coordinator");
	}

	/**
   * Run the test.
   * 
   * @param dbre The database to use.
   * @return true if the test passed.
   * 
   */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		int rows = getRowCount(con, "SELECT COUNT(*) FROM external_db WHERE db_display_name IS NULL OR db_display_name LIKE ' %'");
		
		if (rows > 0) {
			
			ReportManager.problem(this, con, rows + " rows in external_db have null or blank db_display_name - this will mean their label is missing on the web page");
			result = false;
			
		} else {
			
			ReportManager.correct(this, con, "No blank db_display_name fields in external_db");
			
		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // ExternalDBDisplayName
