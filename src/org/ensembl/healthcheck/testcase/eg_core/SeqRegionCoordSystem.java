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

package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for identically-named seq_regions in different co-ordinate systems. Also check that identically-named seq_regions have the
 * same length.
 */

public class SeqRegionCoordSystem extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionCoordSystem testcase.
	 */
	public SeqRegionCoordSystem() {

		addToGroup("compara-ancestral");
		
		setDescription("Check for identically-named seq_regions in different co-ordinate systems. Also check that identically-named seq_regions have the same length.");
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

		if (dbre.isMultiSpecies()) {
			logger.finest("Skipping " + getShortTestName() + " healthcheck for multi-species database " + dbre.getName());
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
				ResultSet rs = stmt.executeQuery("SELECT coord_system_id, name, version FROM coord_system where attrib like '%default_version%' and species_id=" + speciesId);
				while (rs.next()) {
					coordSystems.put(new Long(rs.getLong("coord_system_id")), rs.getString("name") + ":" + rs.getString("version"));
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}

			// check each pair in turn
			Long[] coordSystemIDs = (Long[]) coordSystems.keySet().toArray(new Long[coordSystems.size()]);
			for (int i = 0; i < coordSystemIDs.length; i++) {
				for (int j = i + 1; j < coordSystemIDs.length; j++) {

					String csI = (String) coordSystems.get(coordSystemIDs[i]);
					String csJ = (String) coordSystems.get(coordSystemIDs[j]);

					int same = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM seq_region s1, seq_region s2 WHERE s1.coord_system_id=" + coordSystemIDs[i] + " AND s2.coord_system_id=" + coordSystemIDs[j]
							+ " AND s1.name = s2.name");

					if (same > 0) {

						ReportManager.problem(this, con, "Co-ordinate systems " + csI + " and " + csJ + " have " + same + " identically-named seq_regions - this may cause problems for ID mapping");
						result = false;

					} else {

						ReportManager.correct(this, con, "Co-ordinate systems " + csI + " and " + csJ + " have no identically-named seq_regions");

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

			String query = "SELECT COUNT(*) FROM seq_region s1, seq_region s2, coord_system c1, coord_system c2 " + "WHERE s1.name=s2.name AND s1.coord_system_id != s2.coord_system_id "
					+ "AND c1.coord_system_id=s1.coord_system_id AND c2.coord_system_id=s2.coord_system_id " + "AND s1.length != s2.length and c1.species_id=" + speciesId + " and c2.species_id=" + speciesId
          + " AND c1.attrib like '%default_version%' AND c2.attrib like '%default_version%'";
			// for vega, only report if they are on the same assembly
			if (dbre.getType() == DatabaseType.SANGER_VEGA || dbre.getType() == DatabaseType.VEGA) {
				query += " and c1.version=c2.version";
			}
			int rows = DBUtils.getRowCount(con, query);
			if (rows > 0) {

				ReportManager.problem(this, con, rows + " seq_regions have the same name but different lengths for species " + speciesId);
				result = false;

			} else {

				ReportManager.correct(this, con, "All seq_region lengths match for species " + speciesId);

			}
		}

		return result;

	}

} // SeqRegionCoordsystem

