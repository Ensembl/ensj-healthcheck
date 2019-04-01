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

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships in the tables of the Compara schema that hold genomic
 * alignments.
 */

public class ForeignKeyGenomicAlignmentTables extends AbstractComparaTestCase {

	public ForeignKeyGenomicAlignmentTables() {
		setDescription("Check for broken foreign-key relationships in the genomic-alignment tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		boolean result = true;
		// genomic_align -> dnafrag
		result &= checkForOrphans(con, "genomic_align", "dnafrag_id", "dnafrag", "dnafrag_id");
		// genomic_align <-> genomic_align_block
		result &= checkForOrphans(con, "genomic_align_block", "genomic_align_block_id", "genomic_align", "genomic_align_block_id");
		result &= checkForOrphans(con, "genomic_align", "genomic_align_block_id", "genomic_align_block", "genomic_align_block_id");
		// genomic_align -> genomic_align_tree
		result &= checkOptionalRelation(con, "genomic_align", "node_id", "genomic_align_tree", "node_id");
		// genomic_align_tree internal relationships
		result &= checkForOrphansSameTable(con, "genomic_align_tree", "parent_id", "node_id", true);
		result &= checkForOrphansSameTable(con, "genomic_align_tree", "root_id", "node_id", false);
		result &= checkForOrphansSameTable(con, "genomic_align_tree", "left_node_id", "node_id", true);
		result &= checkForOrphansSameTable(con, "genomic_align_tree", "right_node_id", "node_id", true);
		return result;
	}

} // ForeignKeyGenomicAlignmentTables
