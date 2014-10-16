/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

/**
 * File: CheckPairAlignerStats.java
 * Created by: ckong
 * Created on: Oct 10, 2014
 * 
 * CVS:  $$
 */

package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * 
 * Checks that stats have been generated for DNA alignments in compara
 * Output mlss ID that failed the checks.
 * 
 * @author ckong 
 */
public class CheckPairAlignerStats extends AbstractTemplatedTestCase {

	public CheckPairAlignerStats() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		setDescription("Check that stats has been generated for all DNA alignments in compara");
		appliesToType(DatabaseType.COMPARA);
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
				
		String sql = "SELECT method_link_species_set_id FROM"
				+ " (SELECT mlss.method_link_species_set_id, tc.stats_related_tags "
				+ " FROM method_link_species_set mlss"
				+ " INNER JOIN method_link ml ON mlss.method_link_id = ml.method_link_id"
				+ " LEFT JOIN"
				+ " (SELECT count(*) stats_related_tags, method_link_species_set_id "
				+ " FROM method_link_species_set_tag"
				+ " WHERE tag IN ('non_ref_coding_exon_length','non_ref_genome_coverage',"
				+ " 'non_ref_genome_length','non_ref_insertions','non_ref_matches',"
				+ " 'non_ref_mis_matches','non_ref_uncovered', 'ref_coding_exon_length',"
				+ " 'ref_genome_coverage','ref_genome_length','ref_insertions',"
				+ " 'ref_matches','ref_mis_matches','ref_uncovered')"
				+ " GROUP BY method_link_species_set_id) as tc"
				+ " ON mlss.method_link_species_set_id = tc.method_link_species_set_id"
				+ " WHERE ml.type IN ('BLASTZ_NET', 'LASTZ_NET')"
				+ " HAVING (stats_related_tags != 14) "
				+ " OR (stats_related_tags IS NULL)) as find_missing_stats";
		
		List<Long> mlssIds = getTemplate(dbre).queryForDefaultObjectList(sql, Long.class);
		boolean ok = mlssIds.isEmpty();

		for(Long id: mlssIds) {
			ReportManager.problem(this, dbre.getConnection(), 
					"MLSS ID "+id+" is missing pairAligner stats");
		}
		return ok;
	}
}
		