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
		setDescription("Checks that all trees have a stable id");
		addToGroup("ensembl_genomes_compara");
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean pass = true;
		String sql = "select count(*) " +
				"from protein_tree_node ptn " +
				"left join protein_tree_stable_id ptsi using (node_id) " +
				"where ptn.node_id = ptn.root_id and ptsi.node_id is null";
		Integer count = getTemplate(dbre).queryForDefaultObject(sql, Integer.class);
		if(count > 0) {
			String message = String.format("%d ProteinTree(s) lacked a stable ID. Sql to check is '%s'", count, sql);
			ReportManager.problem(this, dbre.getConnection(), message);
			pass = false;
		}
		return pass;
	}
	
}
