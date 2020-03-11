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
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks phenotype_feature_attrib entries do not contain unsupported characters or unhelpful values.
 */
public class PhenotypeFeatureAttrib extends CheckChar  {

	
	public PhenotypeFeatureAttrib() {

		setDescription("Check that imported phenotype_feature_attrib values are reasonable");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Check phenotype_feature_attrib table
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed
	 */
         public boolean run(final DatabaseRegistryEntry dbre) {
	     boolean result = true;

		Connection con = dbre.getConnection();

		try {
			/*
			 * Will extract a list of phenotype_feature_attrib.values and check for oddness
			 */
		    boolean char_ok   = true;
		    
		    List<String> data = getSqlTemplate(con).queryForDefaultObjectList(
		        "select value from phenotype_feature_attrib", String.class);

		    for (int i = 0; i < data.size(); i++) {

			    String input = data.get(i);

                            // skip 'not provided' as it is a ClinVar status
			    if( input.equalsIgnoreCase( "not provided" ) ){continue;}

			    // check for descriptions suggesting no data
                            boolean name_ok = checkNonTerms(input);
			    if(name_ok == false){
				result = false;
				ReportManager.problem(this, con, "phenotype_feature_attrib : " + input + " is not useful");
			    }

			    // check for unsupported individual characters
			    char_ok = checkUnsupportedChar(input);
			    if(char_ok == false){
				result = false;
				ReportManager.problem(this, con, "phenotype_feature_attrib: \""+ input +"\" has unsupported characters or starts oddly");
			    }

		    }					    
	
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
			result = false;
		}
		if (result) {
			// if there were no problems, just inform for the interface to pick the HC
			ReportManager.correct(this, con, "PhenotypeFeatureAttrib healthcheck passed without any problem");
		}
		return result;
	} 

   

	// --------------------------------------
}
