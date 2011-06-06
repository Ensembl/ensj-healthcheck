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
