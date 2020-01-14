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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;

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

		// Check the left_node_id values are set (and assume right_node_ids have also been set)
		// FIXME: this will have to be updated when left_node_id becomes NULLable
		result &= checkCountIsNonZero(con, "genomic_align_tree", "left_node_id != 0");

		/* Looking at distance_to_parent > 1 is true for LOW_COVERAGE but not epo */
		/* Update 2015-30-04: there are nodes with distance_to_parent > 1
		 * in all the EPO alignments, but also for the "11 fish EPO_LOW_COVERAGE"
		 */
		//result &= checkCountIsZero(con, "genomic_align_tree", "distance_to_parent > 1");

		return result;
	}

} // CheckGenomicAlignTreeTable
