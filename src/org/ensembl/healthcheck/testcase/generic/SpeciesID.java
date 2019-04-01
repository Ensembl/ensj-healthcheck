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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the species_id column in the meta table (and others) is set consistently.
 */
public class SpeciesID extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of SpeciesID
	 */
	public SpeciesID() {

		setDescription("Check that the species_id column in the meta table is set consistently.");
		setPriority(Priority.AMBER);
		setEffect("Could cause problems in multi-species databases");
		setFix("Manually fix affected keys, e.g. UPDATE TABLE meta SET species_id = NULL WHERE meta_key IN ( 'patch', 'schema_version' );");
		setTeamResponsible(Team.GENEBUILD); // No longer valid for funcgen

	}
	
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] keys = { "schema_version", "patch" };

		Connection con = dbre.getConnection();

		String sql = "SELECT COUNT(*) FROM meta WHERE meta_key=? AND species_id IS NOT NULL";

		try {

			PreparedStatement stmt = con.prepareStatement(sql);

			for (int i = 0; i < keys.length; i++) {

				String key = keys[i];
				stmt.setString(1, key);

				ResultSet rs = stmt.executeQuery();

				rs.first();
				int rows = rs.getInt(1);

				if (rows > 0) {
					result = false;
					ReportManager.problem(this, con, "Meta table has " + rows + " rows where " + key + " has a non-NULL value");
				} else {
					ReportManager.correct(this, con, "All " + key + " rows in meta table have null values");
				}

				rs.close();

			}

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}
		return result;

	} // run

} // SpeciesID

