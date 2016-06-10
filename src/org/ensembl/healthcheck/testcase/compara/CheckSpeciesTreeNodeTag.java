/*
 * Copyright [1999-2016] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case for SpeciesTreeNodeTag entries
 */

public class CheckSpeciesTreeNodeTag extends SingleDatabaseTestCase {

	public CheckSpeciesTreeNodeTag() {
		setDescription("Tests that entries are present in species_tree_node_tag");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "species_tree_node_tag")) {
			ReportManager.problem(this, con, "species_tree_node_tag table not present");
			return false;
		}

		if (!tableHasRows(con, "species_tree_root")) {
			ReportManager.info(this, con, "species_tree_root table is empty");
			return true;
		}

		boolean result = true;
		result &= checkCountIsNonZero(con, "species_tree_node_tag", "tag LIKE 'root\\_%'");
		result &= checkCountIsNonZero(con, "species_tree_node_tag", "tag LIKE 'nb%\\_genes%'");
		return result;
	}
}
