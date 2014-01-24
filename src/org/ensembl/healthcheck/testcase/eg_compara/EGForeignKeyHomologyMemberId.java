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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

/**
 * Provides a check for the relationship between the member and homology
 * member table both on the member_id and peptide_member_id field
 * 
 * @author ayates
 */
public class EGForeignKeyHomologyMemberId extends AbstractEGForeignKeyMemberId {
	
	@Override
	protected String getTargetTable() {
		return "homology_member";
	}
	
	protected boolean executeMoreChecks(org.ensembl.healthcheck.DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		return checkForOrphans(con, getTargetTable(), "peptide_member_id", 
				getSourceTable(), getSourceField());
	}
}
