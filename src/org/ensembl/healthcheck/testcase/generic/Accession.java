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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for presence and format of PFAM hits, and format of others. Also checks for protein features with no hit_id.
 */

public class Accession extends SingleDatabaseTestCase {

	private HashMap formats = new HashMap();

	/**
	 * Constructor.
	 */
	public Accession() {

		setDescription("Check for presence and format of PFAM etc hits");
		setTeamResponsible(Team.GENEBUILD);

		// add to this hash to check for other types and formats
		formats.put("pfam", "PF_____%");
		formats.put("prints", "PR_____");
		formats.put("prosite", "PS_____");
		formats.put("profile", "PS_____");
		formats.put("scanprosite", "PS_____");

	}

        /**
         * This test applies only to core dbs
         */
        public void types() {
                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
        }

	/**
	 * Check each type of hit.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return Result.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that there is at least one PFAM hit
		// others - prints, prosite etc - may not have any hits
		// only a problem for core databses
		if (dbre.getType() == DatabaseType.CORE) {
			int hits = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='pfam' AND a.analysis_id=pf.analysis_id");
			if (hits < 1) {
				result = false;
				ReportManager.problem(this, con, "No proteins with PFAM hits");
			} else {
				ReportManager.correct(this, con, hits + " proteins with PFAM hits");
			}
		}

		// check formats for others
		Set keys = formats.keySet();
		Iterator it2 = keys.iterator();

		while (it2.hasNext()) {

			String key = (String) it2.next();
			logger.fine("Checking for logic name " + key + " with hits of format " + formats.get(key));

			// check format of hits
			int badFormat = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='" + key + "' AND a.analysis_id=pf.analysis_id AND pf.hit_name NOT LIKE '"
					+ formats.get(key) + "'");
			if (badFormat > 0) {
				result = false;
				ReportManager.problem(this, con, badFormat + " " + key + " hit IDs are not in the correct format");
			}

		}

		// check for protein features with no hit_id
		int nullHitIDs = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_name IS NULL OR hit_name=''");
		if (nullHitIDs > 0) {
			result = false;
			ReportManager.problem(this, con, nullHitIDs + " protein features have null or blank hit_names");
		} else {
			ReportManager.correct(this, con, "No protein features have null or blank hit_names");
		}

		return result;

	}

}
