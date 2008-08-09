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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all table collations in a particular database are
 * latin1_swedish_ci.
 */

public class SingleDBCollations extends SingleDatabaseTestCase {

	private static String TARGET_COLLATION = "latin1_swedish_ci";

	/**
	 * Create a new SingleDBCollations testcase.
	 */
	public SingleDBCollations() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that all table collations are latin1_swedish_ci");

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

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW TABLE STATUS");

			while (rs.next()) {
				String table = rs.getString("name");
				String collation = rs.getString("collation");
				if (collation == null) {
					ReportManager.problem(this, con, "Can't get collation for " + table);
					result = false;
					continue;
				}
				if (!collation.equals(TARGET_COLLATION)) {
					ReportManager.problem(this, con, table + " has a collation of '" + collation + "' which is not the same as the target "
							+ TARGET_COLLATION);
					result = false;
				}
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			se.printStackTrace();
		}

		if (result) {
			ReportManager.correct(this, con, "All tables have collation " + TARGET_COLLATION);
		}

		return result;

	} // run

} // SingleDBCollations
