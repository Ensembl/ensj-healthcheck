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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check whether tables need to be analysed.
 */
public class AnalyseTables extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalyseTables
	 */
	public AnalyseTables() {

		setDescription("Check whether tables need to be analysed.");
		setPriority(Priority.AMBER);
		setEffect("Causes indices not to be used, making queries slow or unresponsive.");
		setFix("Run ANALYZE TABLE x.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}
	
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
	}

	/**
	 * Run the test.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] tables = DBUtils.getTableNames(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];

			int results = DBUtils.getRowCount(con, " SHOW INDEX FROM " + table + " WHERE CARDINALITY IS NULL");

			if (results > 0) {

				// Don't complain if the table is empty
				if (DBUtils.countRowsInTable(con, table) > 0) {
					ReportManager.problem(this, con, table + " needs to be analysed");
					result = false;
				}
			}
		}
		// Return a correct report line to make it easier to read report
		if (result)
			ReportManager.correct(this, con, "No tables need analysis");

		return result;

	} // run

	// -----------------------------------------------------------------------

} // AnalyseTables

