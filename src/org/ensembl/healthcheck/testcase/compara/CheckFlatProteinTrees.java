package org.ensembl.healthcheck.testcase.compara;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;
import static org.ensembl.healthcheck.ReportManager.problem;
import static org.ensembl.healthcheck.util.CollectionUtils.createArrayList;

import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test case used to look for ProteinTrees where we have leaves whose parent
 * node is the root, we have more than 1 and there is an internal tree structure
 * therefore this looks to be a very suspect tree. We also try to look for flat
 * trees where all members have the root as their parent and have more than
 * {@link #getMaxAllowedFlatMembers()} in this count.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class CheckFlatProteinTrees extends AbstractTemplatedTestCase {

	public CheckFlatProteinTrees() {
		addToGroup("compara_homology");
		setDescription("Look for trees which have internal nodes but all members' parent is the root");
		setTeamResponsible("compara");
	}

	/**
	 * Returns 3 which is the maximum number of elements we allow in a tree before
	 * considering it to be <em>dodgy</em>
	 */
	protected int getMaxAllowedFlatMembers() {
		return 3;
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passed = true;

		SqlTemplate template = getTemplate(dbre);

		MapRowMapper<Long, Long> mapRowMapper = new DefaultMapRowMapper<Long, Long>(
				Long.class, Long.class);

		// Counts all nodes which do not represent a member, do not share the same
		// id
		// as their root and whose root id is not 0. Groups this by the root_id
		String internalNodeCountSql = "SELECT ptn.root_id, count(*) AS internal_nodes FROM protein_tree_node ptn LEFT JOIN protein_tree_member ptm USING (node_id) WHERE ptm.node_id IS NULL AND ptn.node_id <> ptn.root_id AND ptn.root_id <> 0 GROUP BY ptn.root_id";
		// Counts all members per tree where root id is not 0, whose parent id is
		// the same as the root id but have more than one of these per tree
		String flatMemberCountSql = "SELECT ptn.root_id, count(*) AS root_members FROM protein_tree_node ptn JOIN protein_tree_member ptm USING (node_id) WHERE ptn.parent_id = ptn.root_id AND ptn.root_id <> 0 GROUP BY ptn.root_id having root_members > 1";
		// Count all members where root is not 0
		String memberCountSql = "SELECT root_id, count(distinct root_id) from protein_tree_node where root_id <> 0 group by root_id";

		Long totalTreesCount = template.queryForDefaultObject(
				"select count(distinct root_id) from protein_tree_node", Long.class);

		Map<Long, Long> internalNodeCounts = template.queryForMap(
				internalNodeCountSql, mapRowMapper);
		Map<Long, Long> flatMemberCounts = template.queryForMap(flatMemberCountSql,
				mapRowMapper);
		Map<Long, Long> memberCounts = template.queryForMap(memberCountSql,
				mapRowMapper);

		List<Long> flatMembersWithInternalStructure = createArrayList();
		List<Long> flatTreesStructure = createArrayList();

		for (Map.Entry<Long, Long> entry : flatMemberCounts.entrySet()) {
			// If we have an entry then we have a suspect tree
			Long nodeId = entry.getKey();
			Long internalNodeCount = internalNodeCounts.get(nodeId);
			if (internalNodeCount != null) {
				flatMembersWithInternalStructure.add(nodeId);
			}
			else if (entry.getValue() > getMaxAllowedFlatMembers()
					&& entry.getValue() == memberCounts.get(nodeId)) {
				flatTreesStructure.add(nodeId);
			}
		}

		if (flatMembersWithInternalStructure.isEmpty()
				&& flatTreesStructure.isEmpty()) {
			ReportManager.correct(this, dbre.getConnection(), "PASSED ");
		}
		else {
			if (!flatMembersWithInternalStructure.isEmpty()) {
				reportProblem(dbre, flatMembersWithInternalStructure, totalTreesCount,
						"have more than one member joined to the root and a well formed internal tree structure");
			}
			if (!flatTreesStructure.isEmpty()) {
				reportProblem(dbre, flatTreesStructure, totalTreesCount,
						"are flat with more than 2 members");
			}
			passed = false;
		}

		return passed;
	}

	private void reportProblem(DatabaseRegistryEntry dbre, List<Long> ids,
			Long totalTrees, String customMessage) {
		String badIdsString = join(ids, ',');
		String msg = format(
				"%d trees out of %d %s. Suspect root ids are: [%s]", ids.size(),
				totalTrees, customMessage, badIdsString);
		problem(this, dbre.getConnection(), msg);
	}
}
