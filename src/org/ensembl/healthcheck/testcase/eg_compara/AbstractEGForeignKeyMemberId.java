package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Base class used for the member relationships code. This default version
 * checks the {@link #getTargetTable()} table that all of its members have an
 * entry in the member table.
 * 
 * @author ayates
 */
public abstract class AbstractEGForeignKeyMemberId extends AbstractTemplatedTestCase {
	
	public AbstractEGForeignKeyMemberId() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Check for broken foreign-key member relationships in ensembl_compara databases.");
		addToGroup("ensembl_genomes_compara");
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
		return "member";
	}
	
	protected String getSourceField() {
		return "member_id";
	}
	
	protected boolean executeMoreChecks(DatabaseRegistryEntry dbre) {
		return true;
	}
}
