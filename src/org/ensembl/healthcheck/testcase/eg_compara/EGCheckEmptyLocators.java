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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGCheckEmptyLocators extends AbstractTemplatedTestCase {

	public EGCheckEmptyLocators() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks the consistency of GenomeDB locators");
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean pass = true;
		String sql = "select name from genome_db where locator IS NOT NULL";
		List<String> names = getTemplate(dbre).queryForDefaultObjectList(sql, String.class);
		if(! names.isEmpty()) {
			String joinedNames = StringUtils.join(names, ',');
			int count = names.size();
			String message = String.format("%d GenomeDB(s) [%s] did not have empty locators", count, joinedNames);
			ReportManager.problem(this, dbre.getConnection(), message);
			pass = false;
		}
		return pass;
	}
}
