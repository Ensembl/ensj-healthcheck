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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships in the "master" tables of the Compara schema.
 */

public class ForeignKeyMasterTables extends AbstractComparaTestCase {

	public ForeignKeyMasterTables() {
		setDescription("Check for broken foreign-key relationships in the compara master tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		boolean result = true;
		// dnafrag
		result &= checkForOrphans(con, "dnafrag", "genome_db_id", "genome_db", "genome_db_id");
		// species set
		result &= checkForOrphans(con, "species_set", "genome_db_id", "genome_db", "genome_db_id");
		result &= checkForOrphansWithConstraint(con, "genome_db", "genome_db_id", "species_set", "genome_db_id", "taxon_id != 0");
		// method_link_species_set
		result &= checkForOrphans(con, "method_link_species_set", "method_link_id", "method_link", "method_link_id");
		result &= checkForOrphans(con, "method_link_species_set", "species_set_id", "species_set", "species_set_id");
		result &= checkForOrphansWithConstraint(con, "species_set", "species_set_id", "method_link_species_set", "species_set_id", "species_set_id not in (SELECT distinct species_set_id from species_set_tag)");
		// genome_db
		result &= checkForOrphansWithConstraint(con, "genome_db", "taxon_id", "ncbi_taxa_node", "taxon_id", "taxon_id != 0");
		result &= checkForOrphansWithConstraint(con, "genome_db", "taxon_id", "ncbi_taxa_name", "taxon_id", "taxon_id != 0");

		if (!isMasterDB(con)) {
			// The master database has the history of all method_links.
			// Some of them are not used any more, but they must stay
			// there. The following check would not apply in that case
			result &= checkForOrphans(con, "method_link", "method_link_id", "method_link_species_set", "method_link_id");
		}
		return result;
	}

} // ForeignKeyMasterTables
