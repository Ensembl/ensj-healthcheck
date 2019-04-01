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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for repeat features that have repeat_start > repeat_end, or start or end < 1. Note that seq_region_start/end are checked in
 * FeatureCoords.
 */

public class RepeatFeature extends SingleDatabaseTestCase {

	/**
	 * Create a new RepeatFeature testcase.
	 */
	public RepeatFeature() {

		setDescription("Check that repeat_start and repeat_end in repeat_feature make sense.");
		setTeamResponsible(Team.GENEBUILD);

	}

        /**
         * This test only applies to core databases.
         */
        public void types() {

                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

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

		// check that start < end
		String sql = "SELECT COUNT(*) FROM repeat_feature WHERE repeat_start > repeat_end";
		Connection con = dbre.getConnection();
		int rows = DBUtils.getRowCount(con, sql);
		if (rows == 0) {
			ReportManager.correct(this, con, "All repeat_feature_start < repeat_feature end");
		} else if (rows > 0) {
			ReportManager.problem(this, con, rows + " rows in repeat_feature have repeat_start > repeat_end");
			result = false;
		}

		// check start and end not < 1
		sql = "SELECT COUNT(*) FROM repeat_feature WHERE repeat_start < 1 OR repeat_end < 1";
		rows = DBUtils.getRowCount(con, sql);
		if (rows == 0) {
			ReportManager.correct(this, con, "All repeat_feature repeat_start and repeat_end < 1");
		} else if (rows > 0) {
			ReportManager.problem(this, con, rows + " rows in repeat_feature have repeat_start or repeat_end < 1");
			result = false;
		}
		return result;

	} // run

} // RepeatFeature
