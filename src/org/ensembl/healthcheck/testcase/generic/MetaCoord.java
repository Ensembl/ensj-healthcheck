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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in.
 */
public class MetaCoord extends SingleDatabaseTestCase {

	private String[] featureTables = getCoreFeatureTables();

	/**
	 * Create a new instance of MetaCoord.
	 */
	public MetaCoord() {

		setDescription("Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in");
		setTeamResponsible(Team.GENEBUILD);
    setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
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

				logger.finest("Getting feature coordinate systems and max_length for " + tableName);
				ResultSet rs = stmt.executeQuery(sql);

				while (rs.next()) {

					String coordSystemID = rs.getString(1);
					logger.finest("Added feature coordinate system for " + tableName + ": " + coordSystemID);

					// check that the meta_coord table has an entry corresponding to this
					int mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta_coord WHERE coord_system_id=" + coordSystemID + " AND table_name='" + tableName + "'");

					if (mc == 0) {

						ReportManager.problem(this, con, "No entry for coordinate system with ID " + coordSystemID + " for " + tableName + " in meta_coord");
						result = false;

					} else if (mc > 1) {

						ReportManager.problem(this, con, "Coordinate system with ID " + coordSystemID + " duplicated for " + tableName + " in meta_coord");
						result = false;

					}

					// store in coordSystems map - create List if necessary
					List<String> csList = coordSystems.get(tableName);

					if (csList == null) {

						csList = new ArrayList<String>();

					}

					csList.add(coordSystemID);
					coordSystems.put(tableName, csList);
									
					// check that the max_length value in meta_coord corresponds to max feature length in each table per coord_system 
					String mc_max_length = DBUtils.getRowColumnValue(con, "SELECT max_length FROM meta_coord WHERE coord_system_id=" + coordSystemID + " AND table_name='" + tableName + "'");					
					String f_max_length = DBUtils.getRowColumnValue(con, "SELECT ABS(MAX((cast(f.seq_region_end as signed) - cast(f.seq_region_start as signed)) + 1)) FROM " + tableName + " f JOIN seq_region s USING(seq_region_id) WHERE s.coord_system_id=" + coordSystemID);
					
					if (!mc_max_length.equals(f_max_length)) {
						ReportManager.problem(this, con, "max_length value " + mc_max_length + " incorrect for coordinate system with ID " + coordSystemID + " for table " + tableName + " in meta_coord; max_length should equal "+ f_max_length);
						result = false;
					}
					

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
