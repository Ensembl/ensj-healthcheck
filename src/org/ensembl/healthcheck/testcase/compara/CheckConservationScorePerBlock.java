/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks the conservation_score table
 */

public class CheckConservationScorePerBlock extends SingleDatabaseTestCase {

	/**
	 * Create an CheckConservationScorePerBlock that applies to a specific set of
	 * databases.
	 */
	public CheckConservationScorePerBlock() {
		setDescription("Check the conservation_score table in ensembl_compara databases.");
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

		/**
		 * Get Ancestral sequences genome_db_id
		 */
		String ancestral_seq_id = DBUtils.getRowColumnValue(con, "SELECT genome_db_id FROM genome_db WHERE name = \"ancestral_sequences\"");

		/**
		 * Get all mlss_ids for the associated multiple alignment of each
		 * method_link_species_set_id for method_link type of
		 * GERP_CONSERVATION_SCORE
		 */
		List<String[]> msa_mlss_ids_list = DBUtils.getRowValuesList(con, "SELECT method_link_species_set_id, value FROM method_link_species_set LEFT JOIN method_link USING (method_link_id) LEFT JOIN method_link_species_set_tag USING (method_link_species_set_id) WHERE (type = \"GERP_CONSERVATION_SCORE\" OR class LIKE \"ConservationScore%\") AND tag = \"msa_mlss_id\"");
			for (String[] msa_mlss_ids : msa_mlss_ids_list) {
				if (msa_mlss_ids[1] == "") {
					ReportManager.problem(this, con, "There is no msa_mlss_id tag for the GERP mlss" + msa_mlss_ids[0] + "\n");
				} else {
					/**
					 * Even if the ancestral_sequences genome_db is there, it does
					 * not mean that it is used by ALL the alignments, so we use
					 * that extra information as an additional flag to run the
					 * query faster
					 */
					String ancestral_align = DBUtils.getRowColumnValue(con, "SELECT * FROM method_link_species_set mlss LEFT JOIN method_link USING (method_link_id) WHERE method_link_species_set_id = "
							+ msa_mlss_ids[1]
							+ " AND class = \"GenomicAlignTree.ancestral_alignment\";");
					/**
					 * Find the multiple alignments gabs which have more than 3
					 * species but don't have any conservation scores Need to
					 * exclude gabs containing ancestral sequences
					 */
					String useful_sql;
					if (ancestral_seq_id == "" || ancestral_align == "") {
						useful_sql = "SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN conservation_score USING (genomic_align_block_id) WHERE genomic_align_block.method_link_species_set_id = "
								+ msa_mlss_ids[1]
								+ " AND conservation_score.genomic_align_block_id IS NULL GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) > 3";
					} else {
						useful_sql = "SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN conservation_score USING (genomic_align_block_id) LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN dnafrag USING (dnafrag_id) WHERE genomic_align_block.method_link_species_set_id = "
								+ msa_mlss_ids[1]
								+ " AND conservation_score.genomic_align_block_id IS NULL AND genome_db_id <> "
								+ ancestral_seq_id
								+ " GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) > 3";
					}

					String[] failures = DBUtils
							.getColumnValues(con, useful_sql);
					if (failures.length > 0) {
						/**
						 * Warning if there are blocks with 4 genomes because it
						 * is possible to have (human, chimp, rhesus) and one of
						 * cow or dog and still not get above the min_rej_sub
						 * score (default=0.5)
						 */
						String useful_sql4 = "SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN conservation_score USING (genomic_align_block_id) WHERE genomic_align_block.method_link_species_set_id = "
								+ msa_mlss_ids[1]
								+ " AND conservation_score.genomic_align_block_id IS NULL GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) = 4";
						String[] failures4 = DBUtils.getColumnValues(con,
								useful_sql4);
						if (failures.length == failures4.length) {
							ReportManager.problem(this, con, "WARNING conservation_score -> multiple alignments which have more than 3 species but don't have any conservation scores");
							ReportManager.problem(this, con, "WARNING DETAILS: There are " + failures.length + " blocks (mlss= " + msa_mlss_ids[1]
													+ ") with 4 seqs and no conservation score! Must check that the sum of the branch lengths of these 4 species is less than 0.5 (min_neu_evol). If it is greater than 0.5, there is a problem that needs fixing!");
							ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql4);

						} else {
							ReportManager.problem(this, con, "FAILED conservation_score -> multiple alignments which have more than 3 species but don't have any conservation scores");
							ReportManager.problem(this, con, "FAILURE DETAILS: There are " + failures.length + " blocks (mlss= " + msa_mlss_ids[1] + ") with more than 4 seqs and no conservation score!");
							ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
							result = false;
						}
					}
				}
			}

		return result;

	}

} // CheckConservationScorePerBlock
