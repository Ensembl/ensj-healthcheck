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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;

/**
 * Check that all the species-sets are named
 */

public class CheckMSANames extends AbstractComparaTestCase {

	/**
	 * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set
	 * of databases.
	 */
	public CheckMSANames() {
		setDescription("Check the content of the species_set_tag table");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Run the test.
	 * 
	 * @param comparaDbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;

		// ... check that we have one name tag for every MSA
		result &= checkNameTagForMultipleAlignments(comparaDbre);

		return result;
	}

	public boolean checkNameTagForMultipleAlignments(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql = "SELECT method_link_species_set_id, species_set_id, method_link_species_set.name "
			+ " FROM method_link_species_set JOIN method_link USING (method_link_id) JOIN species_set_header USING (species_set_id) "
			+ " WHERE (class LIKE '%multiple_alignment%' OR class LIKE '%tree_alignment%' OR class LIKE '%ancestral_alignment%') AND species_set_header.name = ''";

		List<String[]> data = DBUtils.getRowValuesList(con, sql);
		for (String[] line : data) {
			ReportManager.problem(this, con, "MLSS " + line[0] + " -- '" + line[2] + "' (species_set_id " + line[1] + ") has no name");
			result = false;
		}
		return result;
	}

} // CheckMSANames

