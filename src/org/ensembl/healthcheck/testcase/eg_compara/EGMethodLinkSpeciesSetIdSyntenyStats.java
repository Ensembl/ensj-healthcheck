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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGMethodLinkSpeciesSetIdSyntenyStats extends
		AbstractTemplatedTestCase {

	private final static String QUERY = "SELECT method_link_species_set_id FROM "
			+ "(SELECT mlss.method_link_species_set_id, tc.stats_related_tags "
			+ "FROM method_link_species_set mlss "
			+ "INNER JOIN method_link ml ON mlss.method_link_id = ml.method_link_id "
			+ "LEFT JOIN (SELECT count(*) stats_related_tags, method_link_species_set_id "
			+ "FROM method_link_species_set_tag WHERE tag IN "
			+ "('non_reference_species','non_ref_coding_exon_length','non_ref_covered',"
			+ "'non_ref_genome_coverage','non_ref_genome_length','non_ref_uncovered',"
			+ "'num_blocks','reference_species','ref_coding_exon_length','ref_covered',"
			+ "'ref_genome_coverage','ref_genome_length','ref_uncovered') "
			+ "GROUP BY method_link_species_set_id) as tc "
			+ "ON mlss.method_link_species_set_id = tc.method_link_species_set_id "
			+ "WHERE ml.type = 'SYNTENY' "
			+ "HAVING (stats_related_tags != 13) OR (stats_related_tags IS NULL)) as find_missing_stats";

	public EGMethodLinkSpeciesSetIdSyntenyStats() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether stats have been generated for all synteny MLSSs");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		final List<String> mlsss = getTemplate(dbre).queryForDefaultObjectList(
				QUERY, String.class);
		if (mlsss.size() > 0) {
			ReportManager.problem(
					this,
					dbre.getConnection(),
					"Synteny MLSSs found with no statistics: "
							+ StringUtils.join(mlsss, ","));
			result = false;
		}
		return result;
	}

}
