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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that no genes are named after numeric EntrezGene identifiers.
 */

public class EntrezGeneNumeric extends SingleDatabaseTestCase {

	
	/**
	 * Create a new EntrezGeneNumeric testcase.
	 */
	public EntrezGeneNumeric() {

		addToGroup("release");
		setDescription("Check that no genes are named after numeric EntrezGene identifiers.");
		setPriority(Priority.AMBER);
		setEffect("Causes genes to be displayed with numeric EntrezGene 'names', which is potentially confusing.");
		
	}

	/**
	 * Only run on core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		
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
		
		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene g, external_db e, xref x WHERE g.display_xref_id=x.xref_id AND e.external_db_id=x.external_db_id AND e.db_name='EntrezGene' AND x.display_label REGEXP '^[0-9]+$'");

		if (rows > 0) {
			
			ReportManager.problem(this, con, rows + " genes are named after numeric EntrezGene identifiers");
			result = false;
			
		} else {
			
			ReportManager.correct(this, con, "No genes are named after numeric EntrezGene identifiers");
			
		}
		
		return result;
		
	} // run

	//----------------------------------------------------------------------

} // EntrezGeneNumeric

