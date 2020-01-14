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
 * Checks database does not contain duplicated Publication entries
 */
public class Publication extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Publication
	 */
	public Publication() {

		setDescription("Check the database does not contain duplicated Publication entries");
		setTeamResponsible(Team.VARIATION);

	}

	/**
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
			 * Attempt to extract a list of publications with the same unique id
			 */


		    // fields to check on: 
		    String[] check_id = {"pmid", "pmcid", "doi" }; 


		    for (int i =0; i< 3; i++){

			String stmt = "select count(*) from publication p1, publication p2 where p1." + check_id[i] + " = p2." + check_id[i] + " and p1.publication_id < p2.publication_id;";
			
                        int rows = DBUtils.getRowCount(con,stmt);
                        if (rows > 0) {
			    result = false;
			    ReportManager.problem(this, con, String.valueOf(rows) + " rows are duplicated in publication on " + check_id[i]);
                        }
			

		    }		
		} catch (Exception e) {
		    ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
		    result = false;
		}


                try {
                    // Look for publications without titles
                    String stmt = "select count(*) from publication where title is null";
                        int rows = DBUtils.getRowCount(con,stmt);
                        if (rows > 0) {
                            result = false;
                            ReportManager.problem(this, con, String.valueOf(rows) + " publications have no title ");
                        }
                } catch (Exception e) {
                    ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
                    result = false;
                }



	        // Look for variations or variation_features with display = 0
                boolean variation_ok = checkNonDisplay("variation", con);
	        if(variation_ok == false){
		 	result = false;
	       		ReportManager.problem(this, con, "cited variants have variation.display =0");
	       	    }

                boolean varfeat_ok = checkNonDisplay("variation_feature", con);
  	        if(varfeat_ok == false){
        	 	result = false;
	       		ReportManager.problem(this, con, "cited variants have variation_feature.display =0");
	       	    }

			
		
		if (result) {
		    // if there were no problems, just inform for the interface to pick the HC
		    ReportManager.correct(this, con, "Publication healthcheck passed without any problem");
		}
		return result;
	 } 
    


    private boolean checkNonDisplay( String input, Connection con ) {

             String stmt = "select count(*) from " + input + " ,variation_citation where " + input + ".variation_id = variation_citation.variation_id and " + input + ".display = 0";

             boolean result = true;
	     int rows = DBUtils.getRowCount(con,stmt);
	     if (rows > 0) {
	       	result = false;		
	    	}
	     return result;
	}
}
