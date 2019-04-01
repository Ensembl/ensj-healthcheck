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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check that all top-level seq regions have some gene density features, and that the values agree between the
 * density_feature and seq_region attrib tables. Only checks top-level seq regions that do NOT have an _ in their names. Also checks
 * that there are some density features for each analysis/density type. Also checks that there are no duplicates in the
 * seq_region_attrib table.
 */

public class VariationDensity extends SingleDatabaseTestCase {

        // max number of top-level seq regions to check
        private static final int MAX_TOP_LEVEL = 100;

	// map between analysis.logic_name and seq_region attrib_type.code
	@SuppressWarnings("rawtypes")
  private Map logicNameToAttribCode = new HashMap();

	/**
	 * Create a new DensityFeatures testcase.
	 */
	@SuppressWarnings("unchecked")
  public VariationDensity() {

		setDescription("Check that all top-level seq regions have some variation density features, and that the values agree between the density_feature and seq_region attrib tables.");
		setFailureText("If the genome has been assembled using short-read sequences, some seq_regions might not have density_features");

                logicNameToAttribCode.put("SnpDensity", "SnpCount");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	// ----------------------------------------------------------------------

	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.EST);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.SANGER_VEGA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**Integer.valueOf(
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	@SuppressWarnings("unchecked")
  public boolean run(DatabaseRegistryEntry dbre) {
		
		boolean result = true;

		Connection con = dbre.getConnection();		
                SqlTemplate t = DBUtils.getSqlTemplate(dbre);

                boolean variationDatabaseExists = checkDatabaseExistsByType(dbre,DatabaseType.VARIATION);
                if (!variationDatabaseExists) {
                        return result;
                }
// Density features needed only for species with a karyotype
                String sqlKaryotype = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank'";
                int karyotype = t.queryForDefaultObject(sqlKaryotype, Integer.class);

                if (karyotype == 0) {
                        return result;
                }

		result &= checkFeaturesAndCounts(con);

		result &= checkAnalysisAndDensityTypes(dbre);

		return result;

	} // run

	// ----------------------------------------------------------------------

	@SuppressWarnings("rawtypes")
  private boolean checkFeaturesAndCounts(Connection con) {

		boolean result = true;

		// get top level co-ordinate system ID
		String sql = "SELECT coord_system_id FROM coord_system WHERE rank=1 LIMIT 1";

		String s = DBUtils.getRowColumnValue(con, sql);
                String logicName = "SnpDensity";
                String attribCode = "SnpCount";

		if (s.length() == 0) {
			logger.warning("Error: can't get top-level co-ordinate system for " + DBUtils.getShortDatabaseName(con));
			return false;
		}

		int topLevelCSID = Integer.parseInt(s);

		try {

			// check each top-level seq_region (up to a limit) to see how many density
			// features there are
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT s.seq_region_id, s.name, CASE WHEN ae.seq_region_id IS NULL THEN 0 ELSE 1 END as exception FROM seq_region_attrib sa, attrib_type at, seq_region s LEFT JOIN assembly_exception ae ON s.seq_region_id = ae.seq_region_id WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND at.code = 'karyotype_rank' AND coord_system_id=" + topLevelCSID + " AND (exc_type IN ('HAP', 'PAR') or exc_type IS NULL) GROUP BY s.seq_region_id, s.name, exception");

			int numTopLevel = 0;
                        int noDensity = 0;

			while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {

				long seqRegionID = rs.getLong("s.seq_region_id");
				String seqRegionName = rs.getString("s.name");
				boolean assemblyException = rs.getBoolean("exception");
				logger.fine("Counting density features on seq_region " + seqRegionName);

				sql = "SELECT COUNT(*) FROM density_feature WHERE seq_region_id=" + seqRegionID;
				int dfRows = DBUtils.getRowCount(con, sql);
				if (dfRows == 0) {

                                        noDensity++;
				}

				// check if this species has appropriate density features
				int analRows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM analysis WHERE logic_name='" + logicName + "'");
				if (analRows == 0) {
					logger.fine(DBUtils.getShortDatabaseName(con) + " has no " + logicName + " analysis type, skipping checks for these features");
				} else {

					// check that the sum of the density_feature.density_value matches
					// what
					// is in the seq_region_attrib table

					logger.fine("Comparing density_feature.density_value with seq_region_attrib for " + logicName + " features on " + seqRegionName);

					sql = "SELECT SUM(df.density_value) FROM density_type dt, density_feature df, analysis a WHERE dt.density_type_id=df.density_type_id AND dt.analysis_id=a.analysis_id AND a.logic_name='"
								+ logicName + "' AND seq_region_id=" + seqRegionID;

					String sumDF = DBUtils.getRowColumnValue(con, sql);
					// System.out.println(sql + " " + sumDF);
					//don't check the sum for haplotypes or PAR regions
					if (sumDF != null && sumDF.length() > 0 && !assemblyException) {

						long sumFromDensityFeature = Long.parseLong(sumDF);
						sql = "SELECT value FROM seq_region_attrib sra, attrib_type at WHERE sra.attrib_type_id=at.attrib_type_id AND at.code='" + attribCode + "' AND seq_region_id=" + seqRegionID;
						String sumSRA = DBUtils.getRowColumnValue(con, sql);
						// System.out.println(sql + " " + sumSRA);
						if (sumSRA != null && sumSRA.length() > 0) {

							long valueFromSeqRegionAttrib = Long.parseLong(sumSRA);
							if (Math.abs(sumFromDensityFeature - valueFromSeqRegionAttrib) > 1000) { // allow a bit of leeway

								ReportManager.problem(this, con, "Sum of values for " + logicName + " from density_feature (" + sumFromDensityFeature + ") doesn't agree with value from seq_region_attrib ("
											+ valueFromSeqRegionAttrib + ") for " + seqRegionName);
								result = false;

							}

						} // if sumSRA
                                                if (sumSRA.length() == 0) {
                                                        ReportManager.problem(this, con, seqRegionName + " has no seq_region_attrib for " + attribCode);
                                                        result = false;
                                                }

					} // if sumDF

				} // if rows

			} // while rs.next

                        if (noDensity > 0) {
                                ReportManager.problem(this, con, noDensity + " of the " + MAX_TOP_LEVEL + " first toplevel regions have no density features");
                                result = false;
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

	/**
	 * Check that each analysis_id is used at least in one density_type.
	 */
	private boolean checkAnalysisAndDensityTypes(DatabaseRegistryEntry dbre) {

		boolean result = true;
		
		Connection con = dbre.getConnection();

		String logicName = "SnpDensity";
		String sql = "SELECT dt.density_type_id FROM analysis a, density_type dt WHERE a.analysis_id=dt.analysis_id AND a.logic_name='" + logicName + "'";

		String[] rows = DBUtils.getColumnValues(con, sql);
		if (rows.length == 0) {
			result = false;
		}

		return result;

	}


	// ----------------------------------------------------------------------

} // DensityFeatures
