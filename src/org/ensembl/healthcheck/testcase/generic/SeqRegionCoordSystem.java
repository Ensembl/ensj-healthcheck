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
import java.util.HashMap;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for identically-named seq_regions in different co-ordinate systems.
 * Also check that identically-named seq_regions have the same length.
 */

public class SeqRegionCoordSystem extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionCoordSystem testcase.
	 */
	public SeqRegionCoordSystem() {

		addToGroup("id_mapping");
		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check for identically-named seq_regions in different co-ordinate systems. Also check that identically-named seq_regions have the same length.");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		if (dbre.isMultiSpecies()) {
			logger.finest("Skipping " + getShortTestName()
					+ " healthcheck for multi-species database "
					+ dbre.getName());
			return true;
		}

		if (dbre.getType() == DatabaseType.CORE) {
			result &= checkNames(dbre);
		}

		result &= checkLengths(dbre);

		return result;

	} // run

	private boolean checkNames(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

        // EG add loop to support multispecies databases
		for (int speciesId : dbre.getSpeciesIds()) {

			HashMap coordSystems = new HashMap();

			// build hash of co-ord system IDs to names & versions
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt
						.executeQuery("SELECT coord_system_id, name, version FROM coord_system where species_id="
								+ speciesId);
				while (rs.next()) {
					coordSystems.put(new Long(rs.getLong("coord_system_id")),
							rs.getString("name") + ":"
									+ rs.getString("version"));
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}

			// check each pair in turn
			Long[] coordSystemIDs = (Long[]) coordSystems.keySet().toArray(
					new Long[coordSystems.size()]);
			for (int i = 0; i < coordSystemIDs.length; i++) {
				for (int j = i + 1; j < coordSystemIDs.length; j++) {

					String csI = (String) coordSystems.get(coordSystemIDs[i]);
					String csJ = (String) coordSystems.get(coordSystemIDs[j]);

					int same = getRowCount(con,
							"SELECT COUNT(*) FROM seq_region s1, seq_region s2 WHERE s1.coord_system_id="
									+ coordSystemIDs[i]
									+ " AND s2.coord_system_id="
									+ coordSystemIDs[j]
									+ " AND s1.name = s2.name");

					if (same > 0) {

						ReportManager
								.problem(
										this,
										con,
										"Co-ordinate systems "
												+ csI
												+ " and "
												+ csJ
												+ " have "
												+ same
												+ " identically-named seq_regions - this may cause problems for ID mapping");
						result = false;

					} else {

						ReportManager.correct(this, con, "Co-ordinate systems "
								+ csI + " and " + csJ
								+ " have no identically-named seq_regions");

					}

				} // j

			} // i
		}
		return result;

	}

	private boolean checkLengths(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

        // EG add special code for dealing with multispecies databases
		for (int speciesId : dbre.getSpeciesIds()) {

			int rows = getRowCount(
					con,
					"SELECT COUNT(*) FROM seq_region s1, seq_region s2, coord_system c1, coord_system c2 " +
					"WHERE s1.name=s2.name AND s1.coord_system_id != s2.coord_system_id " +
					"AND c1.coord_system_id=s1.coord_system_id AND c2.coord_system_id=s2.coord_system_id " +
					"AND s1.length != s2.length and c1.species_id="
							+ speciesId + " and c2.species_id=" + speciesId);

			if (rows > 0) {

				ReportManager
						.problem(
								this,
								con,
								rows
										+ " seq_regions have the same name but different lengths for species "
										+ speciesId);
				result = false;

			} else {

				ReportManager
						.correct(this, con,
								"All seq_region lengths match for species "
										+ speciesId);

			}
		}

		return result;

	}

} // SeqRegionCoordsystem

