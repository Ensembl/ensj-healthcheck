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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all top-level seq regions have some SNP/gene/knownGene density
 * features, and that the values agree between the density_feature and
 * seq_region attrib tables. Only checks top-level seq regions that do NOT have
 * an _ in their names. Also checks that there are some density features for
 * each analysis/density type. Also checks that there are no duplicates in the
 * seq_region_attrib table.
 */

public class DensityFeatures extends SingleDatabaseTestCase {

	// max number of top-level seq regions to check
	private static final int MAX_TOP_LEVEL = 100;

	// map between analysis.logic_name and seq_region attrib_type.code
	private Map logicNameToAttribCode = new HashMap();

	/**
	 * Create a new DensityFeatures testcase.
	 */
	public DensityFeatures() {

		// addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that all top-level seq regions have some SNP/gene/knownGene density features, and that the values agree between the density_feature and seq_region attrib tables.");
		setFailureText("May report count mismatches on HAP/PAR regions.\nAlso, if a species has no SNP data, the 'No entry in density_type for analysis snpDensity' warning can be ignored. ");

		logicNameToAttribCode.put("snpDensity", "SNPCount");
		logicNameToAttribCode.put("geneDensity", "GeneCount");
		logicNameToAttribCode.put("knownGeneDensity", "knownGeneCount");

	}

	// ----------------------------------------------------------------------

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= checkFeaturesAndCounts(con);

		result &= checkAnalysisAndDensityTypes(dbre);

		result &= checkDensityTypes(con);

		result &= checkFeatureSeqRegions(con);

		result &= checkDuplicates(con);

		return result;

	} // run

	// ----------------------------------------------------------------------

	private boolean checkFeaturesAndCounts(Connection con) {

		boolean result = true;

		// get top level co-ordinate system ID
		String sql = "SELECT coord_system_id FROM coord_system WHERE rank=1 LIMIT 1";

		String s = getRowColumnValue(con, sql);

		if (s.length() == 0) {
			System.err.println("Error: can't get top-level co-ordinate system for " + DBUtils.getShortDatabaseName(con));
			return false;
		}

		int topLevelCSID = Integer.parseInt(s);

		try {

			// check each top-level seq_region (up to a limit) to see how many density
			// features there are
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM seq_region WHERE coord_system_id=" + topLevelCSID
					+ " AND name NOT LIKE '%\\_%'");

			int numTopLevel = 0;

			while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {

				long seqRegionID = rs.getLong("seq_region_id");
				String seqRegionName = rs.getString("name");
				logger.fine("Counting density features on seq_region " + seqRegionName);

				sql = "SELECT COUNT(*) FROM density_feature WHERE seq_region_id=" + seqRegionID;
				int dfRows = getRowCount(con, sql);
				if (dfRows == 0) {

					ReportManager.problem(this, con, "Top-level seq region " + seqRegionName + " (ID " + seqRegionID
							+ ") has no density features");
					result = false;

				} else {

					ReportManager.correct(this, con, seqRegionName + " has " + dfRows + " density features");
				}

				// check each analysis type
				Iterator it = logicNameToAttribCode.keySet().iterator();
				while (it.hasNext()) {

					String logicName = (String) it.next();
					String attribCode = (String) logicNameToAttribCode.get(logicName);

					// check if this species has appropriate density features
					int analRows = getRowCount(con, "SELECT COUNT(*) FROM analysis WHERE logic_name='" + logicName + "'");
					if (analRows == 0) {
						logger.info(DBUtils.getShortDatabaseName(con) + " has no " + logicName
								+ " analysis type, skipping checks for these features");
					} else {

						// check that the sum of the density_feature.density_value matches
						// what
						// is in the seq_region_attrib table

						logger.fine("Comparing density_feature.density_value with seq_region_attrib for " + logicName + " features on "
								+ seqRegionName);

						sql = "SELECT SUM(df.density_value) FROM density_type dt, density_feature df, analysis a WHERE dt.density_type_id=df.density_type_id AND dt.analysis_id=a.analysis_id AND a.logic_name='"
								+ logicName + "' AND seq_region_id=" + seqRegionID;

						String sumDF = getRowColumnValue(con, sql);
						// System.out.println(sql + " " + sumDF);
						if (sumDF != null && sumDF.length() > 0) {

							long sumFromDensityFeature = Long.parseLong(sumDF);

							sql = "SELECT value FROM seq_region_attrib sra, attrib_type at WHERE sra.attrib_type_id=at.attrib_type_id AND at.code='"
									+ attribCode + "' AND seq_region_id=" + seqRegionID;

							String sumSRA = getRowColumnValue(con, sql);
							// System.out.println(sql + " " + sumSRA);
							if (sumSRA != null && sumSRA.length() > 0) {

								long valueFromSeqRegionAttrib = Long.parseLong(sumSRA);

								if (sumFromDensityFeature != valueFromSeqRegionAttrib) {

									ReportManager.problem(this, con, "Sum of values for " + logicName + " from density_feature ("
											+ sumFromDensityFeature + ") doesn't agree with value from seq_region_attrib (" + valueFromSeqRegionAttrib
											+ ") for " + seqRegionName);
									result = false;

								} else {

									ReportManager.correct(this, con, "density_feature and seq_region_attrib values agree for " + logicName
											+ " on seq region " + seqRegionName);

								}

							} // if sumSRA

						} // if sumDF

					} // if rows

				} // while it

			} // while rs.next

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
		Species species = dbre.getSpecies();

		String[] logicNames = { "PercentGC", "PercentageRepeat", "knownGeneDensity", "geneDensity", "snpDensity" };

		// check that each analysis_id is only used by one density_type
		for (int i = 0; i < logicNames.length; i++) {

			String logicName = logicNames[i];
			String sql = "SELECT dt.density_type_id FROM analysis a, density_type dt WHERE a.analysis_id=dt.analysis_id AND a.logic_name='"
					+ logicName + "'";

			String[] rows = getColumnValues(con, sql);
			if (rows.length >= 1) {

				ReportManager.correct(this, con, "One density_type for analysis " + logicName);

			} else if (rows.length == 0) {

				// only warn about missing snpDensity for species that have SNPs
				if (logicNames[i].equals("snpDensity")
						&& (species != Species.ANOPHELES_GAMBIAE && species != Species.CANIS_FAMILIARIS && species != Species.DANIO_RERIO
								&& species != Species.GALLUS_GALLUS && species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS && species != Species.RATTUS_NORVEGICUS)) {
					continue;
				}
				ReportManager.problem(this, con, "No entry in density_type for analysis " + logicName);
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

		String[] rows = getColumnValues(con, sql);
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
		} else {
			ReportManager.correct(this, con, "All density_type->seq_region relationships are OK");
		}
		return result;

	}

	// ----------------------------------------------------------------------
	/**
	 * Check for duplicate seq_region_attribs for certain attrib types.
	 */
	private boolean checkDuplicates(Connection con) {

		boolean result = true;

		String[] types = { "GeneNo_knwCod", "GeneNo_novCod", "GeneNo_rRNA", "GeneNo_pseudo", "GeneNo_snRNA", "GeneNo_snoRNA",
				"GeneNo_miRNA", "GeneNo_mscRNA", "GeneNo_scRNA", "SNPCount", "codon_table", "KnownPCCount", "NovelPCCount", "NovelPTCount",
				"PutPTCount", "PredPCCount", "IgSegCount", "IgPsSegCount", "TotPsCount", "ProcPsCount", "UnprocPsCount", "KnwnPCProgCount",
				"NovPCProgCount", "AnnotSeqLength", "TotCloneNum", "NumAnnotClone", "KnownPTCount" };

		for (int i = 0; i < types.length; i++) {

			logger.finest("Checking for duplicate " + types[i] + " seq_region attribs");
			int rows = getRowCount(con,
					"SELECT COUNT(*) AS n FROM seq_region_attrib s, attrib_type a WHERE a.attrib_type_id=s.attrib_type_id and a.code ='"
							+ types[i] + "' GROUP BY s.seq_region_id, s.attrib_type_id, s.value HAVING n > 1;");

			if (rows > 0) {
				ReportManager.problem(this, con, rows + " duplicates attributes of type " + types[i] + " in seq_region_attrib");
				result = false;
			} else {
				ReportManager.correct(this, con, "No duplicates of type " + types[i] + " in seq_region_attrib");
			}

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // DensityFeatures
