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

import java.util.HashMap;

import org.ensembl.healthcheck.testcase.compara.AbstractMLSSTagStats;

public class MLSSTagStatsPairwiseAlignment extends AbstractMLSSTagStats {

	protected HashMap<String,String[]> getMandatoryTags() {
		HashMap<String,String[]> mandatoryTags = new HashMap<String,String[]>();
		String[] tags_pairwise = {
			"non_ref_coding_exon_length", "non_ref_genome_coverage","non_ref_genome_length",
			"non_ref_insertions", "non_ref_matches", "non_ref_mis_matches", "non_ref_uncovered",
			"ref_coding_exon_length", "ref_genome_coverage", "ref_genome_length", "ref_insertions",
			"ref_matches", "ref_mis_matches", "ref_uncovered"
		};
		mandatoryTags.put("BLASTZ_NET", tags_pairwise);
		mandatoryTags.put("LASTZ_NET", tags_pairwise);
		mandatoryTags.put("TRANSLATED_BLAT_NET", tags_pairwise);
		return mandatoryTags;
	}

}
