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

/**
 * An EnsEMBL Healthcheck test case that looks for homology mlss_ids that
 * are not linked to any data.
 */

public class ForeignKeyMLSSIdHomology extends AbstractMLSSIdToData {

	public ForeignKeyMLSSIdHomology() {
		setDescription("Check for missing links between method_link_species_set and the homology tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		boolean result = true;

		/* Check method_link_species_set <-> homology */
		/* All method_link for homologies must have an internal ID between 201 and 299 */
		result &= checkMLSSIdLink(con, "homology", "method_link_id >= 201 and method_link_id < 300");

		/* Check method_link_species_set <-> family */
		/* All method_link for families must have an internal ID between 301 and 399 */
		result &= checkMLSSIdLink(con, "family", "method_link_id >= 301 and method_link_id < 400");

		/* Check method_link_species_set <-> gene_tree_root */
		result &= checkMLSSIdLink(con, "gene_tree_root", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
		// Here, we add "false" because gene_tree_root->method_link_species_set links have already been checked previously
		result &= checkMLSSIdLink(con, "gene_tree_root", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')", false);

		/* Check method_link_species_set <-> species_tree_root */
		result &= checkMLSSIdLink(con, "species_tree_root", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ProteinTree.%')");
		result &= checkMLSSIdLink(con, "species_tree_root", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'NCTree.%')", false);

		return result;
	}

} // ForeignKeyMLSSIdHomology
