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
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for xrefs that have HTML markup in the display_label.
 */
public class XrefHTML extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of XrefHTML
	 */
	public XrefHTML() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that there are no xrefs with HTML markup");
		setPriority(Priority.AMBER);
		setEffect("Causes HTML markup to be displayed unrendered");
		setFix("Manually fix affected xrefs.");
		setTeamResponsible("core");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry entry to be checked.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		int rows = getRowCount(con, "SELECT COUNT(*) FROM xref WHERE display_label LIKE '%<%>%<%>%'");
		
		if (rows > 0) {
			ReportManager.problem(this, con, rows + " xrefs appear to have HTML markup (<...>) in the display_label");
			result = false;
		} else {
			ReportManager.correct(this, con, "No xrefs appear to have HTML markup in the display_label");
		}
		
		return result;
		
	}
		
} // XrefHTML

