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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class EGHighConfidence extends SingleDatabaseTestCase {

	public EGHighConfidence() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether HighConfidenceOrthologs pipeline has been run");
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		boolean result = true;
		int numOrthologyMLSS = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE type = 'ENSEMBL_ORTHOLOGUES'");
		if (numOrthologyMLSS > 0) {
			if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM homology WHERE description LIKE \"ortholog%\" AND is_high_confidence IS NOT NULL") == 0) {
				ReportManager.problem(this, dbre.getConnection(), "No orthologies have been annotated with a confidence level");
				result = false;
			}
		}
		return result;
	}

}
