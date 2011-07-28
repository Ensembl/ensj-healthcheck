/*
 Copyright (C) 2004 EBI, GRL
 
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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for presence and format of PFAM hits, and format of others. Also checks for protein features with no hit_id.
 */

public class Accession extends SingleDatabaseTestCase {

	private HashMap formats = new HashMap();

	/**
	 * Constructor.
	 */
	public Accession() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("post-compara-handover");
		
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
			int hits = getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='pfam' AND a.analysis_id=pf.analysis_id");
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
			int badFormat = getRowCount(con, "SELECT COUNT(*) FROM protein_feature pf, analysis a WHERE a.logic_name='" + key + "' AND a.analysis_id=pf.analysis_id AND pf.hit_name NOT LIKE '"
					+ formats.get(key) + "'");
			if (badFormat > 0) {
				result = false;
				ReportManager.problem(this, con, badFormat + " " + key + " hit IDs are not in the correct format");
			} else {
				ReportManager.correct(this, con, "All " + key + " hits are in the correct format");
			}

		}

		// check for protein features with no hit_id
		int nullHitIDs = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_name IS NULL OR hit_name=''");
		if (nullHitIDs > 0) {
			result = false;
			ReportManager.problem(this, con, nullHitIDs + " protein features have null or blank hit_names");
		} else {
			ReportManager.correct(this, con, "No protein features have null or blank hit_names");
		}

		return result;

	}

}
