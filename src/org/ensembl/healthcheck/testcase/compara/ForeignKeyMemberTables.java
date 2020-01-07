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
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships in the *member tables of the Compara schema.
 */

public class ForeignKeyMemberTables extends AbstractComparaTestCase {

	public ForeignKeyMemberTables() {
		setDescription("Check for broken foreign-key relationships in the *member tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		boolean result = true;
		// gene_member table
		result &= checkForOrphans(con, "gene_member", "genome_db_id", "genome_db", "genome_db_id");
		result &= checkForOrphans(con, "gene_member", "taxon_id", "ncbi_taxa_node", "taxon_id");
		result &= checkForOrphans(con, "gene_member", "taxon_id", "ncbi_taxa_name", "taxon_id");
		result &= checkOptionalRelation(con, "gene_member", "dnafrag_id", "dnafrag", "dnafrag_id");
		// seq_member table
		result &= checkOptionalRelation(con, "seq_member", "gene_member_id", "gene_member", "gene_member_id");
		result &= checkOptionalRelation(con, "seq_member", "genome_db_id", "genome_db", "genome_db_id");
		result &= checkForOrphans(con, "seq_member", "taxon_id", "ncbi_taxa_node", "taxon_id");
		result &= checkForOrphans(con, "seq_member", "taxon_id", "ncbi_taxa_name", "taxon_id");
		result &= checkOptionalRelation(con, "seq_member", "dnafrag_id", "dnafrag", "dnafrag_id");
		result &= checkOptionalRelation(con, "seq_member", "sequence_id", "sequence", "sequence_id");
		result &= checkForOrphans(con, "other_member_sequence", "seq_member_id", "seq_member", "seq_member_id");
		return result;
	}

} // ForeignKeyMemberTables
