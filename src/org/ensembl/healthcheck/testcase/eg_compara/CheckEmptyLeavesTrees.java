package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Checks that all gene tree's leaves do not have any children i.e. we 
 * do not allow any leaf to have a left/right index difference greater
 * than 1.
 */
public class CheckEmptyLeavesTrees extends AbstractTemplatedTestCase {

	public CheckEmptyLeavesTrees() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		setDescription("Checks that all nodes where left/right index is greater than 1 have children");
		appliesToType(DatabaseType.COMPARA);
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		String sql = "select distinct ptn.root_id from gene_tree_node ptn left join gene_tree_node ptn2 on (ptn.node_id = ptn2.parent_id) where ptn2.node_id is null and (ptn.right_index -  ptn.left_index) > 1";
		List<Long> rootIds = getTemplate(dbre).queryForDefaultObjectList(
				sql, Long.class);
		boolean ok = rootIds.isEmpty();
		for(Long id: rootIds) {
			ReportManager.problem(this, dbre.getConnection(), 
					"Root tree ID "+id+" has leaves which have a left/right index greater than one but lack children");
		}
		return ok;
	}
}
