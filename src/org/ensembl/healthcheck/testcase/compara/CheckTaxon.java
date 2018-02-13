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
import java.util.Map;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check compara taxon table against core meta ones.
 */

public class CheckTaxon extends AbstractComparaTestCase {

	/**
	 * Create a new instance of MetaCrossSpecies
	 */
	public CheckTaxon() {
		setDescription("Check that the attributes of the taxon table (genus, species," +
				" common_name and classification) correspond to the meta data in the core DB and vice versa.");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Check that the attributes of the taxon table (genus, species, common_name and
	 * classification) correspond to the meta data in the core DB and vice versa.
	 * NB: A warning message is displayed if some dnafrags cannot be checked because
	 * there is not any connection to the corresponding core database.
	 * 
	 * @param comparaDbre
	 *          The database registry containing all the specified databases.
	 * @return true if the all the taxa in compara.taxon table which have a counterpart in
	 *    the compara.genome_db table match the corresponding core databases.
	 */
	public boolean run(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		result &= checkTaxon(comparaDbre);
		return result;
	}


	/**
	 * Check that the attributes of the taxon table (genus, species, common_name and
	 * classification) correspond to the meta data in the core DB and vice versa.
	 * NB: A warning message is displayed if some dnafrags cannot be checked because
	 * there is not any connection to the corresponding core database.
	 * 
	 * @param comparaDbre
	 *          The database registry entry for Compara DB
	 * @return true if the all the taxa in compara.taxon table which have a counterpart in
	 *    the compara.genome_db table match the corresponding core databases.
	 */
	public boolean checkTaxon(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		Connection comparaCon = comparaDbre.getConnection();

		//Check that don't have duplicate entries in the ncbi_taxa_name table
		String useful_sql = "SELECT taxon_id,name,name_class,count(*) FROM ncbi_taxa_name GROUP BY taxon_id,name,name_class HAVING count(*) > 1;";
		String[] failures = DBUtils.getColumnValues(comparaCon, useful_sql);
		if (failures.length > 0) {
			ReportManager.problem(this, comparaCon, "FAILED ncbi_taxa_name contains duplicate entries ");
			ReportManager.problem(this, comparaCon, "FAILURE DETAILS: There are " + failures.length + " ncbi_taxa_names with more than 1 entry");
			ReportManager.problem(this, comparaCon, "USEFUL SQL: " + useful_sql);
			result = false;
		} else {
			result = true;
		}

		for (GenomeEntry genomeEntry : getAllGenomes(comparaDbre, DBUtils.getMainDatabaseRegistry())) {
			if (genomeEntry.getCoreDbre() != null) {
				Integer species_id = genomeEntry.getSpeciesID();
				Connection speciesCon = genomeEntry.getCoreDbre().getConnection();
				String sql1, sql2;
				/* Get taxon_id */
				String taxon_id = DBUtils.getRowColumnValue(speciesCon,
						"SELECT meta_value FROM meta WHERE meta_key = \"species.taxonomy_id\" AND species_id = " + species_id);

				/* Check name ++ compara scientific name := last two entries in the species classification in the core meta table */
				sql1 = "SELECT \"name\", name " +
					" FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + taxon_id;
				sql2 = "SELECT \"name\", meta_value " +
					" FROM meta WHERE meta_key = \"species.classification\" AND species_id = " + species_id + " ORDER BY meta_id LIMIT 1";
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check scientific name in compara and core meta */
				sql1 = "SELECT \"scientific name\", name " +
					" FROM ncbi_taxa_name WHERE name_class = \"scientific name\" AND taxon_id = " + taxon_id;
				sql2 = "SELECT \"scientific name\", meta_value " +
					" FROM meta WHERE meta_key = \"species.scientific_name\" AND species_id = " + species_id;
				result &= compareQueries(comparaCon, sql1, speciesCon, sql2);

				/* Check classification */
				/* This check is quite complex as the axonomy is stored in very different ways in compara
				   and core DBs. In compara, the tree structure is stored in the ncbi_taxa_node table
				   while the names are in the ncbi_taxa_name table. In the core DB, the taxonomy is
				   stored in the meta table as values of the key "species.classification" and they
				   should be sorted by meta_id. In the core DB, only the abbreviated lineage is
				   described which means that we have to ignore ncbi_taxa_node with the
				   genbank_hidden_flag set. On top of that, we want to compare the classification
				   in one single SQL. Therefore, we are getting the results recursivelly and
				   then execute a dumb SQL query with result itself */
				String comparaClassification = "";
				String values1[] = DBUtils.getRowValues(comparaCon,
						"SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + taxon_id);
				if (values1.length == 0) {
					/* if no rows are fetched, this taxon is missing from compara DB */
					ReportManager.problem(this, comparaCon, "No taxon for " + genomeEntry.getName());
				} else {
					String this_taxon_id = values1[1];
					while (!this_taxon_id.equals("0")) {
						values1 = DBUtils.getRowValues(comparaCon,
								"SELECT rank, parent_id, genbank_hidden_flag FROM ncbi_taxa_node WHERE taxon_id = " + this_taxon_id);
						if ( // values1[2].equals("0") &&       // we used to filter out entries with genbank_hidden_flag, we don't anymore
								!values1[1].equals("1") && !values1[1].equals("0") &&
								!values1[0].equals("subgenus") && !values1[0].equals("genus") && !values1[0].equals("species subgroup") && !values1[0].equals("species group")
						   ) {
							String taxonName = DBUtils.getRowColumnValue(comparaCon,
									"SELECT name FROM ncbi_taxa_name " +
									"WHERE name_class = \"scientific name\" AND taxon_id = " + this_taxon_id);

							comparaClassification += " " + taxonName;
						   }
						this_taxon_id = values1[1];
					}
					sql1 = "SELECT \"classification\", \"" + comparaClassification + "\"";
					/* It will be much better to run this using GROUP_CONCAT() but our MySQL server does not support it yet */
					sql2 = "SELECT \"classification\", \"";
					String[] values2 = DBUtils.getColumnValues(speciesCon,
							"SELECT meta_value FROM meta WHERE meta_key = \"species.classification\" AND species_id = " + species_id +
							" ORDER BY meta_id");
					/* Skip first value as it is part of the species name and not the lineage */
					for (int a = 1; a < values2.length; a++) {
						sql2 += " " + values2[a];
					}
					sql2 += "\"";
					result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
				}
			} else {
				ReportManager.problem(this, comparaCon, "No connection for " + genomeEntry.getName());
				result = false;
			}
		}

		return result;
	}

} // CheckTopLevelDnaFrag
