package org.ensembl.healthcheck.testcase.compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.DBUtils;

public class MemberProductionValues extends AbstractTemplatedTestCase {

	protected int totalCount;

	public MemberProductionValues() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks whether the gene_member_hom_stats table has been properly populated");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getSqlTemplate(dbre);
		boolean result = true;

        getTotalCould(dbre);
		result = computeMissingValues (dbre, totalCount);

		return result;
	}

    //Get count
	protected void getTotalCould(DatabaseRegistryEntry dbre) {
		String sql = "SELECT gene_member_id FROM gene_member_hom_stats WHERE collection = 'murinae'";
		totalCount = DBUtils.getRowCount(dbre.getConnection(), sql);
	}

	protected boolean computeMissingValues(DatabaseRegistryEntry dbre, int expectedCount) {
		boolean result = true;
		String sql = String.format(
            "SELECT gene_member_id FROM gene_member_hom_stats WHERE "+
                " gene_trees = 1 AND gene_gain_loss_trees = 0 AND orthologues = 0" + 
                " AND paralogues = 0 AND homoeologues = 0");

		int missingCount = DBUtils.getRowCount(dbre.getConnection(), sql);

        // Will fail if more than 1% of values are missing
		if(missingCount > (totalCount*0.001)) {
			ReportManager.problem(this, dbre.getConnection(), "Too many missing counts (>1%) in gene_member. Wrong homology counts in gene_member_hom_stats.");
			result = false;
        }
		return result;
	}
}











//String sql = "SELECT gmhs.gene_member_id, gene_count, root_id, taxon_id FROM gene_member_hom_stats AS gmhs JOIN gene_member USING(gene_member_id) JOIN gene_tree_node ON (seq_member_id=canonical_member_id) JOIN gene_tree_root_attr USING(root_id)";
//List<String[]> data = DBUtils.getRowValuesList(comparaCon, sql);
//data = DBUtils.getRowValuesList(comparaCon, sql);
//for (String[] line : data) {
//    System.out.println(">>>" + line[0] + "<<<");
//}
