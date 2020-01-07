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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
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

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = super.runTest(dbre);
		// Every genome must have some coding genes
		String query_wrong_stats = "SELECT method_link_species_set_id FROM method_link_species_set_tag WHERE tag = 'non_ref_coding_exon_length' AND value = '0'";
		List<String> mlsss = getTemplate(dbre).queryForDefaultObjectList(query_wrong_stats, String.class);
		if (mlsss.size() > 0) {
			ReportManager.problem( this, dbre.getConnection(), "MLSSs found with wrong statistics: " + StringUtils.join(mlsss, ","));
			ReportManager.problem( this, dbre.getConnection(), "USEFUL SQL: " + query_wrong_stats);
			result = false;
		} else {
			ReportManager.correct(this, dbre.getConnection(), "PASSED ");
		}
		return result;
	}
}
