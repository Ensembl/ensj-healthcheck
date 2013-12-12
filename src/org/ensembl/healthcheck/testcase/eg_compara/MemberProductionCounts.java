package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class MemberProductionCounts extends AbstractTemplatedTestCase {

	public MemberProductionCounts() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether member_production_counts is populated");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getSqlTemplate(dbre);
		boolean result = true;
		if(srv.queryForDefaultObject("SELECT COUNT(*) FROM member_production_counts INNER JOIN member USING (stable_id)", Integer.class)==0) {
			ReportManager.problem(this, dbre.getConnection(), "No entries found in member_production_counts linked to member");
			result = false;
		}
		Integer cnt = srv.queryForDefaultObject("SELECT COUNT(*) FROM member_production_counts WHERE stable_id NOT IN (SELECT stable_id FROM member WHERE source_name = 'ENSEMBLGENE')", Integer.class);
		if(cnt>0) {
			ReportManager.problem(this, dbre.getConnection(), cnt+" entries found in member_production_counts that are not linked to member");
			result = false;
		}
		return result;
	}

}
