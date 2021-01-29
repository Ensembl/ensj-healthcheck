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
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for hit_names that aren't formatted correctly.
 */
public class HitNameFormat extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance.
	 */
	public HitNameFormat() {

		setDescription("Check that there are no incorrectly formatted hit_names");
		setPriority(Priority.AMBER);
		setFix("Manually fix affected values.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database registry entry to be checked.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] tables = { "dna_align_feature", "protein_align_feature",
				"protein_feature" };

		for (String table : tables) {

			int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table
					+ " WHERE hit_name LIKE '%|%'");

			if (rows > 0) {
				ReportManager
						.problem(
								this,
								con,
								rows
										+ " "
										+ table
										+ "s appear to have incorrectly formatted hit_names (containing a '|' symbol)");
				ReportManager
						.problem(
								this,
								con,
								"USEFUL SQL: SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(hit_name,'|',-2),'|',1) FROM "
										+ table
										+ " WHERE hit_name LIKE 'gi%|%'");
				ReportManager
						.problem(
								this,
								con,
								"UPDATE "
										+ table
										+ " SET hit_name = SUBSTRING_INDEX(SUBSTRING_INDEX(hit_name,'|',-2),'|',1) WHERE hit_name LIKE 'gi|%'");
				result = false;
			} else {
				ReportManager.correct(this, con, "All " + table
						+ "s have correctly formatted hit_names");
			}

		}

		return result;

	}

} // HitNameFormat

