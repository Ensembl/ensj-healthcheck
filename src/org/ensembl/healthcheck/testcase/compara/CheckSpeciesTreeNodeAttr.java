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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case for SpeciesTreeNodeAttr entries
 */

public class CheckSpeciesTreeNodeAttr extends SingleDatabaseTestCase {

	public CheckSpeciesTreeNodeAttr() {
		setDescription("Tests that entries are present in species_tree_node_attr");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "species_tree_node_attr")) {
			ReportManager.problem(this, con, "species_tree_node_attr table not present");
			return false;
		}

		if (!tableHasRows(con, "species_tree_root")) {
			ReportManager.info(this, con, "species_tree_root table is empty");
			return true;
		}

		boolean result = true;
		result &= checkCountIsNonZero(con, "species_tree_node_attr", "root_nb_trees > 0");
		result &= checkCountIsNonZero(con, "species_tree_node_attr", "nb_genes > 0");
		return result;
	}
}
