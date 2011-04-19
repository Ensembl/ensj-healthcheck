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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that not all xrefs have the same identifier. Also check that there are no blank/null display_labels.
 */

public class XrefIdentifiers extends SingleDatabaseTestCase {

	/**
	 * Create a new XrefIdentifiers testcase.
	 */
	public XrefIdentifiers() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that not all xrefs have the same identifier.");
		setPriority(Priority.AMBER);
		setEffect("Web display and all other uses of xrefs are broken");
		setFix("Re-import or recalculate xrefs");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

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

		result &= checkSame(con);

		result &= checkBlank(con);

		return result;

	} // run

	// ----------------------------------------------------------------------

	private boolean checkSame(Connection con) {

		boolean result = true;

		// TODO - do this on a per-xref-type basis
		try {

			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT(dbprimary_acc)) FROM xref");

			rs.next();

			int count = rs.getInt(1);
			if (count == 1) {
				ReportManager.problem(this, con, "All xrefs appear to have the same identifier.");
				result = false;
			} else {
				ReportManager.correct(this, con, "Not all xrefs have the same identifier.");
			}

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (result) {

			ReportManager.correct(this, con, "All external dbs are only associated with one Ensembl object type");

		}

		return result;

	}

	// ----------------------------------------------------------------------

	private boolean checkBlank(Connection con) {

		boolean result = true;

		String[] columns = { "dbprimary_acc", "display_label" };

		for (String column : columns) {

			int rows = getRowCount(con, "SELECT COUNT(*) FROM xref WHERE " + column + "='' OR " + column + " IS NULL");

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " xrefs have blank or null " + column + "s");
				result = false;

			} else {

				ReportManager.correct(this, con, "No blank xref " + column + "s");
			}

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // XrefIdentifiers
