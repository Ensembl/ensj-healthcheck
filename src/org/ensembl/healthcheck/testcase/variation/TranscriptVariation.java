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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

// This HC seems a bit useless with Graham's new consequence pipeline. I'll disable it for now and we'll have to discuss what 
// we want checked before enabling it again.

/**
 * Check that if the peptide_allele_string of transcript_variation is not >1. 
 * It should out >1, unless it filled with numbers
 */
public class TranscriptVariation extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of Check Transcript Variation
     */
    public TranscriptVariation() {
        setDescription("Check that if the peptide_allele_string of transcript_variation is not >1. It should out >1, unless it filled with numbers");
		setTeamResponsible(Team.VARIATION);
    }

    /**
     * Find any matching databases that have peptide_allele_string column.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

	// check peptide_allele_string not filled with numbers

        Connection con = dbre.getConnection();
        int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM transcript_variation WHERE pep_allele_string >1");
        if (rows >=1) {
            result = false;
            ReportManager.problem(this, con, rows + " with peptide_allele_string >1");
        } else {
      //      ReportManager.info(this, con, "No transcript_variation have peptide_allele_string >1);
        }

	int rows1 = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM transcript_variation WHERE consequence_types=''");
        if (rows1 >=1) {
            result = false;
            ReportManager.problem(this, con, rows1 + " with consequence_types a empty string");
        } else {
      //      ReportManager.info(this, con, "No transcript_variation have consequence_type a empty string");
        }
        
	int rows2 = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM variation_feature vf WHERE NOT FIND_IN_SET('intergenic_variant',vf.consequence_types) AND NOT EXISTS (SELECT * FROM transcript_variation tv WHERE tv.variation_feature_id = vf.variation_feature_id)");
        if (rows2 >=1) {
	    result = false;
	    ReportManager.problem(this, con, rows2 + " with consequence_type != 'intergenic_variant' and there is no corresponding transcript exists in transcript_variation table");
	}
	if (result){
        	ReportManager.correct(this,con,"transcript_variation have peptide_allele_string >1, indicating column shift, peptide_allele_string become a number or consequence_type a empty string or consequence_type is not a intergenic_variant where there is no transcript exists");
        }

        return result;

    } // run

} // TranscriptVariation
