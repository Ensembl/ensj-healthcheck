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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;

public class EGForeignKeyFamilyMemberId extends AbstractEGForeignKeyMemberId {
	
	public EGForeignKeyFamilyMemberId() {
		super();
	}
	
	@Override
	protected String getTargetTable() {
		return "family_member";
	}
	
	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		if (!tableHasRows(con, getTargetTable())) {
			return true;
		}
		return super.run(dbre);
	}
	
	@Override
	protected boolean executeMoreChecks(DatabaseRegistryEntry dbre) {
		String targetTable = getTargetTable();
		String sourceTable = getSourceTable();
		String field = getSourceField();
		boolean result = true;
    Connection con = dbre.getConnection();
    
		result &= checkForOrphansWithConstraint(con, sourceTable, field, 
				targetTable, field, 
				"source_name in ('Uniprot/SWISSPROT', 'Uniprot/SPTREMBL', 'ENSEMBLPEP')");
    result &= checkForOrphansWithConstraint(con, sourceTable, field, 
    		targetTable, field, 
    		"source_name='ENSEMBLGENE' and member.member_id in (SELECT gene_member_id FROM member m2 WHERE m2.source_name='ENSEMBLPEP')");
    
		return result;
	}
}
