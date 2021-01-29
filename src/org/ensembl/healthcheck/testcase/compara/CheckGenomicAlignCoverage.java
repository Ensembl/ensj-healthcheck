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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that the coverage for a
 * method_link_species_set matches the coverage recorded in the  mlss_tag
 * table
 */

public class CheckGenomicAlignCoverage extends AbstractComparaTestCase {

	/**
	 * Create a CheckGenomicAlignCoverage that applies to a specific set of
	 * databases.
	 */
	public CheckGenomicAlignCoverage() {
		setDescription("Check the actual coverage for a method_link_species_set matches the coverage tag");
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
		String comparaDbName = (con == null) ? "no_database" : DBUtils.getShortDatabaseName(con);

		/**
		 * Get all method_link_species_set_ids for LASTZ_NET (method_link_id = 16)
		 */
		String lastz_sql = "SELECT method_link_species_set_id FROM method_link_species_set JOIN method_link USING(method_link_id) WHERE method_link.type = 'LASTZ_NET'";
		String[] pw_method_link_species_set_ids = DBUtils.getColumnValues(con, lastz_sql);
		
		/**
		 *  - Check if we're working with vertebrates or not
		 *  - Set the overlap check accordingly
		 */
		String division = getComparaDivisionName(dbre);
		String overlap_check = "1"; // non-vertebrate default : overlaps always allowed
		if ( division.equals("vertebrates") ) {
			// vertebrate-specific condition : overlaps only expected in some cases
			overlap_check = "IF(x.ref_status = 'ref' AND x.species_name NOT IN ('homo_sapiens', 'mus_musculus'), 0, 1)";
		}

		if (pw_method_link_species_set_ids.length > 0) {

			for (String mlss_id : pw_method_link_species_set_ids) {
				/**
				Check if the mlss_tag coverage value 'matches' the sum of all genomic_align ranges. We consider a match:
				1. exactly the same value in cases where overlaps are not allowed
				  1.1. in vertebrates:  reference species, but not human or mouse
				  1.2. non-vertebrates: overlaps allowed in all species
				2. sum of genomic_align ranges is larger than tag value when overlaps are allowed (non-ref species or ref human/mouse)
				*/
				
				String tag_coverage_sql = "SELECT LEFT(tag, 3) AS ref_status, GROUP_CONCAT(IF(tag LIKE '%species', value, NULL)) AS species_name, " +
					"GROUP_CONCAT(IF(tag LIKE '%coverage', value, NULL)) AS tag_coverage " +
					"FROM method_link_species_set_tag WHERE (tag LIKE '%species' OR tag LIKE '%genome_coverage') " +
					"AND method_link_species_set_id = " + mlss_id + " GROUP BY LEFT(tag, 3)";
				String genomic_coverage_sql = "SELECT g.name, d.genome_db_id, x.tag_coverage, SUM(ga.dnafrag_end-ga.dnafrag_start+1) AS genomic_align_coverage, " +
					overlap_check + " AS overlaps_allowed " +
					"FROM genomic_align ga JOIN dnafrag d USING(dnafrag_id) JOIN genome_db g USING(genome_db_id) JOIN (" + tag_coverage_sql + 
					") x ON x.species_name = g.name WHERE ga.method_link_species_set_id = " + mlss_id + " GROUP BY g.name";
				String summary_sql = "SELECT SUM(IF((overlaps_allowed = 0 AND tag_coverage = genomic_align_coverage) " +
					"OR (overlaps_allowed = 1 AND tag_coverage <= genomic_align_coverage), 1, 0)) AS coverage_ok " +
					"FROM (" + genomic_coverage_sql + ") y";
				
				String coverage_ok = DBUtils.getRowColumnValue(con, summary_sql);
				
				// get species_set size from db to account for self-alignements
				String species_set_size_sql = "SELECT ss.size FROM species_set_header ss JOIN method_link_species_set m USING(species_set_id) WHERE m.method_link_species_set_id = " + mlss_id;
				String species_set_size = DBUtils.getRowColumnValue(con, species_set_size_sql);
				
				if ( !coverage_ok.equals(species_set_size) ) {
					ReportManager.problem(this, con, "FAILED genomic_align coverage does not match method_link_species_set_tag coverage");
					ReportManager.problem(this, con, "FAILURE DETAILS: Alignment coverage for method_link_species_set_id " + mlss_id + " is inconsistent. "+coverage_ok+"/2 coverage values correct.");
					ReportManager.problem(this, con, "USEFUL SQL: " + summary_sql);
					result = false;
				}
			}
		}
		
		/**
		* Get all method_link_species_set_ids for MSAs (EPO, EPO_LOW_COVERAGE, PECAN)
		*/
		String msa_sql = "SELECT method_link_species_set_id FROM method_link_species_set JOIN method_link USING(method_link_id) WHERE method_link.type IN ('EPO', 'EPO_LOW_COVERAGE', 'PECAN')";
		String[] msa_method_link_species_set_ids = DBUtils.getColumnValues(con, msa_sql);
		
		if (msa_method_link_species_set_ids.length > 0) {

			for (String mlss_id : msa_method_link_species_set_ids) {
				String genomic_align_coverage_sql = "SELECT d.genome_db_id, SUM(ga.dnafrag_end-ga.dnafrag_start+1) AS genomic_align_coverage FROM genomic_align ga JOIN dnafrag d USING(dnafrag_id) " +
					"WHERE ga.method_link_species_set_id = " + mlss_id + " GROUP BY d.genome_db_id";
				String tag_coverage_sql = "SELECT n.genome_db_id, t.value AS tag_coverage, g.genomic_align_coverage FROM species_tree_node n JOIN species_tree_root r USING(root_id) " +
					"JOIN species_tree_node_tag t USING(node_id) JOIN (" + genomic_align_coverage_sql + ")g USING(genome_db_id) WHERE n.genome_db_id IS NOT NULL AND t.tag = 'genome_coverage' " +
					"AND r.method_link_species_set_id = " + mlss_id;
				String summary_sql = "SELECT FORMAT(AVG(IF(genomic_align_coverage >= tag_coverage, 1, 0)), '#') FROM (" + tag_coverage_sql + ") c";
				
				String avg_coverage = DBUtils.getRowColumnValue(con, summary_sql);
				if ( !avg_coverage.equals("1") ) {
					ReportManager.problem(this, con, "FAILED genomic_align coverage does not match species_tree_node_tag coverage");
					ReportManager.problem(this, con, "FAILURE DETAILS: Alignment coverage for method_link_species_set_id " + mlss_id + " is inconsistent.");
					ReportManager.problem(this, con, "USEFUL SQL: " + summary_sql);
					result = false;
				}

			}
		}
		
		return result;
	}

} // CheckGenomicAlignCoverage
