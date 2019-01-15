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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;

/**
 * Check that all the data are attached to principal GenomeDBs
 */

public class NoDataOnGenomeComponents extends AbstractComparaTestCase {

	public NoDataOnGenomeComponents() {
		setDescription("Check that all the data are attached to principal GenomeDBs.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		
		boolean is_master_db = isMasterDB(con);

		boolean result = true;
		
		if (!is_master_db) {

			// The only MLSS that is allowed to have component GenomeDBs is protein-trees (401)
			result &= checkCountIsZero(con, "genome_db JOIN species_set USING (genome_db_id) JOIN method_link_species_set USING (species_set_id)", "genome_component IS NOT NULL AND method_link_id != 401");

			// All the alignments, syntenies, genes etc should be attached to the principal GenomeDBs
			String[] tables = {"genomic_align", "dnafrag_region", "constrained_element", "gene_member", "seq_member"};
			for (String t : tables) {
				result &= checkCountIsZero(con, "genome_db JOIN dnafrag USING (genome_db_id) JOIN " + t + " USING (dnafrag_id)", "genome_component IS NOT NULL");
			}
		}
		
		return result;
	}

} // NoDataOnGenomeComponents
