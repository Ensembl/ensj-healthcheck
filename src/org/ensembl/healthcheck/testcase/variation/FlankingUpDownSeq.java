/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
