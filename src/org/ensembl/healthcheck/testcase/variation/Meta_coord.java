/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks the meta_coord table to make sure it is OK. Only one meta table at a time is done here; checks for the consistency of the
 * meta_coord table across species are done in MetaCrossSpecies.
 */
public class Meta_coord extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckMetaDataTableTestCase
	 */
	public Meta_coord() {

		setDescription("Check that the meta_coord table contains the right entries for the different variation species");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Check various aspects of the meta_coord table.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {
		boolean result = true;

		Connection con = dbre.getConnection();
		String[] tables = { "variation_feature", "compressed_genotype_region", "transcript_variation", "structural_variation_feature", "read_coverage", "phenotype_feature" };
		
		try {
			/*
			 * Will check the presence of the variation_feature, transcript_variation, compressed_genotype
			 * and variation_group_feature entries in the meta_coord, when data present in those tables
			 */
			for (int i = 0; i < tables.length; i++) {
				int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + tables[i]); // count if table has data
				if (rows > 0) {
					// the meta_coord table should contain entry
					result &= checkKeysPresent(con, tables[i]);
				}
			}
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
			result = false;
		}
		if (result) {
			// if there were no problems, just inform for the interface to pick the HC
			ReportManager.correct(this, con, "MetaCoord table healthcheck passed without any problem");
		}
		return result;
	} // run

	// --------------------------------------------------------------

	private boolean checkKeysPresent(Connection con, String tableName) {

		boolean result = true;

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE table_name='" + tableName + "'");
		if (rows == 0) {
			result = false;
			ReportManager.problem(this, con, "No entry in meta_coord table for " + tableName);
		} else {
			ReportManager.correct(this, con, tableName + " entry present");
		}

		return result;
	}

	// --------------------------------------
}
