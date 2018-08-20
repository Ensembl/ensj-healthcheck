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

package org.ensembl.healthcheck.testcase.variation;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.Species;

/**
 * An EnsEMBL Healthcheck test case that looks for variation feature entries
 * with no alleles
 */

public class VariationFeatureAlleles extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public VariationFeatureAlleles() {

        setDescription("Check for variation features with no alleles.");
        setTeamResponsible(Team.VARIATION);

    }

    /**
     * Look for variation feature entries with no alleles
     * 
     * @param dbre
     *            The database to use.
     * @return true Ff all foreign key relationships are valid.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        // Alleles are no longer stored for human variants without frequency data
        Species species = dbre.getSpecies();
        if (species == Species.HOMO_SAPIENS ){
          return true;
        }

        return checkForOrphans(dbre.getConnection(), "variation_feature", "variation_id", "allele", "variation_id",
                true);
    }

} 
