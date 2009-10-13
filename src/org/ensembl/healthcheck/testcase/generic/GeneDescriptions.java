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
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * 
 */

public class GeneDescriptions extends SingleDatabaseTestCase {

	/**
	 * Create a new GeneDescriptions testcase.
	 */
	public GeneDescriptions() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check gene descriptions; correct capitalisation of UniprotKB/SwissProt");
		setPriority(Priority.AMBER);
		setEffect("Capitalisation of Uniprot will be wrong in gene descriptions.");
		setFix("Re-run xref system or manually fix affected xrefs.");

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

		result &= checkSwissprot(dbre);

		return result;

	} // run

	// --------------------------------------------------------------------------
	
	private boolean checkSwissprot(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE description like '%Uniprot%' COLLATE latin1_general_cs");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " descriptions have incorrect spelling/capitalisation of Uniprot attribution");
			result = false;
		} else {
			ReportManager.correct(this, con, "All Uniprot attributions correct.");
		}

		return result;

	}
} // GeneDescriptions
