/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that certain views/tables required for the Biomart build are not present.
 */

public class PostMartTables extends SingleDatabaseTestCase {
	
	/**
	 * Constructor.
	 */
	public PostMartTables() {

		// TODO - group specifically for this?
		setDescription("Check that certain views/tables required for the Biomart build are not present.");
                setTeamResponsible(Team.FUNCGEN);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		for (String table : getBiomartFuncgenTablesAndViews()) {
			
			if (DBUtils.checkTableExists(con, table)) {
				
				ReportManager.problem(this, con,  table + " exists but should have been removed after the Biomart build");
				result = false;

			} else {
				
				ReportManager.correct(this, con, table + " has been removed.");
				
			}
			
		}
		
		return result;

	} // run

} // PostMartTables
