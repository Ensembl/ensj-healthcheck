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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;

/**
 * Check that the main species-set names are similar to the previous
 * database
 */

public class CompareMSANames extends AbstractComparaTestCase {

	/**
	 * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set
	 * of databases.
	 */
	public CompareMSANames() {
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

		DatabaseRegistryEntry lastReleaseDbre = getLastComparaReleaseDbre(comparaDbre);

		if (lastReleaseDbre == null) {
			ReportManager.problem(this,
					comparaDbre.getConnection(),
					"Cannot find the previous compara database in the secondary server. This check expects to find a previous version of the compara database for checking that all the *named* species_sets are still present in the current database.");
			return false;
		}

		boolean result = true;
		result &= checkSetOfSpeciesSets(comparaDbre, lastReleaseDbre);
		return result;
	}

	public boolean checkSetOfSpeciesSets(DatabaseRegistryEntry primaryComparaDbre, DatabaseRegistryEntry secondaryComparaDbre) {

		boolean result = true;
		Connection con1 = primaryComparaDbre.getConnection();
		Connection con2 = secondaryComparaDbre.getConnection();

		// Get list of species_set sets in the secondary server
		String sql = "SELECT species_set_header.name, COUNT(*) "
			+ " FROM method_link_species_set JOIN method_link USING (method_link_id) JOIN species_set_header USING (species_set_id) "
			+ " WHERE (class LIKE '%multiple_alignment%' OR class LIKE '%tree_alignment%' OR class LIKE '%ancestral_alignment%') AND species_set_header.name != ''"
			+ " GROUP BY species_set_header.name";
		Map<String,Integer> primarySets   = DBUtils.getSqlTemplate(con1).queryForMap(sql, new DefaultMapRowMapper<String, Integer>(String.class, Integer.class));
		Map<String,Integer> secondarySets = DBUtils.getSqlTemplate(con2).queryForMap(sql, new DefaultMapRowMapper<String, Integer>(String.class, Integer.class));

		for (Map.Entry<String, Integer> key_value : secondarySets.entrySet()) {
			String key = key_value.getKey();
			Integer primaryValue = primarySets.get(key);
			Integer secondaryValue = key_value.getValue();
			if (!primarySets.containsKey(key)) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is missing (it appears %d time(s) in %s", key, secondaryValue, DBUtils.getShortDatabaseName(con2)));
				result = false;
			} else if (primaryValue < secondaryValue) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is present only %d times instead of %d as in %s", key, primaryValue, secondaryValue, DBUtils.getShortDatabaseName(con2)));
				result = false;
			}
		}
		for (Map.Entry<String, Integer> key_value : primarySets.entrySet()) {
			String next = key_value.getKey();
			if (!secondarySets.containsKey(next)) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is new (compared to %s)", next, DBUtils.getShortDatabaseName(con2)));
				result = false;
			}
		}

		return result;
	}

} // CompareMSANames

