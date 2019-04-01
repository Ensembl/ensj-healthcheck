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
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * 
 */

public class GeneDescriptions extends SingleDatabaseTestCase {

	/**
	 * Create a new GeneDescriptions testcase.
	 */
	public GeneDescriptions() {

		setDescription("Check gene descriptions; correct capitalisation of UniprotKB/SwissProt");
		setPriority(Priority.AMBER);
		setEffect("Capitalisation of Uniprot will be wrong in gene descriptions.");
		setFix("Re-run xref system or manually fix affected xrefs.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

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

		result &= checkSwissprot(dbre);

		return result;

	} // run

	// --------------------------------------------------------------------------

	private boolean checkSwissprot(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(*) FROM gene WHERE description like '%Uniprot%' COLLATE latin1_general_cs");

		if (rows > 0) {
			ReportManager
					.problem(
							this,
							con,
							rows
									+ " descriptions have incorrect spelling/capitalisation of Uniprot attribution, should be \"UniProt\"");
			result = false;
		} else {
			ReportManager.correct(this, con,
					"All Uniprot attributions correct.");
		}

		return result;

	}
} // GeneDescriptions
