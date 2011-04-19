/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for hit_names that aren't formatted correctly.
 */
public class HitNameFormat extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance.
	 */
	public HitNameFormat() {

		addToGroup("release");
		addToGroup("post_genebuild");
		setDescription("Check that there are no incorrectly formatted hit_names");
		setPriority(Priority.AMBER);
		setFix("Manually fix affected values.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry entry to be checked.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] tables = { "dna_align_feature", "protein_align_feature", "protein_feature" };

		for (String table : tables) {

			int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE hit_name LIKE '%|%'");

			if (rows > 0) {
				ReportManager.problem(this, con, rows + " " + table + "s appear to have incorrectly formatted hit_names (containing a '|' symbol)");
				ReportManager.problem(this, con, "USEFUL SQL: SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(hit_name,'|',-2),'|',1) FROM " + table + " WHERE hit_name LIKE 'gi%|%'");
				ReportManager.problem(this, con, "UPDATE " + table + " SET hit_name = SUBSTRING_INDEX(SUBSTRING_INDEX(hit_name,'|',-2),'|',1) WHERE hit_name LIKE 'gi|%'");
				result = false;
			} else {
				ReportManager.correct(this, con, "All " + table + "s have correctly formatted hit_names");
			}

		}

		return result;

	}

} // HitNameFormat

