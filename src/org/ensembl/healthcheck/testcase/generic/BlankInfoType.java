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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for any xref.info_type that are blank ('') - they should be NULL for various other things to work.
 */

public class BlankInfoType extends SingleDatabaseTestCase {

	/**
	 * Create a new BlankInfoType testcase.
	 */
	public BlankInfoType() {

		setDescription("Check for any xref.info_type that are blank ('') or NULL - they should be NONE for various other things to work.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
		

	}

	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
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

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref WHERE info_type = ''");
		rows += DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref WHERE info_type is NULL");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " rows in xref have a blank info_type or null entry - should be set to NONE. See patch 67_68_b");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank info_types in xref");

		}

		return result;

	} // run

} // BlankInfoType
