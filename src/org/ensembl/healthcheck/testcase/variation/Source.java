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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the source table is consistent
 */
public class Source extends SingleDatabaseTestCase {

	public Source() {

		addToGroup("variation-release");
		
		setDescription("Checks that the soucre table is consistent");
		setTeamResponsible(Team.VARIATION);

	}

	/*
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 *
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		boolean result = true;
		
		try {				

                   String versions_stmt = "select count(distinct version) from source where name like '%dbSNP%' ";
                   int versions = DBUtils.getRowCount(con,versions_stmt);
                   if (versions != 1) {
                       result = false;
                       ReportManager.problem(this, con,  versions + " different versions set for dbSNP sources ");
                   }
                }
                catch (Exception e) {
                       ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
                       result = false;
                }

		return result;
	}
}
