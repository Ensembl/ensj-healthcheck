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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all transcripts of genes of protein coding genes translate.
 */

public class TranscriptsTranslate extends SingleDatabaseTestCase {

	/**
	 * Create a new TranscriptsTranslate testcase.
	 */
	public TranscriptsTranslate() {

		setDescription("Check that all protein_coding transcripts translate");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
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

		String sql = "SELECT COUNT(*) FROM transcript tr LEFT JOIN translation t ON t.transcript_id=tr.transcript_id WHERE t.translation_id IS NULL AND tr.biotype=\'protein_coding\'";

		int rows = DBUtils.getRowCount(con, sql);
		if (rows != 0) {

			ReportManager.problem(this, con, rows + " protein_coding transcript(s) do not have translations.");
			result = false;

		} else {

			ReportManager.correct(this, con, "All protein_coding transcripts have translations");

		}

		return result;

	} // run

} // TranscriptsTranslate
