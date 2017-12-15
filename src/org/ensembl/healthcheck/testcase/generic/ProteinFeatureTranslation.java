/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case which checks that the protein_feature table agrees with the translation table.
 */

public class ProteinFeatureTranslation extends SingleDatabaseTestCase implements Repair {

	// hash of lists of protein features to delete
	// key - database name
	private Map featuresToDelete;

	private static int THRESHOLD = 1000; // don't report a problem if there are less results than this

	private static int OUTPUT_LIMIT = 20; // only complain about this many missing translations or long translations

	/**
	 * Create an ProteinFeatureTranslationTestCase that applies to a specific set of databases.
	 */
	public ProteinFeatureTranslation() {

		featuresToDelete = new HashMap();
		setFailureText("Large numbers of features longer than the translation indicate something is wrong. A few is probably OK");
		setHintLongRunning(true);
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This test only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Builds a cache of the translation lengths, then compares them with the values in the protein_features table.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		int problems = 0;

		// get list of transcripts
		String sql = "SELECT t.transcript_id, e.exon_id, tl.start_exon_id, " + "       tl.translation_id, tl.end_exon_id, tl.seq_start, " + "       tl.seq_end, e.seq_region_start, e.seq_region_end "
				+ "FROM   transcript t, exon_transcript et, exon e, translation tl " + "WHERE  t.transcript_id = et.transcript_id " + "AND    et.exon_id = e.exon_id "
				+ "AND    t.transcript_id = tl.transcript_id " + "ORDER  BY t.transcript_id, et.rank";

		try {

			Connection con = dbre.getConnection();

			// check that the protein feature table actually has some rows - if
			// not there's
			// no point working out the translation lengths
			if (!tableHasRows(con, "protein_feature")) {
				ReportManager.problem(this, con, "protein_feature table is empty");
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

			Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);

			Map translationLengths = new HashMap();

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
						if (rs.getInt("start_exon_id") == rs.getInt("end_exon_id")) {
							int length = (rs.getInt("seq_end") - rs.getInt("seq_start")) + 1;
							translationLengths.put(id, new Integer(length));
							continue;
						}
						inCodingRegion = true;
						// subtract seq_start
						int currentLength = ((Integer) translationLengths.get(id)).intValue();
						currentLength -= (rs.getInt("seq_start") - 1);
						translationLengths.put(id, new Integer(currentLength));
					}
				} // if !inCoding

				if (inCodingRegion) {
					if (rs.getInt("exon_id") == rs.getInt("end_exon_id")) {
						// add seq_end
						int currentLength = ((Integer) translationLengths.get(id)).intValue();
						currentLength += rs.getInt("seq_end");
						translationLengths.put(id, new Integer(currentLength));
						inCodingRegion = false;
					} else {
						int currentLength = ((Integer) translationLengths.get(id)).intValue();
						currentLength += (rs.getInt("seq_region_end") - rs.getInt("seq_region_start")) + 1;
						translationLengths.put(id, new Integer(currentLength));
						// inCodingRegion = false;

					}
				} // if inCoding

			} // while rs

			rs.close();
			stmt.close();
			stmt = null;

			// Re-open the statement to make sure it's GC'd
			stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			// stmt.setFetchSize(1000);
			stmt.setFetchSize(Integer.MIN_VALUE);

			logger.fine("Built translation length cache, about to look at protein features");
			// dumpTranslationLengths(con, translationLengths, 100);

			// find protein features where seq_end is > than the length of the
			// translation
			List thisDBFeatures = new ArrayList();
			rs = stmt.executeQuery("SELECT protein_feature_id, translation_id, seq_end FROM protein_feature");

			while (rs.next()) {

				Integer translationID = new Integer(rs.getInt("translation_id"));
				Integer proteinFeatureID = new Integer(rs.getInt("protein_feature_id"));

				if (translationLengths.get(translationID) != null) {
					// some codons can only be 2 bp
					int minTranslationLength = (((Integer) translationLengths.get(translationID)).intValue() + 2) / 3;
					// int minTranslationLength = ((Integer)
					// translationLengths.get(translationID)).intValue();
					if (rs.getInt("seq_end") > minTranslationLength) {
						thisDBFeatures.add(proteinFeatureID);
						// System.out.println("proteinFeatureID: " + proteinFeatureID);
					}
				} else {
					if (problems++ < OUTPUT_LIMIT) {
						ReportManager.problem(this, con, "Protein feature " + proteinFeatureID + " refers to non-existent translation " + translationID);
						result = false;
					}
				}
			}

			featuresToDelete.put(DBUtils.getShortDatabaseName(con), thisDBFeatures);
			if (thisDBFeatures.size() > THRESHOLD) {
				ReportManager.problem(this, con, "protein_feature table has " + thisDBFeatures.size() + " features that are longer than the translation");
				result = false;
			} else if (thisDBFeatures.size() == 0) {
				ReportManager.correct(this, con, "protein_feature table has no features that are longer than the translation");
			} else {
				ReportManager.correct(this, con, "protein_feature table has " + thisDBFeatures.size() + " features that are longer than the translation; this is less than the threshold of " + THRESHOLD);
			}

			rs.close();
			stmt.close();

			if (problems >= OUTPUT_LIMIT) {
				ReportManager.problem(this, con, "Note that only " + OUTPUT_LIMIT + " missing translation IDs were notified, there may be more");
				result = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	// ------------------------------------------
	// Implementation of Repair interface.

	/**
	 * Delete any protein features that run past the end of the translation. <strong>CAUTION! </strong>Actually deletes the features
	 * from the protein_feature table.
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void repair(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		String sql = setupRepairSQL(con);
		if (sql.length() == 0) {
			System.out.println("No invalid protein features were found in " + DBUtils.getShortDatabaseName(con));
		} else {
			try {
				Statement stmt = con.createStatement();
				System.out.println(DBUtils.getShortDatabaseName(con));
				System.out.println(sql);
				// stmt.execute(sql);
				stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

	}

	/**
	 * Show which protein features would be deleted by the repair method.
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void show(DatabaseRegistryEntry dbre) {

		System.out.println("Candidate for repair:");

		Connection con = dbre.getConnection();
		String sql = setupRepairSQL(con);
		if (sql.length() == 0) {
			System.out.println("No invalid protein features were found in " + DBUtils.getShortDatabaseName(con));
		} else {
			System.out.println(DBUtils.getShortDatabaseName(con) + ": " + sql);
		}

	}

	/**
	 * Set up the SQL to delete the offending protein features.
	 * 
	 * @param con
	 *          The database connection to use.
	 * @return The SQL to delete the incorrect protein features, or "" if there are no problems.
	 */
	private String setupRepairSQL(Connection con) {

		StringBuffer sql = new StringBuffer("DELETE FROM protein_feature WHERE protein_feature_id IN (");

		List thisDBFeatures = (List) featuresToDelete.get(DBUtils.getShortDatabaseName(con));

		if (thisDBFeatures == null || thisDBFeatures.size() == 0) {
			return "";
		}

		Iterator featureIterator = thisDBFeatures.iterator();
		while (featureIterator.hasNext()) {
			sql.append(((Integer) featureIterator.next()).intValue());
			if (featureIterator.hasNext()) {
				sql.append(",");
			}
		}
		sql.append(")");

		return sql.toString();

	}

	// -------------------------------------------------------------------------

	// private void dumpTranslationLengths(Connection con, Map lengths, int maxID) {
	//
	// System.out.println("Translation lengths for " + DBUtils.getShortDatabaseName(con));
	//
	// Set keySet = lengths.keySet();
	// List keyList = new ArrayList(keySet);
	// Collections.sort(keyList, new IntegerComparator());
	//
	// Iterator it = keyList.iterator();
	// while (it.hasNext()) {
	//
	// Integer iid = (Integer) it.next();
	// int id = iid.intValue();
	// if (id > maxID) {
	// break;
	// }
	// Integer iLength = (Integer) lengths.get(iid);
	// int length = iLength.intValue();
	// System.out.println("ID: " + id + "\tLength: " + length);
	// }
	//
	// }

	// -------------------------------------------------------------------------

} // ProteinFeatureTranslationTestCase
