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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check compara genome_db table against core meta one.
 */

public class CheckGenomeDB extends AbstractComparaTestCase {

	// FIXME: I'm not sure EG would like to know about this list. Should we
	// tag them in the meta table ?
    private String[] orphanedSpeciesInMasterDB = {"spermophilus_tridecemlineatus", "anopheles_gambiae", "aedes_aegypti", "apis_mellifera", "caenorhabditis_briggsae"};

	/**
	 * Create a new instance of MetaCrossSpecies
	 */
	public CheckGenomeDB() {
		setDescription("Check that the properties of the genome_db table (taxon_id, assembly" +
				" and genebuild) correspond to the meta data in the core DB and vice versa.");
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

		result &= checkAssemblies(comparaDbre);
		result &= checkGenomeDB(comparaDbre);
		result &= checkCountIsZero(comparaDbre.getConnection(), "genome_db", "locator IS NOT NULL");
		return result;
	}


	public boolean checkAssemblies(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		Connection comparaCon = comparaDbre.getConnection();
		String comparaDbName = (comparaCon == null) ? "no_database" : DBUtils.getShortDatabaseName(comparaCon);

		// Get list of species with more than 1 default assembly
		String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE first_release IS NOT NULL AND last_release IS NULL"
			+ " GROUP BY name HAVING count(*) <> 1";
		List<String[]> data = DBUtils.getRowValuesList(comparaCon, sql);
		for (String[] line : data) {
			ReportManager.problem(this, comparaCon, "There are more than 1 current assembly for " + line[0]);
			result = false;
		}

		boolean is_master_db = isMasterDB(comparaCon);
		HashSet<String> allowedOrphanedSpecies = new HashSet<String>();

		// Get list of species with a non-default assembly
		if (!isMasterDB(comparaCon)) {
			sql = "SELECT DISTINCT name FROM genome_db WHERE first_release IS NULL OR last_release IS NOT NULL";
			data = DBUtils.getRowValuesList(comparaCon, sql);
			for (String[] line : data) {
				ReportManager.problem(this, comparaCon, comparaDbName + " There is at least one non-current assembly for " + line[0] + " (this should not happen in the release DB)");
			}
		} else {
			allowedOrphanedSpecies.addAll(Arrays.asList(orphanedSpeciesInMasterDB));
		}

		// Get list of species with no default assembly
		sql = "SELECT DISTINCT name FROM genome_db GROUP BY name HAVING SUM(first_release IS NOT NULL AND last_release IS NULL) = 0";
		data = DBUtils.getRowValuesList(comparaCon, sql);
		for (String[] line : data) {
			if (! allowedOrphanedSpecies.contains(line[0]) ) {
				ReportManager.problem(this, comparaCon, "There is no default assembly for " + line[0]);
				result = false;
			}
		}

		return result;
	}


	public boolean checkGenomeDB(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		Connection comparaCon = comparaDbre.getConnection();

		// Get list of species in compara
		Vector<Species> comparaSpecies = new Vector<Species>();
		String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE first_release IS NOT NULL AND last_release IS NULL"
			+ " AND name <> 'ancestral_sequences'";
		List<String[]> data = DBUtils.getRowValuesList(comparaCon, sql);
		for (String[] line : data) {
			Species species = Species.resolveAlias(line[0].toLowerCase().replace(' ', '_'));
			if (species.toString().equals("unknown")) {
				ReportManager.problem(this, comparaCon, "No species defined for " + line[0] + " in org.ensembl.healthcheck.Species");
			} else {
				comparaSpecies.add(species);
			}
		}

		Map<Species, DatabaseRegistryEntry> speciesMap = getSpeciesCoreDbMap(DBUtils.getMainDatabaseRegistry());

		boolean allSpeciesFound = true;
		for (Species species: comparaSpecies) {
			if (speciesMap.containsKey(species)) {
				Connection speciesCon = speciesMap.get(species).getConnection();

				/* Check production name */
				String sql1, sql2;
				sql1 = "SELECT \"" + species + "\", \"name\", name FROM genome_db" +
					" WHERE genome_db.name = \"" + species + "\" AND  first_release IS NOT NULL AND last_release IS NULL";
				sql2 = "SELECT \"" + species + "\", \"name\", meta_value FROM meta" +
					" WHERE meta_key = \"species.production_name\"";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check taxon_id */
				sql1 = "SELECT \"" + species + "\", \"taxon_id\", taxon_id FROM genome_db" +
					" WHERE genome_db.name = \"" + species + "\" AND  first_release IS NOT NULL AND last_release IS NULL";
				sql2 = "SELECT \"" + species + "\", \"taxon_id\", meta_value FROM meta" +
					" WHERE meta_key = \"species.taxonomy_id\"";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check assembly */
				sql1 = "SELECT \"" + species + "\", \"assembly\", assembly FROM genome_db" +
					" WHERE genome_db.name = \"" + species + "\" AND  first_release IS NOT NULL AND last_release IS NULL";
				sql2 = "SELECT \"" + species + "\", \"assembly\", version FROM coord_system" +
					" WHERE rank=1";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check genebuild */
				sql1 = "SELECT \"" + species + "\", \"genebuild\", genebuild FROM genome_db" +
					" WHERE genome_db.name = \"" + species + "\" AND  first_release IS NOT NULL AND last_release IS NULL";
				sql2 = "SELECT \"" + species + "\", \"genebuild\", meta_value FROM meta" +
					" WHERE meta_key = \"genebuild.start_date\"";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

			} else {
				ReportManager.problem(this, comparaCon, "No connection for " + species);
				allSpeciesFound = false;
			}
		}

		return result;
	}

} // CheckGenomeDB
