/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
 * Check for genes, transcripts and exons where the is_current column is
 * anything other than 1. Any value other than 1 will cause problems for Ensembl
 * databases.
 */
public class IsCurrent extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of IsCurrent.
	 */
	public IsCurrent() {

		setTeamResponsible(Team.CORE);
		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check for genes, transcripts and exons where the is_current column is anything other than 1.");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] types = { "gene", "transcript", "exon" };

		for (int i = 0; i < types.length; i++) {

			String table = types[i];

			int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table
					+ " WHERE is_current IS NULL OR is_current != 1");

			if (rows > 0) {

				ReportManager
						.problem(
								this,
								con,
								rows
										+ " "
										+ table
										+ "s have is_current set to null or some value other than 1");
				result = false;

			} else {

				ReportManager.correct(this, con, "All " + table
						+ "s have is_current=1");

			}

		}

		return result;

	} // run

} // IsCurrent
