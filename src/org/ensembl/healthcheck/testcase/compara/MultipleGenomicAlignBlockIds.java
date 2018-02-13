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

import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class MultipleGenomicAlignBlockIds extends SingleDatabaseTestCase {

	public MultipleGenomicAlignBlockIds() {
		setDescription("Check that every genomic_align_block_id is linked to more than one single genomic_align_id.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (tableHasRows(con, "genomic_align")) {

			// The test does not apply to EPO alignments because they store
			// ancestral sequences in a different genomic_align_block_id
			String sqlNonEPOmlss_ids = "SELECT method_link_species_set_id FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE class LIKE 'GenomicAlign%' AND class != 'GenomicAlignTree.ancestral_alignment'";
			String[] nonEPOmlss_ids = DBUtils.getColumnValues(con, sqlNonEPOmlss_ids);
			for (String mlss_id : nonEPOmlss_ids) {
				result &= checkForSinglesWithConstraint(con, "genomic_align", "genomic_align_block_id", "WHERE method_link_species_set_id = " + mlss_id);
			}

		} else {
			ReportManager.correct(this, con, "NO ENTRIES in genomic_align table, so nothing to test IGNORED");
		}

		return result;

	}

} // MultipleGenomicAlignBlockIds
