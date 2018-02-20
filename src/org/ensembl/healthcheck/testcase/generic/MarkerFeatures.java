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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that: - marker features exist if markers exist - that map_wieghts are
 * set to non-zero values - all marker priorities are > 50 - each chromosome has
 * some marker features - each chromosome has some marker_map_locations
 * 
 * Currently only checks for human, mouse, rat and zebrafish.
 */

public class MarkerFeatures extends SingleDatabaseTestCase {

	// marker priority to warn if under
	private static final int MARKER_PRIORITY_THRESHOLD = 50;

	// max number of top-level seq regions to check
	private static final int MAX_TOP_LEVEL = 100;

	/**
	 * Creates a new instance of MarkerFeatures.
	 */
	public MarkerFeatures() {

		setDescription(
				"Checks that marker_features exist and that they have non-zero map_weights, that marker priorities are sensible and that all chromosomes have some marker features and marker_map_locations");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Test various things about marker features.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// only check for human, mouse, rat and zebrafish
		String s = dbre.getSpecies();
		if (s.equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				|| s.equals(DatabaseRegistryEntry.MUS_MUSCULUS) || s.equals(DatabaseRegistryEntry.RATTUS_NORVEGICUS)
				|| s.equals(DatabaseRegistryEntry.DANIO_RERIO)) { // for

			result &= checkFeaturesAndMapWeights(con);

			result &= checkAllChromosomesHaveMarkers(con);

		}

		return result;

	} // run

	// ----------------------------------------------------------------------
	/*
	 * Verify marker features exist if markers exist, and that map weights are
	 * non-zero.
	 */

	private boolean checkFeaturesAndMapWeights(Connection con) {

		boolean result = true;

		int rowCount = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM marker_feature");

		if (rowCount == 0) {
			ReportManager.problem(this, con, "No marker features in database even though markers are present");
			result = false;
		}

		int badWeightCount = DBUtils.getRowCount(con,
				"SELECT marker_id, COUNT(*) AS correct, map_weight FROM marker_feature GROUP BY marker_id HAVING map_weight != correct");

		if (badWeightCount > 0) {
			ReportManager.problem(this, con,
					badWeightCount + " marker features have not been assigned correct map weights");
			result = false;
		}

		if (result) {
			ReportManager.correct(this, con, "Marker features appear to be ok");
		}

		return result;

	} // checkFeaturesAndMapWeights

	// ----------------------------------------------------------------------
	/**
	 * Check that all chromomes have > 0 markers_map_locations and marker_features.
	 */
	private boolean checkAllChromosomesHaveMarkers(Connection con) {

		boolean result = true;

		// find all the chromosomes, and for each one check that it has some
		// markers
		// note a "chromosome" is assumed to be a seq_region that is:
		// - on the top-level co-ordinate system and
		// - doesn't have and _ or . in the name and
		// - has a seq_region name of less than 3 characters
		// - doesn't have a name starting with "Un" or "MT"

		// get top level co-ordinate system ID
		String sql = "SELECT coord_system_id FROM coord_system WHERE rank=1 LIMIT 1";

		String s = DBUtils.getRowColumnValue(con, sql);

		if (s.length() == 0) {
			System.err
					.println("Error: can't get top-level co-ordinate system for " + DBUtils.getShortDatabaseName(con));
			return false;
		}

		int topLevelCSID = Integer.parseInt(s);

		try {

			// check each top-level seq_region (up to a limit) to see how many
			// marker_map_locations and marker features there are
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM seq_region WHERE coord_system_id=" + topLevelCSID
					+ " AND name NOT LIKE '%\\_%' AND name NOT LIKE '%.%' AND name NOT LIKE 'Un%' AND name NOT LIKE 'MT%' AND LENGTH(name) < 3 ORDER BY name");

			int numTopLevel = 0;

			while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {

				long seqRegionID = rs.getLong("seq_region_id");
				String seqRegionName = rs.getString("name");

				// check marker_map_locations
				logger.fine("Counting marker_map_locations on chromosome " + seqRegionName);

				sql = "SELECT COUNT(*) FROM marker_map_location WHERE chromosome_name='" + seqRegionName + "'";
				int rows = DBUtils.getRowCount(con, sql);
				if (rows == 0) {

					ReportManager.problem(this, con, "Chromosome " + seqRegionName + " (seq_region_id " + seqRegionID
							+ ") has no entries in marker_map_location");
					result = false;

				} else {

					ReportManager.correct(this, con,
							"Chromosome " + seqRegionName + " has " + rows + " marker_map_locations");

				}

				// check marker_features
				logger.fine("Counting marker_features on chromosome " + seqRegionName);
				sql = "SELECT COUNT(*) FROM marker_feature WHERE seq_region_id=" + seqRegionID;
				rows = DBUtils.getRowCount(con, sql);
				if (rows == 0) {

					ReportManager.problem(this, con, "Chromosome " + seqRegionName + " (seq_region_id " + seqRegionID
							+ ") has no marker_features");
					result = false;

				} else {

					ReportManager.correct(this, con,
							"Chromosome " + seqRegionName + " has " + rows + " marker_features");

				}

			}

			rs.close();
			stmt.close();

			if (numTopLevel == MAX_TOP_LEVEL) {
				logger.warning("Only checked first " + numTopLevel + " seq_regions");
			}

		} catch (SQLException se) {
			se.printStackTrace();
		}

		return result;

	}

	// ----------------------------------------------------------------------

} // MarkerFeatures
