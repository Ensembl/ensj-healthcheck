package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGCheckNoTreeStableIds extends AbstractTemplatedTestCase {

	public EGCheckNoTreeStableIds() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks that all protein gene trees have a stable id");
		addToGroup("ensembl_genomes_compara");
	}
	private final static String COUNT_NULLS = "SELECT COUNT(*) " +
			"FROM gene_tree_root " +
        "WHERE member_type = 'protein' " +
        "AND tree_type = 'tree' " +
        "AND cluster_set_id='default' " +
        "AND stable_id IS NULL";
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean pass = true;
		
		Integer count = getTemplate(dbre).queryForDefaultObject(COUNT_NULLS, Integer.class);
		if(count > 0) {
			String message = String.format("%d protein gene tree(s) lacked a stable ID. Sql to check is '%s'", count, COUNT_NULLS);
			ReportManager.problem(this, dbre.getConnection(), message);
			pass = false;
		}
		return pass;
	}
	
}
