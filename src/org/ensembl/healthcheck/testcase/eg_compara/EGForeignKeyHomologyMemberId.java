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
