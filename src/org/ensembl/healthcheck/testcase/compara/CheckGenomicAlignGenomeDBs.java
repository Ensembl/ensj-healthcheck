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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that all the genome_dbs for a
 * method_link_species_set are present in the genomic_aligns
 */

public class CheckGenomicAlignGenomeDBs extends SingleDatabaseTestCase {

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

		if (method_link_species_set_ids.length > 0) {

			for (String mlss_id : method_link_species_set_ids) {
				/**
				 * Expected number of genome_db_ids
				 */

				String gdb_sql = "SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = " + mlss_id;
				String[] num_genome_db_ids = DBUtils.getColumnValues(con, gdb_sql);

				/**
				 * Find genome_db_ids in genomic_aligns. For speed, only look at
				 * the first 100 genomic_align_blocks. If the test fails, it
				 * could be by chance that not all the genome_db_ids are found.
				 * Expect the number of distinct genome_db_ids to be the same as
				 * the number of genome_db_ids in the species set except when I
				 * have an ancestor when the number from the genomic_aligns will
				 * be one larger. Don't specifically test for this, just check
				 * if it's equal to or larger - more worried if it's smaller ie
				 * missed some expected genome_db_ids.
				 */
				String useful_sql;
				useful_sql = "SELECT COUNT(DISTINCT genome_db_id) FROM (SELECT * FROM genomic_align_block WHERE method_link_species_set_id = "
						+ mlss_id
						+ " limit 100) t1 LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN dnafrag USING (dnafrag_id) HAVING COUNT(DISTINCT genome_db_id) >= (SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = "
						+ mlss_id + " )";
				String[] success = DBUtils.getColumnValues(con, useful_sql);

				if (success.length > 0) {
					/**
					 * System.out.println("MLSS " +
					 * mlss_id + " real " + success[0] +
					 * " expected " + num_genome_db_ids[0]);
					 */
					ReportManager.correct(this, con, "All genome_dbs are present in the genomic_aligns for method_link_species_set_id " + mlss_id);
				} else {
					ReportManager.problem(
									this,
									con,
									"WARNING not all the genome_dbs are present in the first 100 genomic_align_block_ids. Could indicate a problem with alignment with method_link_species_set_id "
											+ mlss_id);
					ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
					result = false;
				}
			}
		}

		return result;

	}

} // CheckGenomicAlignGenomeDBs
