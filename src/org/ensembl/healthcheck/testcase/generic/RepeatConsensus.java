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
\Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that there are certain types in repeat_types.
 */

public class RepeatConsensus extends SingleDatabaseTestCase {

	/**
	 * Create a new RepeatConsensus testcase.
	 */
	public RepeatConsensus() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check there certain types in repeat_consensus.repeat_type.");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This test only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

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
		String query = "SELECT COUNT(*) FROM repeat_consensus WHERE repeat_type = ''";
		if (dbre.getType() != DatabaseType.SANGER_VEGA) {// for sangervega, simple is fine
			query += " OR repeat_type ='Simple'";
		}
		int rows = DBUtils.getRowCount(con, query);

		if (rows > 0) {
			String report = "repeat_consensus table has " + rows + " rows of repeat_type empty";
			if (dbre.getType() != DatabaseType.SANGER_VEGA) {// for sangervega, simple is fine
				report += " OR 'Simple'";
			}
			ReportManager.problem(this, con, report);
			if (dbre.getType() == DatabaseType.SANGER_VEGA) {
				ReportManager.problem(this, con, "This probably means the .../sanger-plugins/vega/utils//vega_repeat_libraries.pl script was not run.");
			} else {
				ReportManager.problem(this, con, "This probably means the ensembl/misc-scripts/repeats/repeat-types.pl script was not run.");
			}
			result = false;

		} else {

			ReportManager.correct(this, con, "repeat_consensus appears to have valid repeat_types");
		}

		return result;

	} // run

} // RepeatConsensus
