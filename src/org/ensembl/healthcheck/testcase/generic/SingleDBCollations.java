/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all table collations in a particular database are of
 * the same type of specified in @{link {@link #getTargetCollation()}.
 */
public class SingleDBCollations extends SingleDatabaseTestCase {

	private static String TARGET_COLLATION = "latin1_swedish_ci";

	/**
	 * Create a new SingleDBCollations testcase.
	 */
	public SingleDBCollations() {
		setResponsibilities();
	}
	
	protected void setResponsibilities() {
		setDescription("Check that all table collations are "+getTargetCollation());
		setTeamResponsible(Team.RELEASE_COORDINATOR);
		setSecondTeamResponsible(Team.CORE);
	}
	
	/**
	 * Returns the target collation of latin1_swedish_ci
	 */
	protected String getTargetCollation() {
		return TARGET_COLLATION;
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
		String targetCollation = getTargetCollation();

		Connection con = dbre.getConnection();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.tables WHERE table_schema = DATABASE() and TABLE_COMMENT NOT LIKE '%VIEW%'");

			while (rs.next()) {
				String table = rs.getString("TABLE_NAME");
				String collation = rs.getString("TABLE_COLLATION");
				if (collation == null) {
					ReportManager.problem(this, con, "Can't get collation for " + table);
					result = false;
					continue;
				}
				if (!collation.equals(targetCollation)) {
					ReportManager.problem(this, con, table + " has a collation of '" + collation + "' which is not the same as the target " + targetCollation);
					result = false;
				}
			}

			rs.close();
			stmt.close();

		} catch (SQLException se) {
			se.printStackTrace();
		}

		if (result) {
			ReportManager.correct(this, con, "All tables have collation " + targetCollation);
		}

		return result;

	} // run

} // SingleDBCollations
