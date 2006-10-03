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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that no genes are on a sequence_level coord system. Single-coord system
 * species are ignored.
 */
public class GeneCoordSystem extends SingleDatabaseTestCase {

	
	/**
	 *  Check that no genes are on a sequence_level coord system.
	 */
	public GeneCoordSystem() {

		addToGroup("release");
		setDescription("Check that no genes are on a sequence_level coord system.");

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		// look for genes on sequence_level coord systems
		// ignore situations where the sequence_level coord system is also the top level (rank = 1)
		// such as yeast, as there is only one co-ordinate system
		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene g, coord_system c, seq_region s WHERE g.seq_region_id = s.seq_region_id AND s.coord_system_id = c.coord_system_id AND c.attrib LIKE '%sequence_level%' AND c.rank > 1");
		
		if (rows > 0) {
			ReportManager.problem(this, con, rows + " genes are on a sequence_level coord system; this will slow down the mapper.");
			result = false;
		} else {
			ReportManager.correct(this, con, "No genes on a sequence_level coord system");
		}
		return result;

	} // run

	// -----------------------------------------------------------------

} // GeneCoordSystem
