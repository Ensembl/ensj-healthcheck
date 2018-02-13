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
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks database tables do not have only one value in key columns.
 */
public class CheckNumerous extends SingleDatabaseTestCase {

	public CheckNumerous() {

		setDescription("Check the database does not contain tables with columns with unexpectedly only one value");
		setTeamResponsible(Team.VARIATION);

	}

	/**
  	 * 
  	 * @param dbre
         * The database to check.
    	 * @return True if the test passed
	 */
         public boolean run(final DatabaseRegistryEntry dbre) {
	     boolean result = true;

		Connection con = dbre.getConnection();

		try {

                    // tables to check
                    String[] check_tab = {"variation_feature", "structural_variation_feature", "phenotype_feature", "variation"};
                    // columns to check
                    String[] check_col = {"seq_region_id",     "seq_region_id",                 "seq_region_id",    "class_attrib_id" }; 

                    for (int i =0; i< 4; i++){

		       String stmt = "select count(distinct " + check_col[i] +")  from " + check_tab[i] ;
	
                       int rows = DBUtils.getRowCount(con,stmt);
                       if (rows == 1) {
            		    result = false;
	        	    ReportManager.problem(this, con, "Only " + String.valueOf(rows) + " different values in " + check_tab[i] + "." + check_col[i] );
                       }	

		    }		
		} catch (Exception e) {
		    ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
		    result = false;
		}

            return result;
      }
}

