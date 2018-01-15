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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGCheckNoTreeStableIds extends AbstractTemplatedTestCase {

	public EGCheckNoTreeStableIds() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks that all protein gene trees have a stable id");
	}
	private final static String COUNT_NULLS = "SELECT COUNT(*) " +
			"FROM gene_tree_root " +
        "WHERE member_type = 'protein' " +
        "AND tree_type = 'tree' " +
        "AND clusterset_id='default' " +
        "AND stable_id IS NULL";
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean pass = true;
		
		Integer count = getTemplate(dbre).queryForDefaultObject(COUNT_NULLS, Integer.class);
		if(count > 0) {
			String message = String.format("%d protein gene tree(s) lacked a stable ID. Sql to check is '%s'", count, COUNT_NULLS);
			ReportManager.problem(this, dbre.getConnection(), message);
			pass = false;
		}
		return pass;
	}
	
}
