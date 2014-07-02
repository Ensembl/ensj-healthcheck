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
		setDescription("Checks whether gene member counts have been populated");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getSqlTemplate(dbre);
		boolean result = true;
		if(srv.queryForDefaultObject("SELECT SUM(gene_trees) FROM gene_member", Integer.class)==0) {
			ReportManager.problem(this, dbre.getConnection(), "No counts in gene_member; run 'populate_member_production_counts_table.sql'");
			result = false;
		}
		return result;
	}

}
