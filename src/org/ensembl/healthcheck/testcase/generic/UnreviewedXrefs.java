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
 * Check for Uniprot xrefs that have "Unreviewed" as the primary DB accession.
 */

public class UnreviewedXrefs extends SingleDatabaseTestCase {

	/**
	 * Create a new UnreviewedXrefs testcase.
	 */
	public UnreviewedXrefs() {

		setDescription("Check for Uniprot xrefs that have 'Unreviewed' as the primary DB accession.");
		setPriority(Priority.AMBER);
		setEffect("Affected xrefs will have broken hyperlinks, also problems for downstream pipelines.");
		setFix("Re-run xref system or manually fix affected xrefs.");
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

		// --------------------------------
		// MGI - dbprimary_acc should have MGI: prefix
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name LIKE 'UniProt%' AND x.dbprimary_acc='Unreviewed';");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " UniProt xrefs have \'Unreviewed\' as the primary accession.");
			result = false;
		} else {
			ReportManager.correct(this, con, "No Uniprot xrefs have \'Unreviewed\' as the accession.");
		}

		return result;

	} // run

} // UnreviewedXrefs
