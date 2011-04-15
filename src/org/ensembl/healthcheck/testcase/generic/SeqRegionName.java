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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the seq_region names are in the right format. Only checks human and mouse.
 */

public class SeqRegionName extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionName testcase.
	 */
	public SeqRegionName() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("compara-ancestral");
		setDescription("Check that seq_region names for human and mouse are in the right format.");
                setTeamResponsible("GeneBuilders");
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

		// only do this for human and mouse
		Species s = dbre.getSpecies();
		if (s.equals(Species.HOMO_SAPIENS) || s.equals(Species.MUS_MUSCULUS)) {

			Connection con = dbre.getConnection();

			result &= seqRegionNameCheck(con, "clone", "^[a-zA-Z]+[0-9]+\\.[0-9]+$");
			result &= seqRegionNameCheck(con, "contig", "^[a-zA-Z]+[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$");

		} else if (s.equals(Species.ANCESTRAL_SEQUENCES)) {

			Connection con = dbre.getConnection();

			result &= seqRegionNameCheck(con, "ancestralsegment", "Ancestor_[0-9]+_[0-9]+$");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------
	/**
	 * Check that seq regions of a particular coordinate system are named appropriately.
	 * 
	 * @return True if all seq_region names match the regexp.
	 */

	private boolean seqRegionNameCheck(Connection con, String coordinateSystem, String regexp) {

		boolean result = true;

		int rows = getRowCount(con, String.format(
				"SELECT COUNT(*) FROM seq_region sr, coord_system cs WHERE sr.coord_system_id=cs.coord_system_id AND cs.name='%s' AND sr.name NOT LIKE 'LRG%%' AND sr.name NOT REGEXP '%s' ", coordinateSystem,
				regexp));

		if (rows > 0) {

			ReportManager.problem(this, con, String.format("%d seq_regions in coordinate system %s have names that are not of the correct format", rows, coordinateSystem));
			result = false;

		} else {

			ReportManager.correct(this, con, String.format("All seq_regions in coordinate system %s have names in the correct format", coordinateSystem));

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // SeqRegionName
