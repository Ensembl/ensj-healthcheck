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

/**
 * An EnsEMBL Healthcheck test case that looks for genomic mlss_ids that
 * are not linked to any data.
 */

public class ForeignKeyMLSSIdGenomic extends AbstractMLSSIdToData {

	public ForeignKeyMLSSIdGenomic() {
		setDescription("Check for missing links between method_link_species_set and the genomic tables.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		boolean result = true;

		/* Check method_link_species_set <-> genomic_align(_block) */
		/* All method_link for genomic-alignments must have an internal ID lower than 99 */
		result &= checkMLSSIdLink(con, "genomic_align", "method_link_id IN (SELECT method_link_id FROM method_link WHERE method_link_id < 100 AND class NOT LIKE 'ConstrainedElement.%' AND type NOT LIKE 'CACTUS_HAL%')");
		result &= checkMLSSIdLink(con, "genomic_align_block", "method_link_id IN (SELECT method_link_id FROM method_link WHERE method_link_id < 100 AND class NOT LIKE 'ConstrainedElement.%' AND type NOT LIKE 'CACTUS_HAL%')");

		/* Check method_link_species_set <-> constrained_element */
		/* All method_link for contrained elements must have an internal ID lower than 99 */
		result &= checkMLSSIdLink(con, "constrained_element", "method_link_id IN (SELECT method_link_id FROM method_link WHERE class LIKE 'ConstrainedElement.%')");

		/* Check method_link_species_set <-> synteny_region */
		/* All method_link for syntenies must have an internal ID between 101 and 199 */
		result &= checkMLSSIdLink(con, "synteny_region", "method_link_id >= 101 and method_link_id < 200");

		/* Check method_link_species_set <-> species_tree_root */
		result &= checkMLSSIdLink(con, "species_tree_root", "method_link_id IN (SELECT method_link_id FROM method_link WHERE (class LIKE 'GenomicAlignTree%' OR class LIKE '%multiple_alignment'))");

		return result;
	}

} // ForeignKeyMLSSIdGenomic
