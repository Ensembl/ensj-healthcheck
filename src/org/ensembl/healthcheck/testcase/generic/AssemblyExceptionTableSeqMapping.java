/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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


/*
    split this test into multiple subtests, all in the AssemblyExection group

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

/**
 * Healthcheck for the assembly_exception table.
 */

/*
 * Assembly patches should only match to one seq region on the primary
 * assembly. The current logic name is 'seq_alt_mapping'.
*/

public class AssemblyExceptionTableSeqMapping extends SingleDatabaseTestCase {


    public AssemblyExceptionTableSeqMapping() {
        setDescription("Check assembly_exception table");
        setTeamResponsible(Team.GENEBUILD);

    }

    public void types() {

        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.VEGA);
        removeAppliesToType(DatabaseType.SANGER_VEGA);
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

        result &= seqMapping(dbre);

        return result;
    }

    private boolean seqMapping(DatabaseRegistryEntry dbre) {

        boolean result = false;

        SqlTemplate t  = DBUtils.getSqlTemplate(dbre);
        Connection con = dbre.getConnection();
        String all_sql = "SELECT distinct sr.name "
            + "FROM seq_region sr, assembly_exception ax "
            + "WHERE ax.seq_region_id = sr.seq_region_id "
            + "AND exc_type not in ('PAR')";
        
        List<String> all_exc = t.queryForDefaultObjectList(all_sql, String.class);

        String daf_sql = "SELECT distinct sr.name "
            + "FROM seq_region sr, assembly_exception ax, dna_align_feature daf, analysis a "
            + "WHERE sr.seq_region_id = ax.seq_region_id "
            + "AND exc_type not in ('PAR') AND sr.seq_region_id = daf.seq_region_id "
            + "AND daf.analysis_id = a.analysis_id AND a.logic_name = 'alt_seq_mapping'";

        List<String> daf_exc = t.queryForDefaultObjectList(daf_sql, String.class);

        Set<String> missing = new HashSet<String>(all_exc);
        missing.removeAll(daf_exc);

        if(missing.isEmpty()) {
             result = true;
        }
        for(String name: missing) {
             String msg = String.format("Assembly exception '%s' does not "
                + "have results in dna_align_feature table for analysis 'alt_seq_mapping'", name);
             ReportManager.problem(this, dbre.getConnection(), msg);
        }

        return result;
    }
}
