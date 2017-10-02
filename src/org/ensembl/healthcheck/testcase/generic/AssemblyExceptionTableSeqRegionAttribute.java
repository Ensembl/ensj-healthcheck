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

public class AssemblyExceptionTableSeqRegionAttribute extends SingleDatabaseTestCase {

    /**
     * Check the assembly_exception table.
     */



    public AssemblyExceptionTableSeqRegionAttribute() {
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

        result &= checkSeqRegionAttribute(dbre);

        return result;
    }

    private boolean checkSeqRegionAttribute(DatabaseRegistryEntry dbre) {

        Connection con = dbre.getConnection();
        boolean result = true;

        // If the assembly_exception table contains an exception of type 'HAP' then
        // there should be at least one seq_region_attrib row of type 'non-reference'
        if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception "
            + "WHERE exc_type='HAP'") > 0) {

            if (DBUtils.getRowCount(con, "SELECT COUNT(*) "
                + "FROM seq_region_attrib sra, attrib_type at "
                + "WHERE sra.attrib_type_id=at.attrib_type_id "
                + "AND at.code='non_ref'") == 0) {
                result = false;
                ReportManager.problem(this, con, "assembly_exception contains "
                    + "at least one exception of type 'HAP' but there are no seq_region_attrib rows of type 'non-reference'");
            }

        }

