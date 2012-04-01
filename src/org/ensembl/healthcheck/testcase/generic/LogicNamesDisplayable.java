/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that the logic names in the analysis table are displayable. Currently reads the list of displayable logic names from a text
 * file. Current set of logic names is stored in logicnames.txt (or file specified by logicnames.file property)
 */
public class LogicNamesDisplayable extends SingleDatabaseTestCase {

	// a list of the tables to check the analysis_id in
	private String[] tablesWithAnalysisID = getCoreTablesWithAnalysisID();

	private String logicNamesFile = "logicnames.txt";

	private static final boolean CASE_SENSITIVE = false;

	/**
	 * Creates a new instance of LogicNamesDisplayable.
	 */
	public LogicNamesDisplayable() {

		// addToGroup("post_genebuild");
		// addToGroup("release");

		setTeamResponsible(Team.RELEASE_COORDINATOR);
		setSecondTeamResponsible(Team.GENEBUILD);
		setDescription("Checks that all logic names in analysis are displayable");
		setHintLongRunning(true);

	}

	/**
	 * This test Does not apply to sanger_vega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
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

		try {
			Connection con = dbre.getConnection();
			result &= checkLogicNames(con);
			result &= checkProteinFeatureAnalysis(con);
			result &= checkMissingDBEntries(con);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	} // run

	/**
	 * Looks at analysis IDs in feature tables and checks the logic names they are associated with will be displayed by the web code.
	 * The list of valid logic names is currently at http://www.ensembl.org/Docs/wiki/html/EnsemblDocs/LogicNames.html Note that this
	 * test case actually uses the names from the file logicnames.txt which currently has to be manually created from the above URL.
	 * 
	 * @return
	 */
	private boolean checkLogicNames(Connection con) throws SQLException {

		boolean result = true;

		String message = CASE_SENSITIVE ? "Logic name comparison is case sensitive" : "Logic name comparison is NOT case sensitive";
		logger.info(message);

		// read the file containing the allowed logic names; use a custom file for
		// logic names if it exists
		String customFile = System.getProperty("logicnames.file");
		if (customFile != null) {
			logger.finest("Using custom logicnames file: " + customFile);
			logicNamesFile = customFile;
		}

		String[] allowedLogicNames = Utils.readTextFile(logicNamesFile);
		logger.fine("Read " + allowedLogicNames.length + " logic names from " + logicNamesFile);

		// we don't want to complain about analyses that are defined in supporting
		// features as
		// these will be drawn automatically by the web code, so find these and add
		// them to the
		// list of allowed logic names
		allowedLogicNames = addSupportingFeatureLogicNames(con, allowedLogicNames);

		// cache logic_names by analysis_id
		Map logicNamesByAnalID = new HashMap();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT analysis_id, logic_name" + " FROM analysis");
		while (rs.next()) {
			logicNamesByAnalID.put(rs.getString("analysis_id"), rs.getString("logic_name"));
		}

		for (int t = 0; t < tablesWithAnalysisID.length; t++) {
			String featureTableName = tablesWithAnalysisID[t];
			// skip large tables as this test takes an inordinately long time
			// also identity_xref which isn't really a feature table
			if (featureTableName.equals("protein_align_feature") || featureTableName.equals("dna_align_feature") || featureTableName.equals("repeat_feature") || featureTableName.equals("identity_xref")) {
				continue;
			}

			// skip object_xref as it's allowed to have null analysis_ids
			if (featureTableName.equals("object_xref")) {
				continue;
			}

			logger.finest("Analysing features in " + featureTableName);

			// get analysis IDs
			String[] analysisIDs = DBUtils.getColumnValues(con, "SELECT DISTINCT analysis_id FROM " + featureTableName);

			// check each analysis ID
			for (int i = 0; i < analysisIDs.length; i++) {
				/*
				 * check that there is an entry in the analysis table with this ID (i.e. foreign key integrity check)
				 */
				if (logicNamesByAnalID.get(analysisIDs[i]) == null) {
					ReportManager.problem(this, con, "Feature table " + featureTableName + " refers to non-existent analysis with ID " + analysisIDs[i]);
					result = false;
				} else {
					// check that logic name corresponding to this analysis id
					// is valid
					String logicName = (String) logicNamesByAnalID.get(analysisIDs[i]);
					if (!Utils.stringInArray(logicName, allowedLogicNames, CASE_SENSITIVE)) {
						ReportManager.problem(this, con, "Feature table " + featureTableName + " has features with logic name " + logicName + " which will not be drawn");
						result = false;
					}
				}
			}
		}
		rs.close();
		stmt.close();
		return result;
	}

	// -------------------------------------------------------------------------

	/*
	 * Does a set of analysis checks for the protein feature table This table has some bizarre requirements for the associated
	 * analysis which will hopefully change at some point. In the meantime this healthcheck will verify that those requirements are
	 * met.
	 */
	private boolean checkProteinFeatureAnalysis(Connection con) throws SQLException {

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT a.analysis_id, a.gff_feature, a.gff_source FROM analysis a, protein_feature pf WHERE a.analysis_id = pf.analysis_id " + "GROUP BY a.analysis_id");

		boolean noProblems = true;

		while (rs != null && rs.next()) {
			int analysisId = rs.getInt(1);
			String gffFeat = rs.getString(2);
			if (gffFeat == null) {
				gffFeat = "";
			}
			gffFeat = gffFeat.toUpperCase();
			String gffSource = rs.getString(3);
			if (gffSource == null) {
				gffSource = "";
			}

			if (gffSource.equals("PRINTS") || gffSource.equals("PFAM") || gffSource.equals("PROSITE") || gffSource.equals("PROFILE") || gffSource.equals("TIGRFAM") || gffSource.equals("SMART")
					|| gffSource.equals("PROSITE_PATTERN") || gffSource.equals("PIRSF")) {

				/* gff_feature must be domain */
				if (!gffFeat.equals("DOMAIN")) {
					ReportManager.problem(this, con, "protein_feature" + " analysis with analysis_id = " + analysisId + " and gffSource = " + gffSource + " must have gffSource eq 'DOMAIN'");
					noProblems = false;
				}
			} else if (gffSource.equals("BLASTP") || gffSource.equals("SEG") || gffSource.equals("TMHMM") || gffSource.equals("NCOILS") || gffSource.equals("SIGNALP")) {

				/* gff_feature must not be domain */
				if (gffFeat.equals("DOMAIN")) {
					ReportManager.problem(this, con, "protein_feature" + " analysis with analysis_id = " + analysisId + " and gffSource = " + gffSource + " must have gffSource ne 'DOMAIN'");
					noProblems = false;
				}
			}
		} // while rs

		if (noProblems) {
			ReportManager.correct(this, con, "protein_feature analysis table entries look OK");
		}

		rs.close();
		stmt.close();

		return noProblems;
	}

	// -------------------------------------------------------------------------
	/**
	 * Add any logic names defined in supporting features to the list of allowed logic names.
	 * 
	 * @param initialNames
	 *          The initial list of logic names.
	 * @return initialNames with any logic names from the supporting feature tables added to it.
	 */
	private String[] addSupportingFeatureLogicNames(Connection con, String[] initialNames) {

		List result = new ArrayList(Arrays.asList(initialNames));

		String[] featureTypes = DBUtils.getColumnValues(con, "SELECT DISTINCT(feature_type) FROM supporting_feature");

		for (int i = 0; i < featureTypes.length; i++) {
			String featureType = featureTypes[i];
			String sql = "SELECT DISTINCT a.logic_name FROM supporting_feature sf, " + featureType + " f, analysis a " + "WHERE sf.feature_type='" + featureType + "' AND sf.feature_id=f." + featureType
					+ "_id " + "AND f.analysis_id=a.analysis_id";
			String[] logicNames = DBUtils.getColumnValues(con, sql);
			for (int j = 0; j < logicNames.length; j++) {
				result.add(logicNames[j]);
				logger.finest("Added logic name " + logicNames[j] + " from supporting features in " + featureType);
			}
		}

		return (String[]) result.toArray(new String[result.size()]);

	} // addSupportingFeatureLogicNames

	// -------------------------------------------------------------------------
	/**
	 * Check for analysis table rows where the db column is blank (but not null). This may cause problems as the db column is used for
	 * the 'domain type' labels in ProtView. This may change in future so this check may not be needed in future.
	 * 
	 * @param con
	 *          The database to check.
	 * @return true if there are no blank DB entries.
	 */
	private boolean checkMissingDBEntries(Connection con) {

		boolean result = true;

		String[] blankDBLogicNames = DBUtils.getColumnValues(con, "SELECT logic_name FROM analysis WHERE db=''");
		for (int j = 0; j < blankDBLogicNames.length; j++) {
			ReportManager.problem(this, con, "Analysis with logic name '" + blankDBLogicNames[j] + "' has a blank db field - features of this type will have no label in ProtView");
			result = false;
		}

		if (result == true) {
			ReportManager.correct(this, con, "analysis table has no rows with blank db entries");
		}

		return result;
	}

	// -------------------------------------------------------------------------

} // LogicNamesDisplayable
