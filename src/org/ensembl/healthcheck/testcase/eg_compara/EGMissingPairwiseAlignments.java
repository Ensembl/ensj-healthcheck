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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGMissingPairwiseAlignments extends
		AbstractTemplatedTestCase {

	private final static String QUERY = "SELECT method_link_species_set_id FROM "
	    + "(SELECT mlss.method_link_species_set_id, distinct_mlss_ga.method_link_species_set_id ga_mlss "
	    + "FROM method_link_species_set mlss "
	    + "LEFT JOIN (SELECT DISTINCT method_link_species_set_id FROM genomic_align) as distinct_mlss_ga "
	    + "ON mlss.method_link_species_set_id = distinct_mlss_ga.method_link_species_set_id "
	    + "INNER JOIN method_link ml "
	    + "ON mlss.method_link_id = ml.method_link_id "
	    + "WHERE ml.type IN ('BLASTZ_NET', 'LASTZ_NET', 'TRANSLATED_BLAT_NET', 'ATAC') "
	    + "HAVING distinct_mlss_ga.method_link_species_set_id IS NULL) AS find_missing_ga";


	public EGMissingPairwiseAlignments() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks for method_link_species_sets with a pairwise method_link for which there are no genomic_aligns");
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
					"No genomic_aligns for pairwise MLSSs: "
							+ StringUtils.join(mlsss, ","));
			result = false;
		}
		return result;
	}

}
