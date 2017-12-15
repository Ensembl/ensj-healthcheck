/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

/**
 * Healthcheck for the assembly_exception table.
 */

/*
 * Test checking integrity of assembly exception table so it matches the
 * imported GRC alignments.
 *
 * This test should fail if the AssemblyExceptionTable SeqMapping test fails.
 */

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


public class AssemblyExceptionTableIntegrity extends SingleDatabaseTestCase {

    public AssemblyExceptionTableIntegrity() {
        setDescription("Check assembly_exception table");
        setTeamResponsible(Team.GENEBUILD);

    }

    public void types() {

        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    /**
     * 
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        result &= checkExternalDB(dbre);
        
        return result;
    }

    private boolean checkExternalDB(DatabaseRegistryEntry dbre) {

        boolean result = false;

        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        Connection con = dbre.getConnection();
        String unique_sql = "SELECT distinct sr.name "
            + "FROM seq_region sr, assembly_exception ax, external_db e, dna_align_feature daf, analysis a "
            + "WHERE a.analysis_id = daf.analysis_id "
            + "AND daf.seq_region_id = sr.seq_region_id "
            + "AND ax.seq_region_id = sr.seq_region_id "
            + "AND e.external_db_id = daf.external_db_id "
            + "AND logic_name = 'alt_seq_mapping' "
            + "AND exc_type not in ('PAR') AND e.db_name != 'GRC_primary_assembly'" ;

        List<String> unique_regions = t.queryForDefaultObjectList(unique_sql, String.class);

        if (unique_regions.isEmpty()) {
             result = true;
        }

        for (String region: unique_regions) {
             String msg = String.format("Assembly exception %s has a mapping which is not from 'GRC_primary_assembly'", region);
             ReportManager.problem(this, dbre.getConnection(), msg);
        }

        return result;
    }
}