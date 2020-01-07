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
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for newlines & tabs in gene descriptions - causes problems for TSV dumping.
 */
public class DescriptionNewlines extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of DescriptionNewlines.
	 */
	public DescriptionNewlines() {

		setDescription("Check for newlines and tabs in gene descriptions.");

		setPriority(Priority.AMBER);
		setEffect("Will cause problems for TSV file dumping and importing");
		setFix("Remove newlines and tabs; useful SQL for identifying affected genes:\nSELECT gene_id, stable_id, description FROM gene WHERE (LOCATE('\\n', description) > 0 or LOCATE('\\t', description) > 0);");
		setTeamResponsible(Team.CORE);

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

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM gene WHERE (LOCATE('\n', description) > 0 OR LOCATE('\t', description) > 0)");

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes have newlines and/or tabs in their descriptions");

		} else {

			ReportManager.correct(this, con, "No genes have newlines or tabs in their descriptions");

		}
		return result;

	} // run

	// -------------------------------------------------------------------------

} // DescriptionNewlines
