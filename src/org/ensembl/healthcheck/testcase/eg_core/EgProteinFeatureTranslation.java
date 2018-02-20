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


package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;

/**
 * An EnsEMBL Healthcheck test case which checks that the protein_feature table
 * agrees with the translation table. Modified for use with EnsemblGenomes to
 * support seq_edits
 */

public class EgProteinFeatureTranslation extends AbstractEgCoreTestCase {

	private static int THRESHOLD = 5; // don't report a problem if there are
										// less results than this
	private static int DISPLAY_LIMIT = 20;

	/**
	 * Create an ProteinFeatureTranslationTestCase that applies to a specific
	 * set of databases.
	 */
	public EgProteinFeatureTranslation() {
		super();
		setFailureText("Large numbers of features longer than the translation indicate something is wrong. A few is probably OK");
		setHintLongRunning(true);
	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
	}

	/**
	 * Builds a cache of the translation lengths, then compares them with the
	 * values in the protein_features table.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return Result.
	 */

	public boolean runTest(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// get list of transcripts
		String sql = "SELECT t.transcript_id, e.exon_id, tl.start_exon_id, "
				+ "       tl.translation_id, tl.end_exon_id, tl.seq_start, "
				+ "       tl.seq_end, e.seq_region_start, e.seq_region_end "
				+ "FROM   transcript t, exon_transcript et, exon e, translation tl "
				+ "WHERE  t.transcript_id = et.transcript_id "
				+ "AND    et.exon_id = e.exon_id "
				+ "AND    t.transcript_id = tl.transcript_id "
				+ "ORDER  BY t.transcript_id, et.rank";

		String sqlSeqEdit = "SELECT ta.translation_id,ta.value FROM translation_attrib ta where ta.attrib_type_id=144";

		try {

			Connection con = dbre.getConnection();

			// check that the protein feature table actually has some rows - if
			// not there's
			// no point working out the translation lengths
			if (!tableHasRows(con, "protein_feature")) {
				ReportManager.problem(this, con,
						"protein_feature table is empty");
				return false; // shoud we return true or false in this case?
			}

			// NOTE: By default the MM MySQL JDBC driver reads and stores *all*
			// rows in the
			// ResultSet.
			// Since this TestCase is likely to produce lots of output, we must
			// use the
			// "streaming"
			// mode where only one row of the ResultSet is stored at a time.
			// To do this, the following two lines are both necessary.
			// See the README file for the mm MySQL driver.

			Statement stmt = con.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);

			Map<Integer, Integer> translationLengths = CollectionUtils
					.createHashMap();

			// now calculate and store the translation lengths
			ResultSet rs = stmt.executeQuery(sql);
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);

			boolean inCodingRegion = false;

			while (rs.next()) {

				int currentTranslationID = rs.getInt("translation_id");
				Integer id = new Integer(currentTranslationID);
				// initialise if necessary
				if (translationLengths.get(id) == null) {
					translationLengths.put(id, new Integer(0));
				}

				if (!inCodingRegion) {
					if (rs.getInt("start_exon_id") == rs.getInt("exon_id")) {
						// single-exon-translations
						if (rs.getInt("start_exon_id") == rs
								.getInt("end_exon_id")) {
							int length = (rs.getInt("seq_end") - rs
									.getInt("seq_start")) + 1;
							translationLengths.put(id, new Integer(length));
							continue;
						}
						inCodingRegion = true;
						// subtract seq_start
						int currentLength = ((Integer) translationLengths
								.get(id)).intValue();
						currentLength -= (rs.getInt("seq_start") - 1);
						translationLengths.put(id, new Integer(currentLength));
					}
				} // if !inCoding

				if (inCodingRegion) {
					if (rs.getInt("exon_id") == rs.getInt("end_exon_id")) {
						// add seq_end
						int currentLength = ((Integer) translationLengths
								.get(id)).intValue();
						currentLength += rs.getInt("seq_end");
						translationLengths.put(id, new Integer(currentLength));
						inCodingRegion = false;
					} else {
						int currentLength = ((Integer) translationLengths
								.get(id)).intValue();
						currentLength += (rs.getInt("seq_region_end") - rs
								.getInt("seq_region_start")) + 1;
						translationLengths.put(id, new Integer(currentLength));
						// inCodingRegion = false;

					}
				} // if inCoding

			} // while rs

			rs.close();
			stmt.close();

			// modify according to seqedits
			stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);
			rs = stmt.executeQuery(sqlSeqEdit);
			while (rs.next()) {
				Integer translationId = rs.getInt(1);
				String edit = rs.getString(2);
				String[] vals = edit.split(" +");
				if (!vals[0].equals(vals[1])) {
					Integer len = (Integer) translationLengths
							.get(translationId);
					Integer insLen = new Integer(len + (3 * vals[2].length()));
					translationLengths.put(translationId, insLen);
				}
			}
			rs.close();
			stmt.close();

			stmt = null;
			// Re-open the statement to make sure it's GC'd
			stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);

			logger.fine("Built translation length cache, about to look at protein features");
			// dumpTranslationLengths(con, translationLengths, 100);

			// find protein features where seq_end is > than the length of the
			// translation
			List<String> thisDBFeatures = CollectionUtils.createArrayList();
			rs = stmt
					.executeQuery("SELECT pf.protein_feature_id, pf.translation_id, pf.seq_end, a.logic_name, pf.hit_name "
							+ "FROM protein_feature pf join analysis a using (analysis_id)");

			while (rs.next()) {

				Integer translationID = new Integer(rs.getInt("translation_id"));
				Integer proteinFeatureID = new Integer(
						rs.getInt("protein_feature_id"));

				if (translationLengths.get(translationID) != null) {
					// some codons can only be 2 bp ?!?
					int maxTranslationLength = (((Integer) translationLengths
							.get(translationID)).intValue() + 3) / 3;
					int fl = rs.getInt("seq_end");
					if (fl > maxTranslationLength) {
						result = false;
						String msg = "Protein feature " + proteinFeatureID
								+ "(" + rs.getString(4) + "/" + rs.getString(5)
								+ ") ends at " + fl + " which is beyond the "
								+ maxTranslationLength
								+ " length of the translation " + translationID;
						thisDBFeatures.add(msg);
					}
				}

			}

			if (thisDBFeatures.size() > THRESHOLD) {
				ReportManager
						.problem(
								this,
								con,
								"protein_feature table has "
										+ thisDBFeatures.size()
										+ " features that are longer than the translation");
				int n = 0;
				for (String msg : thisDBFeatures) {
					if (n < DISPLAY_LIMIT) {
						ReportManager.problem(this, con, msg);
					} else if (n == DISPLAY_LIMIT
							&& DISPLAY_LIMIT < thisDBFeatures.size()) {
						ReportManager.problem(this, con, "... "
								+ (thisDBFeatures.size() - DISPLAY_LIMIT)
								+ " more problem translations remain");
					}
					n++;
				}
			} else if (thisDBFeatures.size() == 0) {
				ReportManager
						.correct(this, con,
								"protein_feature_table has no features that are longer than the translation");
				
			} else {
				ReportManager
						.correct(
								this,
								con,
								"protein_feature_table has "
										+ thisDBFeatures.size()
										+ " features that are longer than the translation; this is less than the threshold of "
										+ THRESHOLD);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Checks that the protein_feature table agrees with the translation table.";
	}

} // ProteinFeatureTranslationTestCase
