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
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check compara genome_db table against core meta one.
 */

public class CheckGenomeDB extends AbstractComparaTestCase {

	/**
	 * Create a new instance of MetaCrossSpecies
	 */
	public CheckGenomeDB() {
		setDescription("Check that the properties of the genome_db table (taxon_id, assembly"
				+ " and genebuild) correspond to the meta data in the core DB and vice versa.");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Check that the properties of the genome_db table (taxon_id, assembly and
	 * genebuild) correspond to the meta data in the core DB and vice versa. NB: A
	 * warning message is displayed if some dnafrags cannot be checked because there
	 * is not any connection to the corresponding core database.
	 * 
	 * @param comparaDbre
	 *            The database registry containing all the specified databases.
	 * @return true if the all the dnafrags are top_level seq_regions in their
	 *         corresponding core database.
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
		String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE first_release IS NOT NULL AND last_release IS NULL AND genome_component IS NULL"
				+ " GROUP BY name HAVING count(*) <> 1";
		List<String[]> data = DBUtils.getRowValuesList(comparaCon, sql);
		for (String[] line : data) {
			ReportManager.problem(this, comparaCon, "There are more than 1 current assembly for " + line[0]);
			result = false;
		}

		boolean is_master_db = isMasterDB(comparaCon);

		// Get list of species with a non-default assembly
		if (!isMasterDB(comparaCon)) {
			sql = "SELECT DISTINCT name FROM genome_db WHERE first_release IS NULL OR last_release IS NOT NULL";
			data = DBUtils.getRowValuesList(comparaCon, sql);
			for (String[] line : data) {
				ReportManager.problem(this, comparaCon,
						comparaDbName + " There is at least one non-current assembly for " + line[0]
								+ " (this should not happen in the release DB)");
				result = false;
			}
		} else {
			// Get list of species with no default assembly
			sql = "SELECT DISTINCT name FROM genome_db GROUP BY name HAVING SUM(first_release IS NOT NULL AND last_release IS NULL) = 0";
			data = DBUtils.getRowValuesList(comparaCon, sql);
			for (String[] line : data) {
				ReportManager.info(this, comparaCon, "There is no default assembly for " + line[0]);
			}
		}

		return result;
	}

	public boolean checkGenomeDB(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		Connection comparaCon = comparaDbre.getConnection();

		for (GenomeEntry genomeEntry : getAllGenomes(comparaDbre, DBUtils.getMainDatabaseRegistry())) {
			String species = genomeEntry.getName();
			if (genomeEntry.getCoreDbre() != null) {

				Integer genome_db_id = genomeEntry.getGenomeDBID();

				Connection speciesCon = genomeEntry.getCoreDbre().getConnection();
				Integer species_id = genomeEntry.getSpeciesID();

				/* Check production name */
				result &= checkGenomeDBField(comparaCon, speciesCon, species_id, species, genome_db_id, "name",
						"species.production_name");

				/* Check taxon_id */
				result &= checkGenomeDBField(comparaCon, speciesCon, species_id, species, genome_db_id, "taxon_id",
						"species.taxonomy_id");

				/* Check assembly */
				String sql1 = "SELECT \"" + species
						+ "\", \"assembly\", CONCAT(assembly,\"\") FROM genome_db WHERE genome_db_id = " + genome_db_id;
				String sql2 = "SELECT \"" + species + "\", \"assembly\", version FROM coord_system WHERE species_id = "
						+ species_id + " ORDER BY rank LIMIT 1";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check genebuild */
				result &= checkGenomeDBField(comparaCon, speciesCon, species_id, species, genome_db_id, "genebuild",
						"genebuild.start_date");

			} else {
				ReportManager.problem(this, comparaCon, "No connection for " + species);
				result = false;
			}
		}

		return result;
	}

	private boolean checkGenomeDBField(Connection comparaCon, Connection speciesCon, Integer species_id, String species,
			Integer genome_db_id, String fieldName, String metaKey) {
		String sql0 = "SELECT \"" + species + "\", \"" + fieldName + "\", ";
		String sql1 = sql0 + fieldName + " FROM genome_db WHERE genome_db_id = " + genome_db_id;
		String sql2 = sql0 + "CONCAT(meta_value,\"\") FROM meta WHERE meta_key = \"" + metaKey + "\" AND species_id = "
				+ species_id;
		return compareQueries(comparaCon, sql1, speciesCon, sql2);
	}

} // CheckGenomeDB
