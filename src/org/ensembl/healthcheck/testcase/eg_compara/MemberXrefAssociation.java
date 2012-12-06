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
		addToGroup("ensembl_genomes_compara");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getSqlTemplate(dbre);
		boolean result = true;
		if(srv.queryForDefaultObject("select count(*) from member_xref join member using (member_id)", Integer.class)==0) {
			ReportManager.problem(this, dbre.getConnection(), "No entries found in member_xref linked to member");
			result = false;
		}
		Integer cnt = srv.queryForDefaultObject("select count(*) from member_xref x left join member m using (member_id) where m.member_id is null", Integer.class);
		if(cnt>0) {
			ReportManager.problem(this, dbre.getConnection(), cnt+" entries found in member_xref that are not linked to member");
			result = false;
		}
		return result;
	}

}
