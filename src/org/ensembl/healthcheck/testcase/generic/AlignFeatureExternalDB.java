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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all DNA and protein align features have an external_db_id set.
 */

public class AlignFeatureExternalDB extends SingleDatabaseTestCase {

	/**
	 * Create a new AlignFeatureExteralDB testcase.
	 */
	public AlignFeatureExternalDB() {

		setDescription("Check that all DNA and protein align features have an external_db_id set.");
		setPriority(Priority.AMBER);
		setEffect("Needed for web display.");
		setFix("Run ensembl-personal/genebuilders/scripts/assign_external_db_ids.pl to set values.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {
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

		Connection con = dbre.getConnection();

		String[] tables = { "protein_align_feature", "dna_align_feature" };

		for (int i = 0; i < tables.length; i++) {

			result &= checkNoNulls(con, tables[i], "external_db_id");
			result &= checkNoZeroes(con, tables[i], "external_db_id");

		}

		return result;

	} // run

}
