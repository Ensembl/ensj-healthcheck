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
 * Check for any retrotransposed transcripts that have translations (shouldn't be any).
 */

public class Retrotransposed extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public Retrotransposed() {

		setDescription("Check for any retrotransposed transcripts that have translations (shouldn't be any).");
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

		Connection con = dbre.getConnection();
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM transcript t, translation tln WHERE t.transcript_id=tln.transcript_id AND t.biotype = 'retrotransposed'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " transcripts with biotype 'retrotransposed' have translations.");
			result = false;

		} else {

			ReportManager.correct(this, con, "No retrotransposed transcripts have translations.");

		}

		return result;

	} // run

} // Retrotransposed
