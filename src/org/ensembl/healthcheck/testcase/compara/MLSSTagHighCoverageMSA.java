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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case for the "high_coverage_mlss_id" MethodLinkSpeciesSetTag entries
 */

public class MLSSTagHighCoverageMSA extends SingleDatabaseTestCase {

	public MLSSTagHighCoverageMSA() {
		setDescription("Tests that proper entries are in method_link_species_set_tag.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "method_link_species_set_tag")) {
			result = false;
			ReportManager.problem(this, con, "method_link_species_set_tag table not present");
			return result;
		}

		result &= checkLowCoverageMLSSAreLinkedToHighCoverageMLSS(dbre);

		return result;
	}

	public boolean checkLowCoverageMLSSAreLinkedToHighCoverageMLSS(final DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		String sql = "SELECT mlss1.method_link_species_set_id, tag, value, mlss2.method_link_species_set_id, ml2.type"
			+ " FROM method_link_species_set mlss1 JOIN method_link ml1 USING (method_link_id)"
			+ " LEFT JOIN method_link_species_set_tag mlsst ON mlsst.method_link_species_set_id = mlss1.method_link_species_set_id AND tag = 'high_coverage_mlss_id'"
			+ " LEFT JOIN (method_link_species_set mlss2 JOIN method_link ml2 USING (method_link_id)) ON value = mlss2.method_link_species_set_id"
			+ " WHERE ml1.type = 'EPO_LOW_COVERAGE'";

		List<String[]> all_rows = DBUtils.getRowValuesList(con, sql);
		boolean result = true;
		for (String[] row : all_rows) {
			// Check all the potential errors
			if (row[1] == null) {
				ReportManager.problem(this, con, String.format("The MLSS ID %s is missing its 'high_coverage_mlss_id' tag", row[0]));
			} else if (row[2] == null) {
				ReportManager.problem(this, con, String.format("The 'high_coverage_mlss_id' tag for MLSS ID %s has a NULL value", row[0]));
			} else if (row[3] == null) {
				ReportManager.problem(this, con, String.format("The value of the 'high_coverage_mlss_id' tag for MLSS ID %s does not link to a valid MLSS ID: '%s'", row[0], row[2]));
			} else if (!row[4].equals("EPO")) {
				ReportManager.problem(this, con, String.format("The value of the 'high_coverage_mlss_id' tag for MLSS ID %s does not link to a 'EPO' MLSS but '%s' (ID %s)", row[0], row[4], row[3]));
			} else {
				// This row is correct: everything is non-NULL, and the EPO_LOW_COVERAGE mlss is linked to a EPO
				continue;
			}
			result = false;
		}
		return result;
	}
}
