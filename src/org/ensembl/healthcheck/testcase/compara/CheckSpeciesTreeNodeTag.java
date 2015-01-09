/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**                                                                                                                                                                                 
 * An EnsEMBL Healthcheck test case for SpeciesTreeNodeTag entries
 */

public class CheckSpeciesTreeNodeTag extends SingleDatabaseTestCase {

	public CheckSpeciesTreeNodeTag() {
		addToGroup("compara_homology");
		setDescription("Tests that proper entries are in method_link_species_set_tag.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "species_tree_node_tag")) {
			ReportManager.problem(this, con, "species_tree_node_tag table not present");
			return false;
		}

		// These methods return false if there is any problem with the test
		boolean result = true;
		result &= checkTreeStatsArePresent(dbre);
		return result;
	}

	public boolean checkTreeStatsArePresent(final DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		if (!tableHasRows(con, "species_tree_root")) {
			return true;
		}

		int n_tags_root = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM species_tree_node_tag WHERE tag LIKE 'root\\_%'");
		int n_tags_genes = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM species_tree_node_tag WHERE tag LIKE 'nb%\\_genes%'");

		boolean result = true;
		if (n_tags_root == 0) {
			ReportManager.problem(this, con, "There are no species_tree_node_tags to describe properties of the root nodes");
			result = false;
		} else if (n_tags_genes == 0) {
			ReportManager.problem(this, con, "There are no species_tree_node_tags to summarize the gene counts");
			result = false;
		}
		return result;
	}
}
