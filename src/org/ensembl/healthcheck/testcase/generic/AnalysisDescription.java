/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check that all of certain types of objects have analysis_descriptions. Also check that displayable field is set.
 */

public class AnalysisDescription extends SingleDatabaseTestCase {

	String[] types = { "gene", "transcript", "prediction_transcript" }; // add more here - must have analysis_id

	/**
	 * Create a new AnalysisDescription testcase.
	 */
	public AnalysisDescription() {

		setDescription("Check that all of certain types of objects have analysis_descriptions; also check that displayable field is set.");
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

		result &= checkDescriptions(dbre);
		result &= checkDisplayable(dbre);

		return result;

	} // run

	// ------------------------------------------------------------------------------

	private boolean checkDescriptions(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		SqlTemplate t = getSqlTemplate(con);

		// cache logic_names by analysis_id
		Map<Integer, String> logicNamesByAnalID = getLogicNamesFromAnalysisTable(con);

		String[] tableTypes = tableNames();

		for (String tableType : tableTypes) {

			logger.finest("type is " + tableType);

			// get analyses that are used
			// special case for transcripts - need to link to gene table and get analysis from there
			String sql = String.format("SELECT DISTINCT(analysis_id) FROM %s", tableType);


			List<Integer> analyses = t.queryForDefaultObjectList(sql, Integer.class);

			// check each one has an analysis_description
			for (Integer analysisId : analyses) {
			  int count = t.queryForDefaultObject("select count(*) from analysis_description where analysis_id =?", Integer.class, analysisId);

				if (count == 0) {
					ReportManager.problem(this, con, String.format("Analysis %s is used in %s but has no entry in analysis_description", logicNamesByAnalID.get(analysisId), tableType));
					result = false;
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
