/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * Check for blank or null versions in the xref table.
 */

public class XrefVersions extends SingleDatabaseTestCase {

	/**
	 * Create a new XrefVersions testcase.
	 */
	public XrefVersions() {

		addToGroup("post_genebuild");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");
		
		setDescription("Check for blank or null versions in the xref table.");
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
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref WHERE version='' OR version IS NULL");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " rows in xref have blank or null versions.");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank/null versions in xref");

		}

		return result;

	} // run

} // XrefVersions
