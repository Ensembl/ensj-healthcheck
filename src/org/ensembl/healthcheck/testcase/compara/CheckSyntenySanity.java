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
import java.util.Arrays;
import java.util.ArrayList;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for chromosomes
 * missing synteny
 */

public class CheckSyntenySanity extends SingleDatabaseTestCase {

	/**
	 * Create an CheckSynteny that applies to a specific set of databases.
	 */
	public CheckSyntenySanity() {
		setDescription("Check for missing syntenies in the compara database.");
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
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

			for (String this_mlss_id : get_all_method_link_species_set_ids(con)) {
				result &= check_this_synteny(con, this_mlss_id);
			}

		return result;

	}

	private ArrayList<String> get_all_method_link_species_set_ids(Connection con) {
		ArrayList<String> method_link_species_set_ids = new ArrayList<String>();

		String[] method_link_ids = DBUtils
				.getColumnValues(
						con,
						"SELECT method_link_id FROM method_link WHERE class LIKE 'SyntenyRegion%' OR type = 'SYNTENY'");
		for (String method_link_id : method_link_ids) {
			String[] these_method_link_ids = DBUtils
					.getColumnValues(
							con,
							"SELECT method_link_species_set_id FROM method_link_species_set WHERE method_link_id = "
									+ method_link_id);
			method_link_species_set_ids.addAll(Arrays.asList(these_method_link_ids));
		}

		return method_link_species_set_ids;
	}

	private boolean check_this_synteny(Connection con,
			String method_link_species_set_id) {
		boolean result = true;

		String[] genome_db_ids = DBUtils
				.getColumnValues(
						con,
						"SELECT genome_db_id FROM method_link_species_set LEFT JOIN species_set"
								+ " USING (species_set_id) WHERE method_link_species_set_id = "
								+ method_link_species_set_id);

		/**
		 * Looks for method_link_species_sets of GenomicAlignBlocks using the
		 * same species set.
		 * 
		 * If no synteny regions can be found for the method_link_species_set, 
		 * then the genomic align blocks will be checked.
		 * 
		 */
		String[] alignment_mlss_ids = DBUtils
				.getColumnValues(
						con,
						"SELECT mlss2.method_link_species_set_id FROM method_link_species_set mlss1,"
								+ " method_link_species_set mlss2, method_link ml WHERE mlss1.method_link_species_set_id = "
								+ method_link_species_set_id
								+ " AND mlss1.species_set_id = mlss2.species_set_id"
								+ " AND mlss2.method_link_id = ml.method_link_id AND ml.class like 'GenomicAlignBlock%'");
		
		for (String genome_db_id : genome_db_ids) {
			String genome_db_name = DBUtils.getRowColumnValue(con,
					"SELECT name FROM genome_db " + " WHERE genome_db_id = "
							+ genome_db_id);
			
			/**
			 * Get ids of dna_frags that are longer that 1Mb. The 'NOT LIKE'
			 * bits exclude coord systems like
			 * 
			 *   - unknown_singleton
			 *   - unknown_group and
			 *   - chromosome_group.
			 * 
			 */			
			String[] these_dnafrag_ids = DBUtils.getColumnValues(con,
					"SELECT dnafrag_id FROM dnafrag WHERE genome_db_id = "
							+ genome_db_id
							+ " AND coord_system_name IN ('chromosome', 'group')"
							+ " AND name NOT LIKE '%\\_%'"
							+ " AND name NOT LIKE '%Un%'"
							+ " AND name NOT IN ('MT') AND length > 1000000");
			
			for (String dnafrag_id : these_dnafrag_ids) {
				
				/**
				 * count is the number of synteny regions that are on the 
				 * dnafrag tested in this iteration of the loop that belong
				 * to the method_link_species_set being tested in this call
				 * of the method.
				 * 
				 */
				int count = DBUtils
						.getRowCountFast(
								con,
								"SELECT count(*) FROM synteny_region "
										+ " LEFT JOIN dnafrag_region USING (synteny_region_id) WHERE"
										+ " method_link_species_set_id = "
										+ method_link_species_set_id
										+ " AND dnafrag_id = "
										+ dnafrag_id);
				/*
				 * If synteny regions were found, this is ok, otherwise check 
				 * alignments from genomic align blocks.
				 * 
				 */
				if (count == 0) {
					int aln_count = 0;
					String aln_name = "";
					String aln_mlss_id = "";
					for (String alignment_mlss_id : alignment_mlss_ids) {
						
						/**
						 *  Name of the dna frag with the greatest amount of
						 *  genomic alignment blocks that make hits on other
						 *  dna frags (foreign alignments) and how many such 
						 *  genomic alignment blocks exist on this dna frag. 
						 */
						String[] aln_result = DBUtils
								.getRowValues(
										con,
										"SELECT dnafrag.name, count(*) FROM"
												+ " genomic_align ga1 LEFT JOIN genomic_align ga2 USING (genomic_align_block_id)"
												+ " LEFT JOIN dnafrag ON (ga2.dnafrag_id = dnafrag.dnafrag_id) WHERE"
												+ " ga1.dnafrag_id = "
												+ dnafrag_id
												+ " AND dnafrag.coord_system_name IN ('chromosome', 'group')"
												+ " AND ga1.method_link_species_set_id = "
												+ alignment_mlss_id
												+ " AND ga1.dnafrag_id <> ga2.dnafrag_id GROUP BY ga2.dnafrag_id "
												+ " ORDER BY count(*) DESC LIMIT 1");

						if (aln_result.length > 0
								&& Integer.valueOf(aln_result[1]).intValue() > aln_count) {
							aln_count = Integer.valueOf(aln_result[1])
									.intValue();
							aln_name = aln_result[0];
							aln_mlss_id = alignment_mlss_id;
						}
					}
					
					/*
					 * If a dna_frag has more than 1000 foreign alignments,
					 * this is reported as an error. 
					 */
					if (aln_count > 1000) {
						String dnafrag_name = DBUtils.getRowColumnValue(con,
								"SELECT name FROM dnafrag "
										+ " WHERE dnafrag_id = "
										+ dnafrag_id);
						String dnafrag_length = DBUtils.getRowColumnValue(con,
								"SELECT length FROM dnafrag "
										+ " WHERE dnafrag_id = "
										+ dnafrag_id);
						ReportManager.problem(this, con, aln_count
								+ " alignments to " + genome_db_name + " chr."
								+ dnafrag_name + " and no syntenies for MLSS "
								+ method_link_species_set_id);
						result = false;
					}
				}
			}
		}

		return result;
	}

} // CheckHomology
