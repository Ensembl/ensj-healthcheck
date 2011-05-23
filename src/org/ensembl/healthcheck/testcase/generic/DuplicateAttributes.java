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
 * Check for duplicated rows in various *_attrib tables.
 */
public class DuplicateAttributes extends SingleDatabaseTestCase {

	double THRESHOLD = 0.0; // fraction of non-unique rows must be greater than this for a warning to occur

	String[] attribs = { "gene", "transcript", "translation", "seq_region", "misc" };

	/**
	 * Creates a new instance of DuplicateAttributes
	 */
	public DuplicateAttributes() {

		addToGroup("release");
		addToGroup("post_genebuild");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check for duplicated rows in various *_attrib tables.");
		setPriority(Priority.AMBER);
		setEffect("Many duplicates can cause serious performance problems.");
		setFix("Remove duplicated rows if appropriate.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		for (String attrib : attribs) {

			String table = attrib + "_attrib";
			String column = attrib.equals("misc") ? "misc_feature_id" : attrib + "_id";

			logger.finest("Checking " + table);

			int totalRows = getRowCount(con, "SELECT COUNT(*) FROM " + table);

			if (totalRows == 0) { // avoid division by zero
				continue;
			}

			int uniqueRows = getRowCount(con, "SELECT COUNT(DISTINCT " + column + ", attrib_type_id, value) FROM " + table);

			int duplicates = totalRows - uniqueRows;

			if ((double) duplicates / (double) totalRows > THRESHOLD) {

				ReportManager.problem(this, con, table + " has " + totalRows + " rows in total but only " + uniqueRows + " are unique");
				result = false;

			} else {

				ReportManager.correct(this, con, "No duplicated rows in " + table);

			}
		}

		return result;

	} // run

} // DuplicateAttributes

