/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check that first_release and last_release are correctly set and all
 * consistent
 */

public class CheckFirstLastRelease extends AbstractComparaTestCase {

	public CheckFirstLastRelease() {
		setDescription("Check that first_release and last_release are correctly set and all consistent");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Check that the properties of the genome_db table (taxon_id, assembly and genebuild)
	 * correspond to the meta data in the core DB and vice versa.
	 * NB: A warning message is displayed if some dnafrags cannot be checked because
	 * there is not any connection to the corresponding core database.
	 * 
	 * @param comparaDbre
	 *          The database registry containing all the specified databases.
	 * @return true if the all the dnafrags are top_level seq_regions in their corresponding
	 *    core database.
	 */
	public boolean run(final DatabaseRegistryEntry comparaDbre) {

		boolean result = true;

		result &= checkNullSanity(comparaDbre);
		result &= checkReleaseConsistency(comparaDbre);
		return result;
	}


	public boolean checkNullSanity(DatabaseRegistryEntry comparaDbre) {

		Connection comparaCon = comparaDbre.getConnection();
		boolean is_master_db = isMasterDB(comparaCon);
		
		boolean result = true;

		// NULL + non-NULL is forbiddden
		result &= checkCountIsZero(comparaCon, "genome_db", "first_release IS NULL AND last_release IS NOT NULL");
		result &= checkCountIsZero(comparaCon, "species_set_header", "first_release IS NULL AND last_release IS NOT NULL");
		result &= checkCountIsZero(comparaCon, "method_link_species_set", "first_release IS NULL AND last_release IS NOT NULL");

		if (is_master_db) {
			// In the master database, last_release cannot be set in the future (incl. the current release)
			result &= checkCountIsZero(comparaCon, "genome_db", "last_release IS NOT NULL AND last_release >= " + DBUtils.getRelease());
			result &= checkCountIsZero(comparaCon, "species_set_header", "last_release IS NOT NULL AND last_release >= " + DBUtils.getRelease());
			result &= checkCountIsZero(comparaCon, "method_link_species_set", "last_release IS NOT NULL AND last_release >= " + DBUtils.getRelease());

		} else {
			// last_release should be NULL in the release database
			result &= checkCountIsZero(comparaCon, "genome_db", "last_release IS NOT NULL");
			result &= checkCountIsZero(comparaCon, "species_set_header", "last_release IS NOT NULL");
			result &= checkCountIsZero(comparaCon, "method_link_species_set", "last_release IS NOT NULL");

			// NULL + NULL is only allowed in the master database
			result &= checkCountIsZero(comparaCon, "genome_db", "first_release IS NULL AND last_release IS NULL");
			result &= checkCountIsZero(comparaCon, "species_set_header", "first_release IS NULL AND last_release IS NULL");
			result &= checkCountIsZero(comparaCon, "method_link_species_set", "first_release IS NULL AND last_release IS NULL");
		}


		return result;
	}

	public boolean checkReleaseConsistency(DatabaseRegistryEntry comparaDbre) {

		Connection comparaCon = comparaDbre.getConnection();
		boolean is_master_db = isMasterDB(comparaCon);
		
		boolean result = true;

		// NOTE: The queries are not symmetrical ! t1 constraints the first/last_release of t2

		// Example with t1=genome_db and t2=species_set_header
		String[] conditions = {
			// The species-set has been released but the GenomeDB hasn't
			"t1.first_release IS NULL AND t2.first_release IS NOT NULL",
			// The GenomeDB is not current any more but the species-set still is
			"t1.last_release IS NOT NULL AND t2.first_release IS NOT NULL AND t2.last_release IS NULL",
			// The species-set has been released before the GenomeDB
			"t1.first_release IS NOT NULL AND t2.first_release IS NOT NULL AND t2.first_release < t1.first_release",
			// The GenomeDB has been retired before the species-set
			"t1.last_release IS NOT NULL AND t2.last_release IS NOT NULL AND t2.last_release > t1.last_release",
		};

		String[] tableJoins = {
			"genome_db t1 JOIN species_set USING (genome_db_id) JOIN species_set_header t2 USING (species_set_id)",
			"genome_db t1 JOIN species_set USING (genome_db_id) JOIN method_link_species_set t2 USING (species_set_id)",
			"species_set_header t1 JOIN method_link_species_set t2 USING (species_set_id)",
		};

		for (String t : tableJoins) {
			for (String c : conditions) {
				result &= checkCountIsZero(comparaCon, t, c);
			}
		}

		return result;
	}

} // CheckFirstLastRelease
