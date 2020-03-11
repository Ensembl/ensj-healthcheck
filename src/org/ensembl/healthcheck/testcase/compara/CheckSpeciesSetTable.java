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
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken entries in the
 * method_link_species_set table
 */

public class CheckSpeciesSetTable extends AbstractComparaTestCase {

	public CheckSpeciesSetTable() {
		setDescription("Check for broken entries in the method_link_species_set table.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		boolean result = true;

		/* Check that species_set_tag refers to existing species sets */
		result &= checkForOrphans(con, "species_set_tag", "species_set_id", "species_set", "species_set_id");

		/* Check uniqueness of species_set entries */
		int numOfDuplicatedSpeciesSets = DBUtils.getRowCount(con,
				"SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
				"SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
		if (numOfDuplicatedSpeciesSets > 0) {
			ReportManager.problem(this, con, "FAILED species_set table contains " + numOfDuplicatedSpeciesSets + " duplicated entries");
			ReportManager.problem(this, con, "USEFUL SQL: SELECT gdbs, count(*) num, GROUP_CONCAT(species_set_id) species_set_ids FROM ("+
					"SELECT species_set_id, GROUP_CONCAT(genome_db_id) gdbs FROM species_set GROUP by species_set_id) t1 GROUP BY gdbs HAVING COUNT(*)>1");
			result = false;
		}

		return result;

	}

} // CheckSpeciesSetTable
