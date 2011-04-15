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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that certain tables have display_labels set.
 */

public class DisplayLabels extends SingleDatabaseTestCase {

	/**
	 * Create a new DisplayLabels testcase.
	 */
	public DisplayLabels() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that certain tables have display_labels set");
                setTeamResponsible("Core and GeneBuilders");
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

		result &= checkNoNulls(con, "prediction_transcript", "display_label");
		result &= checkNoNulls(con, "simple_feature", "display_label");
		result &= checkNoNulls(con, "xref", "display_label");

		return result;

	} // run

	// ----------------------------------------------------------------------

} // DisplayLabels
