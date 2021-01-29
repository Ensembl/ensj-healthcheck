/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public abstract class AbstractMLSSTagStats extends AbstractTemplatedTestCase {

	private final static String QUERY = "SELECT method_link_species_set_id, stats_related_tags FROM "
			+ "(SELECT mlss.method_link_species_set_id, tc.stats_related_tags "
			+ "FROM method_link_species_set mlss "
			+ "INNER JOIN method_link ml ON mlss.method_link_id = ml.method_link_id "
			+ "LEFT JOIN (SELECT count(*) stats_related_tags, method_link_species_set_id "
			+ "FROM method_link_species_set_tag WHERE tag IN "
			+ "(%s) "
			+ "GROUP BY method_link_species_set_id) as tc "
			+ "ON mlss.method_link_species_set_id = tc.method_link_species_set_id "
			+ "WHERE ml.type = '%s' "
			+ "HAVING (stats_related_tags != %d) OR (stats_related_tags IS NULL)) as find_missing_stats";

	abstract protected HashMap<String,String[]> getMandatoryTags();

	public AbstractMLSSTagStats() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether stats have been generated for all MLSSs");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		for (Map.Entry<String, String[]> method_tags : getMandatoryTags().entrySet()) {
			Vector<String> quoted_tags = new Vector<String>();
			for (String t: method_tags.getValue()) {
				quoted_tags.add(String.format("'%s'", t));
			}
			List<String> mlsss = getTemplate(dbre).queryForDefaultObjectList(String.format(QUERY, StringUtils.join(quoted_tags, ","), method_tags.getKey(), method_tags.getValue().length), String.class);
			if (mlsss.size() > 0) {
				ReportManager.problem( this, dbre.getConnection(), "MLSSs for " + method_tags.getKey() + " found with no statistics: " + StringUtils.join(mlsss, ","));
				ReportManager.problem( this, dbre.getConnection(), "USEFUL SQL: " + String.format(QUERY, StringUtils.join(quoted_tags, ","), method_tags.getKey(), method_tags.getValue().length));
				result = false;
			} else {
				ReportManager.correct(this, dbre.getConnection(), "PASSED ");
			}
		}
		return result;
	}

}
