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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for the consistency of the
 * genomic_align_tree table
 */

public class CheckGenomicAlignTreeTable extends AbstractComparaTestCase {

	public CheckGenomicAlignTreeTable() {
		setDescription("Check the consistency of the genomic_align_tree table.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		if (!tableHasRows(con, "genomic_align_tree")) {
			ReportManager.correct(this, con, "NO ENTRIES in genomic_align_tree table, so nothing to test IGNORED");
			return true;
		}

		boolean result = true;

		String[] method_link_species_set_ids = DBUtils.getColumnValues(con, "SELECT method_link_species_set_id FROM method_link_species_set LEFT JOIN method_link USING (method_link_id) WHERE class IN (\"GenomicAlignTree.ancestral_alignment\", \"GenomicAlignTree.tree_alignment\")");

		for (String mlss_id: method_link_species_set_ids) {
			String mlss_id_condition = "FLOOR(node_id/10000000000) = " + mlss_id;

			// Check the NULLable columns are not always NULL
			result &= checkCountIsNonZero(con, "genomic_align_tree", mlss_id_condition + " AND parent_id IS NOT NULL");
			result &= checkCountIsNonZero(con, "genomic_align_tree", mlss_id_condition + " AND left_node_id IS NOT NULL");
			result &= checkCountIsNonZero(con, "genomic_align_tree", mlss_id_condition + " AND right_node_id IS NOT NULL");

			// Check the validity of distance_to_parent
			String all_rows_sql = "SELECT 1 FROM genomic_align_tree WHERE " + mlss_id_condition;
			int n_rows = DBUtils.getRowCount(con, all_rows_sql);
			String bad_dist_rows_sql = "SELECT 1 FROM genomic_align_tree WHERE " + mlss_id_condition + " AND distance_to_parent > 1";
			int n_bad_dist_rows = DBUtils.getRowCount(con, bad_dist_rows_sql);
			// We allow up to 1% of the rows to have distance_to_parent>1
			// (it only happens in 0.025% of the cases at the moment)
			if (100 * n_bad_dist_rows < 1 * n_rows) {
				ReportManager.correct(this, con, "distance_to_parent<1 alignment mlss_id=" + mlss_id);
			} else {
				ReportManager.problem(this, con, "distance_to_parent>1 for " + n_bad_dist_rows + " rows out of " + n_rows + " for alignment mlss_id=" + mlss_id);
				result = false;
			}
		}

		return result;
	}

} // CheckGenomicAlignTreeTable
