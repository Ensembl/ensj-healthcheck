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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for genes, transcripts and exons where the is_current column is
 * anything other than 1. Any value other than 1 will cause problems for Ensembl
 * databases.
 */
public class IsCurrent extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of IsCurrent.
	 */
	public IsCurrent() {

		addToGroup("post_genebuild");
		addToGroup("release");

		setDescription("Check for genes, transcripts and exons where the is_current column is anything other than 1.");

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

		String[] types = { "gene", "transcript", "exon" };

		for (int i = 0; i < types.length; i++) {

			String table = types[i];

			int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE is_current IS NULL OR is_current != 1");

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " " + table + "s have is_current set to null or some value other than 1");
				result = false;

			} else {

				ReportManager.correct(this, con, "All " + table + "s have is_current=1");

			}

		}

		return result;

	} // run

} // IsCurrent
