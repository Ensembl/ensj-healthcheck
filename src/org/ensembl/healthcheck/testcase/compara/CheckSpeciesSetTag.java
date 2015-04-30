/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class CheckSpeciesSetTag extends AbstractComparaTestCase {

	/**
	 * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set
	 * of databases.
	 */
	public CheckSpeciesSetTag() {

		addToGroup("compara_genomic");
		setDescription("Check the content of the species_set_tag table");
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
	public boolean run(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;

		DatabaseRegistryEntry[] allSecondaryComparaDBs = DBUtils.getSecondaryDatabaseRegistry().getAll(DatabaseType.COMPARA);

		// ... check that we have one name tag for every MSA
		result &= checkNameTagForMultipleAlignments(comparaDbre);

		if (allSecondaryComparaDBs.length == 0) {
			result = false;
			ReportManager.problem(this,
					comparaDbre.getConnection(),
					"Cannot find the compara database in the secondary server. This check expects to find a previous version of the compara database for checking that all the *named* species_sets are still present in the current database.");
		}

		for (DatabaseRegistryEntry secondaryComparaDbre: allSecondaryComparaDBs) {
			// Check vs previous compara DB.
			result &= checkSetOfSpeciesSets(comparaDbre, secondaryComparaDbre);
		}

		return result;
	}

	public boolean checkSetOfSpeciesSets(DatabaseRegistryEntry primaryComparaDbre, DatabaseRegistryEntry secondaryComparaDbre) {

		boolean result = true;
		Connection con1 = primaryComparaDbre.getConnection();
		Connection con2 = secondaryComparaDbre.getConnection();

		// Get list of species_set sets in the secondary server
		String sql = "SELECT value, count(*) FROM species_set_tag WHERE tag = 'name' GROUP BY value";
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

	public boolean checkNameTagForMultipleAlignments(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (tableHasRows(con, "species_set_tag")) {

			// Get list of species_set sets in the secondary server
			String sql1 = "SELECT species_set_id, value FROM species_set_tag WHERE tag = 'name'";

			// Find all the species_set_ids for multiple alignments
			String sql2 = "SELECT species_set_id, name FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE"
				+ " class LIKE '%multiple_alignment%' OR class LIKE '%tree_alignment%' OR class LIKE '%ancestral_alignment%'";

			Map<Integer, String> allSetsWithAName = DBUtils.getSqlTemplate(con).queryForMap(sql1, new DefaultMapRowMapper<Integer, String>(Integer.class, String.class));
			Map<Integer, String> allSetsForMultipleAlignments = DBUtils.getSqlTemplate(con).queryForMap(sql2, new DefaultMapRowMapper<Integer, String>(Integer.class, String.class));

			for (Map.Entry<Integer, String> key_value : allSetsForMultipleAlignments.entrySet()) {
				if (!allSetsWithAName.containsKey(key_value.getKey())) {
					ReportManager.problem(this, con, "There is no name entry in species_set_tag for MSA \"" + key_value.getValue()+ "\".");
					result = false;
				}
			}

		} else {
			ReportManager.problem(this, con, "species_set_tag table is empty. There will be no aliases for multiple alignments");
			result = false;
		}

		return result;
	}

} // CheckSpeciesSetTag

