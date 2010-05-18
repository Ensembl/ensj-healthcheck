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
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all of certain types of objects have analysis_descriptions. Also check that displayable field is set.
 */

public class AnalysisDescription extends SingleDatabaseTestCase {

	String[] types = { "gene", "transcript", "prediction_transcript" }; // add more here - must have analysis_id

	/**
   * Create a new AnalysisDescription testcase.
   */
	public AnalysisDescription() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that all of certain types of objects have analysis_descriptions; also check that displayable field is set.");
		setTeamResponsible("GeneBuilders");
		
	}

	/**
   * Run the test.
   * 
   * @param dbre The database to use.
   * @return true if the test passed.
   * 
   */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		result &= checkDescriptions(dbre);
		result &= checkDisplayable(dbre);
		
		return result;

	} // run

	// ------------------------------------------------------------------------------
	
	private boolean checkDescriptions(DatabaseRegistryEntry dbre) {
		
		boolean result = true;
		
		Connection con = dbre.getConnection();

		// cache logic_names by analysis_id
		Map logicNamesByAnalID =  getLogicNamesFromAnalysisTable(con);
		
		String[] tableTypes = tableNames();
		
		for (int i = 0; i < tableTypes.length; i++) {
			logger.finest("type is " + tableTypes[i]);
			// get analyses that are used
			// special case for transcripts - need to link to gene table and get analysis from there
			String sql =  "SELECT DISTINCT(analysis_id) FROM " + tableTypes[i];
						
			if (tableTypes[i].equals("transcript")) {
				sql = "SELECT DISTINCT(g.analysis_id) FROM gene g, transcript t WHERE t.gene_id=g.gene_id";
			}
			
			System.out.println(sql);
			
			String[] analyses = getColumnValues(con, sql);

		
			// check each one has an analysis_description
			for (int j = 0; j < analyses.length; j++) {
				int count = getRowCount(con, "SELECT COUNT(*) FROM analysis_description WHERE analysis_id=" + analyses[j]);
				if (count == 0) {
					ReportManager.problem(this, con, "Analysis " + logicNamesByAnalID.get(analyses[j]) + " is used in " + tableTypes[i]
							+ " but has no entry in analysis_description");
					result = false;
				} else {
					ReportManager.correct(this, con, "Analysis " + logicNamesByAnalID.get(analyses[j]) + " is used in " + tableTypes[i]
							+ " and has an entry in analysis_description");
				}

			}
		}

		return result;
		
	}
	
	// ------------------------------------------------------------------------------
	
	private boolean checkDisplayable(DatabaseRegistryEntry dbre) {
		
		return checkNoNulls(dbre.getConnection(), "analysis_description", "displayable");
		
	}
	
	// ------------------------------------------------------------------------------
	
	
	protected String[] tableNames() {

		return types;

	}
	
	
} // AnalysisDescription
