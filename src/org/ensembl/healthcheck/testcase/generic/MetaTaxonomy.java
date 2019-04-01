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


package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the meta entries related to the taxonomy are in sync with the
 * NCBI database
 **/

public class MetaTaxonomy extends SingleDatabaseTestCase {

	final protected String taxonomyDbName = "ncbi_taxonomy";

	public MetaTaxonomy() {
		setDescription("Check that the meta entries related to the taxonomy are in sync with the NCBI database");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}


	public boolean run(DatabaseRegistryEntry coreDbre) {

		// Find the NCBI database
		DatabaseRegistryEntry ncbiDbre = findTaxonomyDatabase(coreDbre, DBUtils.getMainDatabaseRegistry());

		if (ncbiDbre != null) {
			boolean result = true;
			for (int species_id : coreDbre.getSpeciesIds()) {
				// Extract the taxon_id of this genome
				String sql_taxon_id = "SELECT meta_value FROM meta WHERE meta_key = \"species.taxonomy_id\" AND species_id = " + species_id;
				String taxon_id = DBUtils.getRowColumnValue(coreDbre.getConnection(), sql_taxon_id);
				// Check the meta keys
				result &= checkScientificName(ncbiDbre, coreDbre, species_id, taxon_id);
				result &= checkFirstClassificationName(ncbiDbre, coreDbre, species_id, taxon_id);
				result &= checkRestOfClassification(ncbiDbre, coreDbre, species_id, taxon_id);
			}
			return result;
		} else {
			return false;
		}
	}


	/**
	 * Scan the DatabaseRegistry and find the entry of the NCBI Taxonomy
	 * database.
	 * For some reason, the type of the taxonomy database is "unknown", so
	 * we can't compare {@code entry.getType()} to {@code
	 * DatabaseType.NCBI_TAXONOMY}
	 */
	protected DatabaseRegistryEntry findTaxonomyDatabase(final DatabaseRegistryEntry coreDbre, final DatabaseRegistry dbr) {
		for (DatabaseRegistryEntry entry : dbr.getAllEntries()) {
			if (entry.getName().equals(taxonomyDbName)) {
				return entry;
			}
		}
		ReportManager.problem(this, coreDbre.getConnection(), "No ncbi_taxonomy database found");
		return null;
	}


	/**
	 * Helper method that compares a name coming from the core database to
	 * the expected name from the NCBI taxonomy database.
	 * This method is used in all the comparisons of this module and
	 * provides a consistent way of reporting the errors
	 */
	protected boolean compareNames(final DatabaseRegistryEntry coreDbre, final String name_description, int species_id, String taxon_id, final String name_core_db, final String name_taxonomy_db) {
		/* Check scientific name in compara and core meta */
		if (name_core_db.equals(name_taxonomy_db)) {
			ReportManager.info(this, coreDbre.getConnection(), name_description + " of species_id=" + species_id + " is the same as expected (" + name_core_db + ") based on the taxonomy id (" + taxon_id + ")");
			return true;
		} else {
			ReportManager.problem(this, coreDbre.getConnection(), name_description + " of species_id=" + species_id + " (" + name_core_db + ") differs from what is expected (" + name_taxonomy_db + ") based on the taxonomy id (" + taxon_id + ")");
			return false;
		}
	}


	/**
	 * Check that the scientific_name meta key is the same as expected
	 * based on the taxon_id
	 */
	public boolean checkScientificName(final DatabaseRegistryEntry ncbiDbre, final DatabaseRegistryEntry coreDbre, int species_id, String taxon_id) {

		String sql_core_db = "SELECT meta_value FROM meta WHERE meta_key = \"species.scientific_name\" AND species_id = " + species_id;
		String name_core_db = DBUtils.getRowColumnValue(coreDbre.getConnection(), sql_core_db);

		String sql_taxonomy_db = "SELECT name FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + taxon_id;
		String name_taxonomy_db = DBUtils.getRowColumnValue(ncbiDbre.getConnection(), sql_taxonomy_db);

		return compareNames(coreDbre, "Scientific name", species_id, taxon_id, name_core_db, name_taxonomy_db);
	}


	/**
	 * Check that the first element of the classification array is the
	 * same as the scientific name
	 */
	public boolean checkFirstClassificationName(final DatabaseRegistryEntry ncbiDbre, final DatabaseRegistryEntry coreDbre, int species_id, String taxon_id) {
		String sql_core_db = "SELECT meta_value FROM meta WHERE meta_key = \"species.classification\" AND species_id = " + species_id + " ORDER BY meta_id LIMIT 1";
		String name_core_db = DBUtils.getRowColumnValue(coreDbre.getConnection(), sql_core_db);

		String sql_taxonomy_db = "SELECT name FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + taxon_id;
		String name_taxonomy_db = DBUtils.getRowColumnValue(ncbiDbre.getConnection(), sql_taxonomy_db);

		return compareNames(coreDbre, "First classification name", species_id, taxon_id, name_core_db, name_taxonomy_db);
	}


	/**
	 * Check that the classification array (except the first element) are
	 * the same as traversing the NCBI taxonomy upwards from the genome's
	 * parent node
	 */
	public boolean checkRestOfClassification(final DatabaseRegistryEntry ncbiDbre, final DatabaseRegistryEntry coreDbre, int species_id, String taxon_id) {
		/* This check is quite complex as the taxonomy is stored in very different ways in taxonomy
		   and core DBs. In the former, the tree structure is stored in the ncbi_taxa_node table
		   while the names are in the ncbi_taxa_name table. In the core DB, the taxonomy is
		   stored in the meta table as values of the key "species.classification" and they
		   should be sorted by meta_id. In the core DB, only the abbreviated lineage is
		   described which means that we have to ignore ncbi_taxa_node with the
		   genbank_hidden_flag set. */
		String values1[] = DBUtils.getRowValues(ncbiDbre.getConnection(), "SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + taxon_id);

		if (values1.length == 0) {
			// if no rows are fetched, this taxon is missing from the taxonomy DB 
			ReportManager.problem(this, ncbiDbre.getConnection(), "No entry for " + taxon_id);
			return false;

		} else {

			// Extract the clasification from the NCBI database
			String ncbiClassification = "";
			String this_taxon_id = values1[1];
			while (!this_taxon_id.equals("0")) {
				values1 = DBUtils.getRowValues(ncbiDbre.getConnection(), "SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + this_taxon_id);
				if ( // values1[2].equals("0") &&       // we used to filter out entries with genbank_hidden_flag, we don't anymore
						!values1[1].equals("1") && !values1[1].equals("0") &&
						!values1[0].equals("subgenus") && !values1[0].equals("genus") && !values1[0].equals("species subgroup") && !values1[0].equals("species group")
				   ) {
					String taxonName = DBUtils.getRowColumnValue(ncbiDbre.getConnection(), "SELECT name FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + this_taxon_id);
					ncbiClassification += " " + taxonName;
				}
				this_taxon_id = values1[1];
			}

			// And from the core database
			String core_classification = "";
			String[] classification_values = DBUtils.getColumnValues(coreDbre.getConnection(), "SELECT meta_value FROM meta WHERE meta_key = \"species.classification\" AND species_id = " + species_id + " ORDER BY meta_id");
			/* Skip first value as it is part of the species name and not the lineage */
			for (int a = 1; a < classification_values.length; a++) {
				core_classification += " " + classification_values[a];
			}

			// NOTE: both strins have a leading space
			return compareNames(coreDbre, "Classification", species_id, taxon_id, core_classification, ncbiClassification);
		}
	}
}

