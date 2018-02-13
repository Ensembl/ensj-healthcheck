/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for sequence that does not match GTACN.
 */

public class NonGTACNSequence extends SingleDatabaseTestCase {

	/**
	 * Create a new NonGTACNSequence testcase.
	 */
	public NonGTACNSequence() {

		setDescription("Check for sequence that does not match GTACN.");
		setPriority(Priority.AMBER);
		setEffect("May indicate the presence of ambiguity codes.");
		setFix("Change affected bases to Ns");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Only applies to core databases
	 */
	public void types() {
		
		setAppliesToType(DatabaseType.CORE);
		
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

		try {

			Statement stmt = con.createStatement();

			// if this query returns anything then something is wrong
			ResultSet rs = stmt.executeQuery("SELECT sr.seq_region_id, sr.name FROM seq_region sr, dna d WHERE sr.seq_region_id=d.seq_region_id AND d.sequence REGEXP '[^ATGCN]'");

			while (rs != null && rs.next()) {

				result = false;
				
				ReportManager.problem(this, con, String.format("%s (seq_region_id %s) has non GTACN sequence", rs.getString(2), rs.getLong(1)));

			} // while rs

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (result == true) {
			
			ReportManager.correct(this, con, "No entries in dna table have non-GTACN bases.");

		}
	
		return result;

	} // run

} // NonGTACNSequence
