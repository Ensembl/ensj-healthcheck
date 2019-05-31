/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that there the interpro table is populated and there are interpro
 * protein features
 */

public class InterproFeatures extends SingleDatabaseTestCase {

    /**
     * Create a new InterproFeatures testcase.
     */
    public InterproFeatures() {

        setDescription("Check that there the interpro table is populated and "
            + "there are interpro protein features");
        setTeamResponsible(Team.GENEBUILD);
        removeAppliesToType(DatabaseType.OTHERFEATURES);

    }

    /**
     * This only really applies to core databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.CDNA);
        removeAppliesToType(DatabaseType.RNASEQ);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *            The database to use.
     * @return true if the test passed.
     * 
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        
        Connection con = dbre.getConnection();

        if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM interpro") == 0) {

            ReportManager.problem(this, con, "InterPro table is empty");
            result &= false;

        }

        if(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature "
            + "JOIN interpro ON (id=hit_name)") == 0) {

            ReportManager.problem(this, con,
                "No InterPro protein features found");
            result &= false;

        }
        
        return result;

    } // run

}
