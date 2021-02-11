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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the seq_region_strand is +1 or -1 for several tables.
 */

public class Strand extends SingleDatabaseTestCase {

	private String[] tables = { "prediction_transcript", "prediction_exon", "transcript", "gene", "exon" };

	/**
	 * Create a new Strand testcase.
	 */
	public Strand() {

		setDescription("Check that seq_region_strand is +/-1 in several tables.");
		setTeamResponsible(Team.GENEBUILD);

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

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			String sql = "SELECT COUNT(*) FROM " + table + " WHERE seq_region_strand NOT IN (1,-1)";
			Connection con = dbre.getConnection();
			int rows = DBUtils.getRowCount(con, sql);
			if (rows == 0) {
				ReportManager.correct(this, con, "All seq_region_strand in " + table + " are 1 or -1");
			} else if (rows > 0) {
				ReportManager.problem(this, con, rows + " rows in " + table + " have seq_region_strand not equal to 1 or -1");
				result = false;
			}
		}

		return result;

	} // run

} // Strand
