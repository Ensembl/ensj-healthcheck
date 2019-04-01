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
 * relationships in the homology tables of the Compara schema.
 */

public class ForeignKeyHomologyTables extends AbstractComparaTestCase {

	public ForeignKeyHomologyTables() {
		setDescription("Check for broken foreign-key relationships in the homology tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		boolean result = true;

		result &= checkForOrphans(con, "homology_member", "homology_id", "homology", "homology_id");
		result &= checkForOrphans(con, "homology", "homology_id", "homology_member", "homology_id");
		result &= checkForOrphans(con, "homology_member", "seq_member_id", "seq_member", "seq_member_id");
		result &= checkForOrphans(con, "homology_member", "gene_member_id", "gene_member", "gene_member_id");
		result &= checkOptionalRelation(con, "homology", "gene_tree_node_id", "gene_tree_node", "node_id");
		result &= checkOptionalRelation(con, "homology", "gene_tree_root_id", "gene_tree_root", "root_id");
		result &= checkOptionalRelation(con, "homology", "species_tree_node_id", "species_tree_node", "node_id");

		return result;
	}

} // ForeignKeyHomologyTables
