/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Compare the analysis_descriptions in the current database with those from the
 * equivalent database on the secondary server. Note only certain columns are
 * checked.
 * 
 * Note this is not comparing counts so doesn't extend
 * ComparePreviousVersionBase.
 */

public class ComparePreviousVersionAnalysisDescriptions extends SingleDatabaseTestCase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionAnalysisDescriptions() {

		addToGroup("release");
		setDescription("Compare the analysis_descriptions in the current database with those from the equivalent database on the secondary server. Note only certain columns are checked.");

	}

	// ----------------------------------------------------------------------

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection currentCon = dbre.getConnection();

		DatabaseRegistryEntry previous = getEquivalentFromSecondaryServer(dbre);
		Connection previousCon = previous.getConnection();

		// Get data from current database, compare each one with equivalent on
		// previous
		String currentSQL = "SELECT a.logic_name, ad.display_label, ad.displayable, ad.web_data FROM analysis a, analysis_description ad WHERE a.analysis_id=ad.analysis_id";
		String previousSQL = "SELECT ad.display_label, ad.displayable, ad.web_data FROM analysis a, analysis_description ad WHERE a.analysis_id=ad.analysis_id AND a.logic_name=?";

		try {

			PreparedStatement currentStmt = currentCon.prepareStatement(currentSQL);
			PreparedStatement previousStmt = previousCon.prepareStatement(previousSQL);

			ResultSet currentRS = currentStmt.executeQuery();

			while (currentRS.next()) {

				String logicName = currentRS.getString(1);
				String currentDisplayLabel = currentRS.getString(2);
				int currentDisplayable = currentRS.getInt(3);
				String currentWebData = currentRS.getString(4);
				
				previousStmt.setString(1, logicName);
				ResultSet previousRS = previousStmt.executeQuery();
				if (previousRS == null) {
					continue;
				}
				if (!previousRS.next()) {
					continue;
				}
				
				String previousDisplayLabel = previousRS.getString(1);
				int previousDisplayable = previousRS.getInt(2);
				String previousWebData = previousRS.getString(3);
				
				if (!currentDisplayLabel.equals(previousDisplayLabel)) {
					ReportManager.problem(this, currentCon, "Display label for logic name " + logicName + " differs; current: '" + currentDisplayLabel + "' previous: '" + previousDisplayLabel + "'");
					result = false;
				} else {
					ReportManager.correct(this, currentCon, "Display labels identical between releases for " + logicName);
				}
				
				if (currentDisplayable != previousDisplayable) {
					ReportManager.problem(this, currentCon, "Displayable flag for logic name " + logicName + " differs; current: '" + currentDisplayable + "' previous: '" + previousDisplayable + "'");
					result = false;
				} else {
					ReportManager.correct(this, currentCon, "Displayable flags identical between releases for " + logicName);
				}
				
				if (currentWebData != null && previousWebData !=null && !currentWebData.equals(previousWebData)) {
					ReportManager.problem(this, currentCon, "Web data for logic name " + logicName + " differs; current: '" + currentWebData + "' previous: '" + previousWebData + "'");
					result = false;
				} else {
					ReportManager.correct(this, currentCon, "Web data identical between releases for " + logicName);
				}
				
				previousRS.close();

			}

			currentRS.close();

			currentStmt.close();
			previousStmt.close();
			

		} catch (SQLException e) {

			System.err.println("Error executing SQL");
			e.printStackTrace();

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // ComparePreviousVersionAnalysisDescriptions

