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

package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class MemberXrefAssociation extends AbstractTemplatedTestCase {

	public MemberXrefAssociation() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether member_xref is populated");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getSqlTemplate(dbre);
		boolean result = true;
		if(srv.queryForDefaultObject("select count(*) from member_xref join gene_member using (gene_member_id)", Integer.class)==0) {
			ReportManager.problem(this, dbre.getConnection(), "No entries found in member_xref linked to gene_member");
			result = false;
		}
		Integer cnt = srv.queryForDefaultObject("select count(*) from member_xref x left join gene_member m using (gene_member_id) where m.gene_member_id is null", Integer.class);
		if(cnt>0) {
			ReportManager.problem(this, dbre.getConnection(), cnt+" entries found in member_xref that are not linked to gene_member");
			result = false;
		}
		return result;
	}

}
