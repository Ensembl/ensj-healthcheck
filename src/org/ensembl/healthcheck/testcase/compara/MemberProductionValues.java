package org.ensembl.healthcheck.testcase.compara;

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;

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

        // For each collection, get the percentage of genes that are in a tree but without any homologues
        String sql = "SELECT collection, 100 * SUM(gene_trees > 0 AND orthologues = 0 AND paralogues = 0 AND homoeologues = 0) / SUM(gene_trees > 0) FROM gene_member_hom_stats GROUP BY collection";
        DefaultMapRowMapper<String, Double> mapper = new DefaultMapRowMapper<String, Double>(String.class, Double.class);
        Map<String,Double> percentageMissingData = getSqlTemplate(dbre).queryForMap(sql, mapper);

        boolean result = true;
        for (String collection : percentageMissingData.keySet()) {
            // Fail if more than 0.1% of values are missing
            if (percentageMissingData.get(collection) > 0.1) {
                ReportManager.problem(this, dbre.getConnection(), "Too many genes without homologues (" + percentageMissingData.get(collection) + "%) in gene_member_hom_stats for the collection " + collection + ". Wrong homology counts.");
                result = false;
            }
        }
        return result;
    }
}
