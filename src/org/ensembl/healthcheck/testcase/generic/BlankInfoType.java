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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for any xref.info_type that are blank ('') - they should be NULL for various other things to work.
 */

public class BlankInfoType extends SingleDatabaseTestCase {

	/**
	 * Create a new BlankInfoType testcase.
	 */
	public BlankInfoType() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("funcgen");

		setDescription("Check for any xref.info_type that are blank ('') - they should be NULL for various other things to work.");
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

		int rows = getRowCount(con, "SELECT COUNT(*) FROM xref WHERE info_type = ''");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " rows in xref have a blank info_type - should be set to null");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank info_types in xref");

		}

		return result;

	} // run

} // BlankInfoType
