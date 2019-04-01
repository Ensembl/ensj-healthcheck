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
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that all the genome_dbs for a
 * method_link_species_set are present in the genomic_aligns
 */

public class CheckGenomicAlignGenomeDBs extends AbstractComparaTestCase {

	/**
	 * Create an CheckGenomicAlignGenomeDBs that applies to a specific set of
	 * databases.
	 */
	public CheckGenomicAlignGenomeDBs() {
		setDescription("Check the genome_dbs for a method_link_species_set are present in the genomic_aligns");
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
		 * Check have entries in the genomic_align table
		 */
		if (!tableHasRows(con, "genomic_align")) {
			ReportManager.problem(this, con,
					"No entries in the genomic_align table");
			return result;
		}
		if (!tableHasRows(con, "genomic_align_block")) {
			ReportManager.problem(this, con,
					"No entries in the genomic_align_block table");
			return result;
		}
		if (!tableHasRows(con, "method_link_species_set")) {
			ReportManager.problem(this, con,
					"No entries in the method_link_species_set table");
			return result;
		}
		/**
		 * Get all method_link_species_set_ids for genomic_align_blocks
		 */
		String[] method_link_species_set_ids = DBUtils
				.getColumnValues(con,
						"SELECT distinct(method_link_species_set_id) FROM genomic_align_block");

		String ancestral_gdb_id = DBUtils.getRowColumnValue(con, "SELECT genome_db_id FROM genome_db WHERE name = \"ancestral_sequences\"");

		if (method_link_species_set_ids.length > 0) {

			for (String mlss_id : method_link_species_set_ids) {

				/**
				 * Find genome_db_ids in genomic_aligns. For speed, first only
				 * look at the first 100 genomic_align_blocks. If the test fails,
				 * test all genomic_align_blocks.
				 * Expect the number of distinct genome_db_ids to be the same as
				 * the number of genome_db_ids in the species set except when I
				 * have an ancestor when the number from the genomic_aligns will
				 * be one larger. Don't specifically test for this, just check
				 * if it's equal to or larger - more worried if it's smaller ie
				 * missed some expected genome_db_ids.
				 */
				String gab_part1_sql = "(SELECT genome_db_id FROM (SELECT genomic_align_block_id FROM genomic_align_block WHERE method_link_species_set_id = " + mlss_id;
				String ancestral_sql = ancestral_gdb_id.equals("") ? "" : ("WHERE genome_db_id != " + ancestral_gdb_id);
				String gab_part2_sql = " ) _t1 JOIN genomic_align USING (genomic_align_block_id) JOIN dnafrag USING (dnafrag_id) " + ancestral_sql + ") _t3";
				String mlss_sql = "(SELECT genome_db_id FROM species_set JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = " + mlss_id + ") _t2";

				String useful_sql = "SELECT DISTINCT genome_db_id FROM genomic_align JOIN dnafrag USING (dnafrag_id) WHERE method_link_species_set_id = " + mlss_id + "; ";
				useful_sql += "SELECT DISTINCT genome_db_id FROM species_set JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = " + mlss_id + ";";

				// genomic_align -> species_set
				boolean only_expected_genomes = checkConsistency(con, gab_part1_sql + " LIMIT 100 " + gab_part2_sql, mlss_sql, "_t2.genome_db_id");
				if (only_expected_genomes) {
					// Maybe the first 100 blocks were lucky. Check the others !
					only_expected_genomes = checkConsistency(con, gab_part1_sql + gab_part2_sql, mlss_sql, "_t2.genome_db_id");
				}
				if (only_expected_genomes) {
					ReportManager.correct(this, con, "All the genomic_aligns of method_link_species_set_id " + mlss_id + " match the species-set");
				} else {
					ReportManager.problem(this, con, "Some genomic_aligns of method_link_species_set_id " + mlss_id + " are not found in the species-set");
					ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
					result = false;
				}

				// species_set -> genomic_align
				boolean all_genomes_used = checkConsistency(con, mlss_sql, gab_part1_sql + " LIMIT 100 " + gab_part2_sql, "_t3.genome_db_id");
				if (!all_genomes_used) {
					// The first 100 blocks don't contain all the genomes.  Check the remaining blocks as well
					all_genomes_used = checkConsistency(con, mlss_sql, gab_part1_sql + gab_part2_sql, "_t3.genome_db_id");
				}
				if (all_genomes_used) {
					ReportManager.correct(this, con, "All the genome_dbs of method_link_species_set_id " + mlss_id + " are found in the genomic_aligns");
				} else {
					ReportManager.problem(this, con, "Some genome_dbs of method_link_species_set_id " + mlss_id + " are not found in the genomic_aligns");
					ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
					result = false;
				}
			}
		}

		return result;
	}

	public boolean checkConsistency(Connection con, String left_sql, String right_sql, String right_column) {

		String sql = "SELECT COUNT(*) FROM " + left_sql + " LEFT JOIN " + right_sql + " USING (genome_db_id) WHERE " + right_column + " IS NULL";

		int orphans = DBUtils.getRowCountFast(con, sql);
		return (orphans == 0);
	}

} // CheckGenomicAlignGenomeDBs
