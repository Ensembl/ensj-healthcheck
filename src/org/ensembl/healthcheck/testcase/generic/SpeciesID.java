/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the species_id column in the meta table (and others) is set
 * consistently.
 */
public class SpeciesID extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of SpeciesID
	 */
	public SpeciesID() {

		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("compara-ancestral");
		setDescription("Check that the species_id column in the meta table is set consistently.");
		setPriority(Priority.AMBER);
		setEffect("Could cause problems in multi-species databases");
		setFix("Manually fix affected keys, e.g. UPDATE TABLE meta SET species_id = NULL WHERE meta_key IN ( 'patch', 'schema_version' );");
		setTeamResponsible("core"); //No longer valid for funcgen 

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
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

