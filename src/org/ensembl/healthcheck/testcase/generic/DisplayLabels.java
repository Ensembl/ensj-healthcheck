/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that certain tables have display_labels set.
 */

public class DisplayLabels extends SingleDatabaseTestCase {

	/**
	 * Create a new DisplayLabels testcase.
	 */
	public DisplayLabels() {

		setDescription("Check that certain tables have display_labels set");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
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

		result &= checkNoNulls(con, "prediction_transcript", "display_label");
		result &= checkNoNulls(con, "simple_feature", "display_label");
		result &= checkNoNulls(con, "xref", "display_label");

		result &= checkNoBadCharacters(con, "xref", "display_label");
		return result;

	} // run

	// ----------------------------------------------------------------------

} // DisplayLabels
