/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * Check for DNA that is not stored on the sequence-level coordinate system.
 */

public class SequenceLevel extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public SequenceLevel() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check for DNA that is not stored on the sequence-level coordinate system.");
		setTeamResponsible(Team.GENEBUILD);
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
		String sql = "SELECT cs.name, COUNT(1) FROM coord_system cs, seq_region s, dna d WHERE d.seq_region_id = s.seq_region_id AND cs.coord_system_id =s.coord_system_id AND attrib NOT LIKE '%sequence_level%' GROUP BY cs.coord_system_id";

		try {

			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs == null) {

				ReportManager.correct(this, con, "All DNA is attached to a sequence-level co-ordinate system.");

			} else {

				while (rs.next()) {

					String coordSystem = rs.getString(1);
					int rows = rs.getInt(2);

					ReportManager.problem(this, con, String.format("Coordinate system %s has %d seq regions containing sequence, but it does not have the sequence_level attribute", coordSystem, rows));
					result = false;

				}

			}

		} catch (SQLException e) {
			System.err.println("Error executing: " + sql);
			e.printStackTrace();
		}

		return result;

	} // run

} // SequenceLevel
