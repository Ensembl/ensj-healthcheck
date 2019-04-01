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
 * Check that only genes have projected xrefs.
 */

public class ProjectedXrefGenes extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProjectedXrefGenes() {

		setDescription("Check that only genes have projected xrefs");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * Run the test for each database.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql = "SELECT COUNT(*) FROM external_db e, xref x, object_xref ox, transcript t WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.ensembl_object_type='Transcript' AND ox.ensembl_id=t.transcript_id AND x.info_type='PROJECTION'";

		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			ReportManager.problem(this, con, "There are " + rows + " projected xrefs linked to transcripts however only genes and translations should have them.");
			result = false;

		} else {

			ReportManager.correct(this, con, "No projected xrefs associated with transcripts");

		}

		return result;
	}

} // ProjectedXrefGenes
