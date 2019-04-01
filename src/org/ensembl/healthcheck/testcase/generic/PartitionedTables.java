/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check if any tables are partitioned. None should be yet, until their definitions are updated in table.sql.
 */
public class PartitionedTables extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of PartitionedTables
	 */
	public PartitionedTables() {

		setDescription("Check whether tables have been partitioned.");
		
		setPriority(Priority.AMBER);
		setEffect("Tables should only be partitioned if the partitions are defined in table.sql.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	/**
	 * Run the test.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW TABLE STATUS WHERE create_options LIKE '%partitioned%'");

			while (rs.next()) {

				String table = rs.getString(1);
				ReportManager.problem(this, con, table + " is partitioned but shouldn't be.");
				result = false;

			}

			rs.close();

			stmt.close();

		} catch (Exception e) {

			String msg = "Could not get partioned table status";
			logger.severe(msg);
			throw new RuntimeException(msg,e);
		}

		if (result == true) {

			ReportManager.correct(this, con, "No tables are partitioned");
		}

		return result;

	} // run

	// -----------------------------------------------------------------------

} // PartitionedTables

