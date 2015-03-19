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

import java.util.HashMap;

import org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetIdStats;

public class MethodLinkSpeciesSetIdStatsHomology extends MethodLinkSpeciesSetIdStats {

	protected HashMap<String,String[]> getMandatoryTags() {
		HashMap<String,String[]> mandatoryTags = new HashMap<String,String[]>();
		String[] tags_orthologies = {
			"n_many-to-many_groups", "n_many-to-many_pairs",
			"n_many-to-one_groups", "n_many-to-one_pairs",
			"n_one-to-many_groups", "n_one-to-many_pairs",
			"n_one-to-one_groups", "n_one-to-one_pairs"
		};
		mandatoryTags.put("ENSEMBL_ORTHOLOGUES", tags_orthologies);

		String[] tags_paralogies = {
			"n_gene_split_genes", "n_gene_split_groups", "n_gene_split_pairs", "avg_gene_split_perc_id",
			"n_within_species_paralog_genes", "n_within_species_paralog_groups", "n_within_species_paralog_pairs", "avg_within_species_paralog_perc_id"
		};
		mandatoryTags.put("ENSEMBL_PARALOGUES", tags_paralogies);

		return mandatoryTags;
	}

}
