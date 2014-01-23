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
 * Check for any coord_system.versions that are blank ('') - they should be NULL for various other things to work.
 */

public class BlankCoordSystemVersions extends SingleDatabaseTestCase {

	/**
	 * Create a new BlankCoordSystem testcase.
	 */
	public BlankCoordSystemVersions() {

		addToGroup("post_genebuild");
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		addToGroup("compara-ancestral");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
                addToGroup("post-projection");
		
		setDescription("Check for any coord_system.version that are blank ('') - they should be NULL.");
		setTeamResponsible(Team.GENEBUILD);// this is now inaccurate for funcgen usecase

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

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM coord_system WHERE version = ''");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " rows in coord_system have a blank versions - should be set to null");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank versions in coord_system");

		}

		return result;

	} // run

} // BlankCoordSystemVersions
