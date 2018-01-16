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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for duplicate synonym names which may have different import sources despite having the same original source
 */
public class VariationSynonym extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Check Variation Synonym
	 */
	public VariationSynonym() {
		setDescription("Check for duplicate variation synonyms");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Run the test
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		
		if (dbre.getSpecies() != Species.HOMO_SAPIENS) {
			ReportManager.info(this, con, "This test is only for human at moment");
			return true;
		}
		
		boolean result = true;
		
		try {
			
		    int rows = DBUtils.getRowCount(con, "select vs1.variation_synonym_id, vs1.variation_id, vs1.name from variation_synonym vs1, variation_synonym vs2 where  vs2.variation_id =  vs1.variation_id and vs2.name  = vs1.name and vs2.variation_synonym_id > vs1.variation_synonym_id and vs1.source_id = vs2.source_id");

		    if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " duplicate variation_synonyms detected");
		    } else {
			ReportManager.correct(this, con, "No duplicate variation_synonyms detected");
		    }
		
			
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an error: " + e.getMessage());
			result = false;
		}
				
		return result;

	} // run

} // VariationSynonym
