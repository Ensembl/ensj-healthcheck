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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Base class used for the member relationships code. This default version
 * checks the {@link AbstractEGForeignKeyMemberId#getTargetTable()} table that all of its members have an
 * entry in the member table.
 * 
 * @author ayates
 */
public abstract class AbstractEGForeignKeyMemberId extends AbstractTemplatedTestCase {
	
	public AbstractEGForeignKeyMemberId() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Check for broken foreign-key member relationships in ensembl_compara databases.");
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		String targetTable = getTargetTable();
		String sourceTable = getSourceTable();
		String field = getSourceField();
		
		boolean result = true;

    Connection con = dbre.getConnection();

    if (tableHasRows(con, sourceTable)) {
      result &= checkForOrphans(con, targetTable, field, sourceTable, field);
      result &= executeMoreChecks(dbre);
    }
		return result;
	}
	
	protected abstract String getTargetTable();
	
	protected String getSourceTable() {
		return "gene_member";
	}
	
	protected String getSourceField() {
		return "gene_member_id";
	}
	
	protected boolean executeMoreChecks(DatabaseRegistryEntry dbre) {
		return true;
	}
}
