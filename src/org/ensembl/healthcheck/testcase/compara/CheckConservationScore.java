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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks the conservation_score table
 */

public class CheckConservationScore extends SingleDatabaseTestCase {

	/**
	 * Create an CheckConservationScore that applies to a specific set of
	 * databases.
	 */
	public CheckConservationScore() {
		setDescription("Check the conservation_score table in ensembl_compara databases.");
		setTeamResponsible(Team.COMPARA);
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

		Connection con = dbre.getConnection();

		/**
		 * Get all method_link_species_set_ids for method_link type of
		 * GERP_CONSERVATION_SCORE
		 */
		String[] method_link_species_set_ids = DBUtils.getColumnValues(con, "SELECT method_link_species_set_id FROM method_link_species_set LEFT JOIN method_link USING (method_link_id) WHERE type=\"GERP_CONSERVATION_SCORE\" OR class LIKE \"ConservationScore%\"");

		if (method_link_species_set_ids.length > 0) {

			/**
			 * Check have entries in conservation_score table
			 */
			if (!tableHasRows(con, "conservation_score")) {
				ReportManager.problem(this, con, "FAILED: Database contains entry in the method_link_species_set table but the conservation_score table is empty");
				return false;
			}

			boolean result = true;
			for(String mlss_id : method_link_species_set_ids) {

				// Get the mlss_id for the associated multiple alignment
				String multi_align_mlss_id = DBUtils.getRowColumnValue(con, "SELECT value FROM method_link_species_set_tag WHERE tag=\"msa_mlss_id\" AND method_link_species_set_id=" + mlss_id);
				if (multi_align_mlss_id == "") {
					ReportManager.problem(this, con, "There is no msa_mlss_id tag for the GERP mlss" + mlss_id + "\n");
				} else {
					String has_scores_sql = "SELECT 1 FROM genomic_align_block JOIN conservation_score USING (genomic_align_block_id) WHERE method_link_species_set_id = " + multi_align_mlss_id + " LIMIT 1";
					int rows = DBUtils.getRowCount(con, has_scores_sql);
					if (rows > 0) {
						ReportManager.correct(this, con, "Some scores for alignment mlss_id=" + multi_align_mlss_id);
					} else {
						ReportManager.problem(this, con, "FAILED conservation_score: no scores for alignment mlss_id=" + multi_align_mlss_id);
						result = false;
					}
				}
			}

			return result;

		} else if (tableHasRows(con, "conservation_score")) {
			ReportManager.problem(this, con, "FAILED: Database contains data in the conservation_score table but no corresponding entry in the method_link_species_set table.");
			return false;

		} else {
			ReportManager.correct(this, con, "NO conservation scores in this database");
			return true;
		}
	}

} // CheckConservationScore
