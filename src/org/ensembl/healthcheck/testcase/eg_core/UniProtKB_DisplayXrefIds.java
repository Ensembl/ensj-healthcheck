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

package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Created by IntelliJ IDEA. User: axk Date: 15/03/2011 Time: 10:21
 */
public class UniProtKB_DisplayXrefIds extends AbstractRowCountTestCase {

	public UniProtKB_DisplayXrefIds() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	private final static String QUERY = "SELECT count(*) "
			+ "FROM gene g, xref x, external_db d "
			+ "WHERE g.display_xref_id = x.xref_id AND x.external_db_id = d.external_db_id "
			+ "AND d.db_name IN ('Uniprot/SPTREMBL','Uniprot/SPTREMBL_predicted','Uniprot/SWISSPROT','Uniprot/SWISSPROT_predicted')";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
	 * ()
	 */
	@Override
	protected int getExpectedCount() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return QUERY;
	}

	@Override
	protected String getErrorMessage(int count) {
		return count
				+ " gene display_xref_ids attached to Uniprot/*, instead of Uniprot_genename";
	}

}
