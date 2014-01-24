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
 * Check that certain views/tables required for the Biomart build are present.
 */

public class PreMartTables extends SingleDatabaseTestCase {
	
	/**
	 * Constructor.
	 */
	public PreMartTables() {

		// TODO - group specifically for this?
		setDescription("Check that certain views/tables required for the Biomart build are present.");
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
				
				ReportManager.correct(this, con, "Requried table " + table + " exists.");
				
			} else {
				
				ReportManager.problem(this, con, "Required table or view " + table + " does not exist");
				result = false;
				
			}
			
		}
		
		// in the case of probestuff_helper_tmp, check that it has rows
		if (DBUtils.checkTableExists(con, "probestuff_helper_tmp") && !tableHasRows(con, "probestuff_helper_tmp")) {
			ReportManager.problem(this, con, "probestuff_helper_tmp is empty");
		}
		
		return result;

	} // run

} // PreMartTablesAndViews
