/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.healthcheck.testcase.compara;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;
import static org.ensembl.healthcheck.ReportManager.problem;
import static org.ensembl.healthcheck.util.CollectionUtils.createArrayList;

import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test case used to look for ProteinTrees where we have leaves whose parent
 * node is the root, we have more than 1 and there is an internal tree structure
 * therefore this looks to be a very suspect tree. We also try to look for flat
 * trees where all members have the root as their parent and have more than
 * getMaxAllowedFlatMembers() in this count.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class CheckFlatProteinTrees extends AbstractTemplatedTestCase {

	public CheckFlatProteinTrees() {
		setDescription("Look for trees which have internal nodes but all members' parent is the root");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Returns 3 which is the maximum number of elements we allow in a tree before
	 * considering it to be <em>dodgy</em>
	 */
	protected int getMaxAllowedFlatMembers() {
		return 2;
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passed = true;

		SqlTemplate template = getTemplate(dbre);

		MapRowMapper<Long, Long> mapRowMapper = new DefaultMapRowMapper<Long, Long>(
				Long.class, Long.class);

		// Counts all nodes which do not represent a seq_member, do not share the same
		// id
		// as their root and whose root id is not 0. Groups this by the root_id
		String internalNodeCountSql = "SELECT gtn.root_id, count(*) AS internal_nodes FROM gene_tree_node gtn WHERE gtn.seq_member_id IS NULL AND gtn.node_id <> gtn.root_id AND gtn.root_id <> 0 GROUP BY gtn.root_id";
		// Counts all members per tree where root id is not 0, whose parent id is
		// the same as the root id but have more than one of these per tree
		String flatMemberCountSql = "SELECT gtn.root_id, count(*) AS root_members FROM gene_tree_node gtn WHERE gtn.seq_member_id IS NOT NULL AND gtn.parent_id = gtn.root_id AND gtn.root_id <> 2 GROUP BY gtn.root_id having root_members > 1";
		// Count all members where root is not 0
		String memberCountSql = "SELECT root_id, count(distinct root_id) from gene_tree_node where root_id <> 0 group by root_id";
		// Selects all the non-rooted trees
		String nonrootedTreesSql = "SELECT root_id, 1 FROM gene_tree_root WHERE tree_type = 'tree' AND clusterset_id LIKE '%\\_it\\_%' AND clusterset_id NOT LIKE 'pg\\_it\\_%' ";

		Long totalTreesCount = template.queryForDefaultObject(
				"select count(*) from gene_tree_root", Long.class);


		Map<Long, Long> internalNodeCounts = template.queryForMap(
				internalNodeCountSql, mapRowMapper);
		Map<Long, Long> flatMemberCounts = template.queryForMap(flatMemberCountSql,
				mapRowMapper);
		Map<Long, Long> memberCounts = template.queryForMap(memberCountSql,
				mapRowMapper);
		Map<Long, Long> nonrootedTrees = template.queryForMap(nonrootedTreesSql,
				mapRowMapper);

		List<Long> flatMembersWithInternalStructure = createArrayList();
		List<Long> flatTreesStructure = createArrayList();

		for (Map.Entry<Long, Long> entry : flatMemberCounts.entrySet()) {
			// If we have an entry then we have a suspect tree
			Long nodeId = entry.getKey();
			Long internalNodeCount = internalNodeCounts.get(nodeId);
			if (internalNodeCount != null) {
				if (! nonrootedTrees.containsKey(nodeId)) {
					problem(this, dbre.getConnection(), format("%d has a problem: %d", nodeId, nonrootedTrees.get(nodeId)));
					flatMembersWithInternalStructure.add(nodeId);
				}
			}
			else if (entry.getValue() > getMaxAllowedFlatMembers()
					&& entry.getValue().equals(memberCounts.get(nodeId))) {
				flatTreesStructure.add(nodeId);
			}
		}

		if (!flatMembersWithInternalStructure.isEmpty()) {
			reportProblem(dbre, flatMembersWithInternalStructure, totalTreesCount,
					"have more than one seq_member joined to the root and a well formed internal tree structure");
			passed = false;
		}
		if (!flatTreesStructure.isEmpty()) {
			reportProblem(dbre, flatTreesStructure, totalTreesCount,
					"are flat with more than 2 members");
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
