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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all seq_regions comprising genes are marked as toplevel in
 * seq_region_attrib. Also checks that there is at least one seq_region marked
 * as toplevel (needed by compara).
 */

public class SeqRegionsTopLevel extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionsTopLevel testcase.
	 */
	public SeqRegionsTopLevel() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that all seq_regions comprising genes are marked as toplevel in seq_region_attrib, and that there is at least one toplevel seq_region.");

	}

	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);

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

		// check that all gene seq_regions have toplevel attributes
		// pre-fetch correct attrib_type_id to save a join
		String val = getRowColumnValue(con, "SELECT attrib_type_id FROM attrib_type WHERE code=\'toplevel\'");
		if (val == null || val.equals("")) {
			ReportManager.problem(this, con, "Can't find a seq_region attrib_type with code 'toplevel', exiting");
			return false;
		}
		int topLevelAttribType = Integer.parseInt(val);

		logger.info("attrib_type_id for toplevel: " + topLevelAttribType);

			int numTopLevelGenes = getRowCount(con, "SELECT COUNT(*) FROM seq_region_attrib sra, gene g WHERE sra.attrib_type_id = " + topLevelAttribType
					+ " AND sra.seq_region_id=g.seq_region_id");
			int numGenes = getRowCount(con, "SELECT COUNT(*) FROM gene");
			
			int nonTopLevelGenes = numGenes - numTopLevelGenes;
			
			if (nonTopLevelGenes > 0) {
				
				ReportManager.problem(this, con, nonTopLevelGenes + " genes are on seq_regions which are not toplevel; this may cause problems for Compara and slow down the mapper.");
				result = false;
				
			} else {
				
				ReportManager.correct(this, con, "All genes are on toplevel seq regions");
				
			}

		// check for at least one toplevel seq_region
		int rows = getRowCount(con, "SELECT COUNT(*) FROM seq_region_attrib WHERE attrib_type_id=" + val);
		if (rows == 0) {

			ReportManager.problem(this, con, "No seq_regions are marked as toplevel. This may cause problems for Compara");
			result = false;

		} else {

			ReportManager.correct(this, con, rows + " seq_regions are marked as toplevel");

		}

		// check that there is one co-ordinate system with rank = 1
		rows = getRowCount(con, "SELECT COUNT(*) FROM coord_system WHERE rank=1");
		if (rows == 0) {

			ReportManager.problem(this, con, "No co-ordinate systems have rank = 1");
			result = false;

		} else if (rows > 1) {

			ReportManager.problem(this, con, rows + " rows in coord_system have a rank of 1. There can be only one.");
			result = false;

		} else {

			ReportManager.correct(this, con, "One co-ordinate system has rank = 1");

		}
		return result;

	} // run

} // SeqRegionsTopLevel
