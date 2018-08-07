package org.ensembl.healthcheck.testcase.compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class MemberProductionValues extends AbstractTemplatedTestCase {

    public MemberProductionValues() {
        setTeamResponsible(Team.COMPARA);
        appliesToType(DatabaseType.COMPARA);
        setDescription("Checks whether the gene_member_hom_stats table has been properly populated");
    }

    @Override
    protected boolean runTest(DatabaseRegistryEntry dbre) {
        boolean result = true;

        result = computeMissingValues (dbre);

        return result;
    }

    protected boolean computeMissingValues(DatabaseRegistryEntry dbre) {
        boolean result = true;

        //Get the total counts
        String sql_1 = "SELECT gene_member_id FROM gene_member_hom_stats WHERE collection = 'murinae'";
        int totalCount = DBUtils.getRowCount(dbre.getConnection(), sql_1);

        //Get the missing counts
        String sql_2 = "SELECT gene_member_id FROM gene_member_hom_stats WHERE gene_trees = 1 AND gene_gain_loss_trees = 0 AND orthologues = 0 AND paralogues = 0 AND homoeologues = 0";
        int missingCount = DBUtils.getRowCount(dbre.getConnection(), sql_2);

        // Will fail if more than 0.1% of values are missing
        if(missingCount > (totalCount*0.001)) {
            ReportManager.problem(this, dbre.getConnection(), "Too many missing counts (>0.1%) in gene_member_hom_stats. Wrong homology counts.");
            result = false;
        }
        return result;
    }
}
