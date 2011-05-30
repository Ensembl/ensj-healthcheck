/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that if the up_seq or down_seq of flanking_sequence is null, that up_seq_region_start or down_seq_region_start should not
 * be null.
 */
public class FlankingUpDownSeq extends SingleDatabaseTestCase {
	
	/**
	 * Creates a new instance of CheckFlankingUpDownSeq
	 */
	public FlankingUpDownSeq() {
		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Check that if the up_seq or down_seq of flanking_sequence is null, that up_seq_region_start or down_seq_region_start should not be null.");
		setTeamResponsible(Team.VARIATION);

	}
	
	/**
	 * Find any matching databases that have both no up_seq and up_seq_region_start.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// check up_seq and up_seq_region_start

		Connection con = dbre.getConnection();
		
		try {
			
			// Verify that entries in flanking_sequence have only one of [up|down]_seq and [up|down]_seq_region_[start|end] set
			result &= checkCountIsZero(con, "flanking_sequence", 
					"(up_seq IS NULL AND (up_seq_region_start IS NULL OR up_seq_region_end IS NULL)) OR " +
					"(up_seq IS NOT NULL AND (up_seq_region_start IS NOT NULL OR up_seq_region_end IS NOT NULL))");
			result &= checkCountIsZero(con, "flanking_sequence", 
					"(down_seq IS NULL AND (down_seq_region_start IS NULL OR down_seq_region_end IS NULL)) OR " +
					"(down_seq IS NOT NULL AND (down_seq_region_start IS NOT NULL OR down_seq_region_end IS NOT NULL))");
							
		} catch (Exception e) {
			result = false;
			ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
		}
		
 		if (result) {
			ReportManager.correct(this, con, "All variations have flanking sequence correct");
		}
		return result;

	} // run

} // FlankingUpDownSeq
