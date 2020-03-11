/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

public class DensityFeatures extends SingleDatabaseTestCase {

        // max number of top-level seq regions to check
        private static final int MAX_TOP_LEVEL = 100;

	// map between analysis.logic_name and seq_region attrib_type.code
	@SuppressWarnings("rawtypes")
  private Map logicNameToAttribCode = new HashMap();

	/**
	 * Create a new DensityFeatures testcase.
	 */
	@SuppressWarnings("unchecked")
  public DensityFeatures() {

		setDescription("Check that all top-level seq regions have some gene density features, and that the values agree between the density_feature and seq_region attrib tables.");
		setFailureText("If the genome has been assembled using short-read sequences, some seq_regions might not have density_features");

		logicNameToAttribCode.put("CodingDensity", "coding_cnt");
                logicNameToAttribCode.put("PseudogeneDensity", "pseudogene_cnt");
                logicNameToAttribCode.put("ShortNonCodingDensity", "noncoding_cnt_s");
                logicNameToAttribCode.put("LongNonCodingDensity", "noncoding_cnt_l");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	// ----------------------------------------------------------------------

	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.EST);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
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
		
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			logicNameToAttribCode.put("PCodDensity", "knownGeneCount");
                        logicNameToAttribCode.remove("CodingDensity");
                        logicNameToAttribCode.remove("PseudogeneDensity");
                        logicNameToAttribCode.put("ShortNonCodingDensity", "noncoding_cnt_s");
                        logicNameToAttribCode.put("LongNonCodingDensity", "noncoding_cnt_l");
		}

		boolean result = true;

		Connection con = dbre.getConnection();		
                SqlTemplate t = DBUtils.getSqlTemplate(dbre);

// Density features needed only for species with a karyotype
                String sqlKaryotype = "SELECT count(*) FROM seq_region_attrib sa, attrib_type at WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'karyotype_rank'";
                int karyotype = t.queryForDefaultObject(sqlKaryotype, Integer.class);

                if (karyotype == 0) {
                        return result;
                }

		result &= checkFeaturesAndCounts(con);

		result &= checkAnalysisAndDensityTypes(dbre);

		result &= checkDensityTypes(con);

		result &= checkFeatureSeqRegions(con);

		return result;

	} // run

	// ----------------------------------------------------------------------

	@SuppressWarnings("rawtypes")
  private boolean checkFeaturesAndCounts(Connection con) {

		boolean result = true;

		try {

			// check each top-level seq_region (up to a limit) to see how many density
			// features there are
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT s.seq_region_id, s.name, CASE WHEN ae.seq_region_id IS NULL THEN 0 ELSE 1 END as exception FROM seq_region_attrib sa, attrib_type at, seq_region_attrib sa2, attrib_type at2, seq_region s LEFT JOIN assembly_exception ae ON s.seq_region_id = ae.seq_region_id WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND at.code = 'karyotype_rank' AND s.seq_region_id = sa2.seq_region_id AND sa2.attrib_type_id = at2.attrib_type_id AND at2.code = 'toplevel' AND (exc_type IN ('HAP', 'PAR') or exc_type IS NULL) GROUP BY s.seq_region_id, s.name, exception");

			int numTopLevel = 0;
                        int noDensity = 0;

			while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {

				long seqRegionID = rs.getLong("s.seq_region_id");
				String seqRegionName = rs.getString("s.name");
				boolean assemblyException = rs.getBoolean("exception");
				logger.fine("Counting density features on seq_region " + seqRegionName);

				String sql = "SELECT COUNT(*) FROM density_feature WHERE seq_region_id=" + seqRegionID;
				int dfRows = DBUtils.getRowCount(con, sql);
				if (dfRows == 0) {

                                        noDensity++;
				}

				// check each analysis type
				Iterator it = logicNameToAttribCode.keySet().iterator();
				while (it.hasNext()) {

					String logicName = (String) it.next();
					String attribCode = (String) logicNameToAttribCode.get(logicName);

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

				} // while it

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
	 * Check that each analysis_id is used at least one one density_type.
	 */
	private boolean checkAnalysisAndDensityTypes(DatabaseRegistryEntry dbre) {

		boolean result = true;
		
		Connection con = dbre.getConnection();
//		Species species = dbre.getSpecies();
		String[] logicNames;
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			logicNames = new String[] { "PCodDensity" };
		} else {

			logicNames = new String[] { "PercentGC", "PercentageRepeat", "CodingDensity", "PseudogeneDensity", "ShortNonCodingDensity", "LongNonCodingDensity" };
		}

		// check that each analysis_id is only used by one density_type
		for (int i = 0; i < logicNames.length; i++) {

			String logicName = logicNames[i];
			String sql = "SELECT dt.density_type_id FROM analysis a, density_type dt WHERE a.analysis_id=dt.analysis_id AND a.logic_name='" + logicName + "'";

			String[] rows = DBUtils.getColumnValues(con, sql);
			if (rows.length == 0) {

				if (dbre.getType() != DatabaseType.SANGER_VEGA || logicName.equalsIgnoreCase("knownGeneDensity")) {// for sangervega only
																																																						// report analysis
					ReportManager.problem(this, con, "RelCo: No entry in density_type for analysis " + logicName + " - run density pipeline");
				}
				result = false;

			}
			// note UNIQUE constraint prevents duplicate analysis_id/block_size values

		}

		return result;

	}

	// ----------------------------------------------------------------------
	/**
	 * Check for density_types that reference non-existent analysis_ids.
	 */
	private boolean checkDensityTypes(Connection con) {

		boolean result = true;

		String sql = "SELECT dt.density_type_id FROM density_type dt LEFT JOIN analysis a ON dt.analysis_id=a.analysis_id WHERE a.analysis_id IS NULL";

		String[] rows = DBUtils.getColumnValues(con, sql);
		if (rows.length == 0) {

			ReportManager.correct(this, con, "All density_types reference existing analysis_ids");

		} else {

			for (int j = 0; j < rows.length; j++) {
				ReportManager.problem(this, con, "density_type with ID " + rows[j] + " references non-existent analysis");
			}

			result = false;

		}

		return result;

	}

	// ----------------------------------------------------------------------
	/**
	 * Check for density_features that link to non-existent seq_regions.
	 */
	private boolean checkFeatureSeqRegions(Connection con) {

		boolean result = true;

		logger.finest("Checking density_feature-seq_region links");
		int orphans = countOrphans(con, "density_feature", "seq_region_id", "seq_region", "seq_region_id", true);
		if (orphans > 0) {
			ReportManager.problem(this, con, orphans + " density_features reference non-existent seq_regions");
			result = false;
		}
		return result;

	}

	// ----------------------------------------------------------------------

} // DensityFeatures
