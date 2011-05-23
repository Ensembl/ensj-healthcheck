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
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that transcript frameshift attributes have been calculated.
 */

public class FrameshiftAttributes extends SingleDatabaseTestCase {

	/**
	 * Create a new FrameshiftAttributes testcase.
	 */
	public FrameshiftAttributes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that transcript frameshift attributes have been calculated.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	/**
	 * Only applies to core dbs.
	 */
	public void types() {

		List types = new ArrayList();

		types.add(DatabaseType.CORE);

		setAppliesToTypes(types);

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

		int rows = getRowCount(con, "SELECT COUNT(*) FROM attrib_type at, transcript_attrib ta WHERE at.attrib_type_id=ta.attrib_type_id AND at.code='Frameshift'");

		if (rows == 0) {

			ReportManager.problem(this, con, "No transcript frameshift attributes found\n --> make sure you ran script ensembl/misc_scripts/frameshift_transcript_attribs.pl");
			result = false;

		} else {

			ReportManager.correct(this, con, rows + " transcript frameshift attributes found");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // ESTStableID
