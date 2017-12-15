package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Healthcheck for the assembly_exception table.
 */

/*
 * Assembly patches should only match to one seq region on the primary assembly.
 */

public class AssemblyExceptionTableUniqueRegion extends SingleDatabaseTestCase {


    public AssemblyExceptionTableUniqueRegion() {
        setDescription("Check assembly_exception table");
        setTeamResponsible(Team.GENEBUILD);

    }

    public void types() {

        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    /**
     * Check the data in the assembly_exception table. Note referential integrity checks are done in CoreForeignKeys.
     * 
     * @param dbre
     *          The database to use.
     * @return Result.
     */

    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        result &= uniqueRegion(dbre);

        return result;
    }


    private boolean uniqueRegion(DatabaseRegistryEntry dbre) {

        boolean result = false;

        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        Connection con = dbre.getConnection();
        String unique_sql = "SELECT distinct sr.name "
            + "FROM seq_region sr, assembly_exception ax, seq_region sr2, dna_align_feature daf, analysis a "
            + "WHERE a.analysis_id = daf.analysis_id "
            + "AND daf.seq_region_id = sr.seq_region_id "
            + "AND ax.seq_region_id = sr.seq_region_id "
            + "AND ax.exc_seq_region_id = sr2.seq_region_id "
            + "AND logic_name = 'alt_seq_mapping' "
            + "AND exc_type not in ('PAR') AND sr2.name != hit_name" ;
        
        List<String> unique_regions = t.queryForDefaultObjectList(unique_sql, String.class);

        if (unique_regions.isEmpty()) {
             result = true;
        }

        for (String region: unique_regions) {
             String msg = String.format("Assembly exception %s maps to more than one reference region", region);
             ReportManager.problem(this, dbre.getConnection(), msg);
        }

        return result;
    }
}