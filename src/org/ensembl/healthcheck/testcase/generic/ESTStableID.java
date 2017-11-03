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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that EST genes/transcripts/translations have EST in the stable ID.
 */

public class ESTStableID extends SingleDatabaseTestCase {

	/**
	 * Create a new ESTStableID testcase.
	 */
	public ESTStableID() {

		setDescription("Check that EST genes/transcripts/translations have EST in the stable ID.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Only applies to EST/OTHERFEATURES dbs.
	 */
	public void types() {

		List<DatabaseType> types = new ArrayList<DatabaseType>();

		types.add(DatabaseType.EST);
		types.add(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.RNASEQ);

		setAppliesToTypes(types);

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

		Connection con = dbre.getConnection();

		Map tableToLetter = new HashMap();

		tableToLetter.put("gene", "G");
		tableToLetter.put("transcript", "T");
		tableToLetter.put("translation", "P");

		Iterator it = tableToLetter.keySet().iterator();

		while (it.hasNext()) {

			String table = (String) it.next();
			String letter = (String) tableToLetter.get(table);

			String regexp = "EST" + letter + "[0-9]+";

			String sql = "SELECT COUNT(*) FROM " + table + " x, analysis a WHERE a.analysis_id=x.analysis_id "
					+ "AND a.logic_name LIKE '%est%' AND x.stable_id NOT REGEXP '" + regexp + "'";
			if (table.equals("translation")) {
				// need extra join to transcript table
				sql = "SELECT COUNT(*) FROM translation x, transcript t, analysis a WHERE a.analysis_id=t.analysis_id AND x.transcript_id=t.transcript_id AND a.logic_name LIKE '%est%' AND x.stable_id NOT REGEXP 'ESTP[0-9]+'";
			}

			int rows = DBUtils.getRowCount(con, sql);

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " " + table + " stable IDs do not contain EST" + letter);
				result = false;

			} else {

				ReportManager.correct(this, con, "All stable IDs contain EST" + letter);

			}

			sql = "SELECT COUNT(*) FROM " + table + " x, analysis a WHERE a.analysis_id=x.analysis_id "
					+ "AND a.logic_name NOT LIKE '%est%' AND x.stable_id REGEXP '" + regexp + "'";
			if (table.equals("translation")) {
				// need extra join to transcript table
				sql = "SELECT COUNT(*) FROM translation x, transcript t, analysis a WHERE a.analysis_id=t.analysis_id AND x.transcript_id=t.transcript_id AND a.logic_name NOT LIKE '%est%' AND x.stable_id REGEXP 'ESTP[0-9]+'";
			}
			rows = DBUtils.getRowCount(con, sql);

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " non ESTs " + table + " stable IDs contain EST" + letter);
				result = false;

			} else {

				ReportManager.correct(this, con, "All non ESTs stable IDs do not contain EST" + letter);

			}

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // ESTStableID
