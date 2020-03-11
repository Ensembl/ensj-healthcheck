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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that there are certain types in repeat_types.
 */

public class RepeatConsensus extends SingleDatabaseTestCase {

	/**
	 * Create a new RepeatConsensus testcase.
	 */
	public RepeatConsensus() {

		setDescription("Check there certain types in repeat_consensus.repeat_type.");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This test only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

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
		String query = "SELECT COUNT(*) FROM repeat_consensus WHERE repeat_type = ''";
		if (dbre.getType() != DatabaseType.SANGER_VEGA) {// for sangervega, simple is fine
			query += " OR repeat_type ='Simple'";
		}
		int rows = DBUtils.getRowCount(con, query);

		if (rows > 0) {
			String report = "repeat_consensus table has " + rows + " rows of repeat_type empty";
			if (dbre.getType() != DatabaseType.SANGER_VEGA) {// for sangervega, simple is fine
				report += " OR 'Simple'";
			}
			ReportManager.problem(this, con, report);
			if (dbre.getType() == DatabaseType.SANGER_VEGA) {
				ReportManager.problem(this, con, "This probably means the .../sanger-plugins/vega/utils//vega_repeat_libraries.pl script was not run.");
			} else {
				ReportManager.problem(this, con, "This probably means the ensembl/misc-scripts/repeats/repeat-types.pl script was not run.");
			}
			result = false;

		} else {

			ReportManager.correct(this, con, "repeat_consensus appears to have valid repeat_types");
		}

		return result;

	} // run

} // RepeatConsensus
