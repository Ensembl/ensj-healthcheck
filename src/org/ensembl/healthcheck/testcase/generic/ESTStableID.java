/*
 Copyright (C) 2003 EBI, GRL
 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that EST genes/transcripts/translations have EST in the stable ID.
 */

public class ESTStableID extends SingleDatabaseTestCase {

	/**
	 * Create a new ESTStableID testcase.
	 */
	public ESTStableID() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that EST genes/transcripts/translations have EST in the stable ID.");

	}

	/**
	 * Only applies to EST/OTHERFEATURES dbs.
	 */
	public void types() {

		List types = new ArrayList();

		types.add(DatabaseType.EST);
		types.add(DatabaseType.OTHERFEATURES);

		setAppliesToTypes(types);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
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

			int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + "_stable_id WHERE stable_id NOT REGEXP '" + regexp + "'");

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " " + table + " stable IDs do not contain EST" + letter);
				result = false;

			} else {

				ReportManager.correct(this, con, "All stable IDs contain EST" + letter );

			}

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // ESTStableID
