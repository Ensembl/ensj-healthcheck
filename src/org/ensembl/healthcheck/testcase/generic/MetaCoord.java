/*
 Copyright (C) 2004 EBI, GRL

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in.
 */
public class MetaCoord extends SingleDatabaseTestCase {

	private String[] featureTables = getCoreFeatureTables();

	/**
	 * Create a new instance of MetaCoord.
	 */
	public MetaCoord() {

		addToGroup("release");
		addToGroup("post_genebuild");
		setDescription("Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
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

		// coordSystems is a hash of lists of coordinate systems that each feature table contains
		Map<String, List<String>> coordSystems = new HashMap<String, List<String>>();

		try {

			Statement stmt = con.createStatement();

			// build up a list of all the coordinate systems that are in the various feature tables
			for (String tableName : featureTables) {
				String sql = "";
				if (dbre.getType() == DatabaseType.SANGER_VEGA) {
					sql = "SELECT DISTINCT(sr.coord_system_id) FROM seq_region sr join coord_system cs on sr.coord_system_id = cs.coord_system_id, " + tableName
							+ " f WHERE sr.seq_region_id = f.seq_region_id and cs.version like 'VEGA%' ";
				} else {
					sql = "SELECT DISTINCT(sr.coord_system_id) FROM seq_region sr, " + tableName + " f WHERE sr.seq_region_id = f.seq_region_id";
				}

				logger.finest("Getting feature coordinate systems for " + tableName);
				ResultSet rs = stmt.executeQuery(sql);

				while (rs.next()) {

					String coordSystemID = rs.getString(1);
					logger.finest("Added feature coordinate system for " + tableName + ": " + coordSystemID);

					// check that the meta_coord table has an entry corresponding to this
					int mc = getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE coord_system_id=" + coordSystemID + " AND table_name='" + tableName + "'");

					if (mc == 0) {

						ReportManager.problem(this, con, "No entry for coordinate system with ID " + coordSystemID + " for " + tableName + " in meta_coord");
						result = false;

					} else if (mc > 1) {

						ReportManager.problem(this, con, "Coordinate system with ID " + coordSystemID + " duplicated for " + tableName + " in meta_coord");
						result = false;

					} else {

						ReportManager.correct(this, con, "Coordinate system with ID " + coordSystemID + " for table " + tableName + " has an entry in meta_coord");

					}

					// store in coordSystems map - create List if necessary
					List<String> csList = coordSystems.get(tableName);

					if (csList == null) {

						csList = new ArrayList<String>();

					}

					csList.add(coordSystemID);
					coordSystems.put(tableName, csList);

				}

				rs.close();

			}

			// check that every meta_coord table entry refers to a coordinate system that is used in a feature
			// if this isn't true it's not fatal but should be flagged
			String sql = "SELECT * FROM meta_coord";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String tableName = rs.getString("table_name");
				String csID = rs.getString("coord_system_id");
				logger.finest("Checking for coord_system_id " + csID + " in " + tableName);

				List<String> featureCSs = coordSystems.get(tableName);
				if (featureCSs != null && !featureCSs.contains(csID)) {
					ReportManager.problem(this, con, "meta_coord has entry for coord_system ID " + csID + " in " + tableName + " but this coordinate system is not actually used in " + tableName);
					result = false;
				}

			}

			rs.close();
			stmt.close();

			// check that there are no null max_length entries
			result &= checkNoNulls(con, "meta_coord", "max_length");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

}
