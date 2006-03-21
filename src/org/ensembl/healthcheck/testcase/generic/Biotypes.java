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
 * Check for null biotypes, and also for any 'ensembl' biotypes - should be
 * 'protein_coding'
 */
public class Biotypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Biotypes.
	 */
	public Biotypes() {

		addToGroup("post_genebuild");
		addToGroup("release");

		setDescription("Check for null biotypes, and also for any 'ensembl' biotypes - should be 'protein_coding'");

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
		result &= checkNull(con);
		result &= checkEnsembl(con);

		return result;

	} // run

	// -------------------------------------------------------------------------

	private boolean checkNull(Connection con) {

		boolean result = true;

		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE biotype IS NULL");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " genes have null biotypes");
			result = false;

		} else {

			ReportManager.correct(this, con, "No null biotypes in gene table");

		}

		return result;

	}

	// -------------------------------------------------------------------------

	private boolean checkEnsembl(Connection con) {

		boolean result = true;

		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE biotype='ensembl'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " genes have 'ensembl' biotypes - should probably be 'protein_coding'");
			result = false;

		} else {

			ReportManager.correct(this, con, "No 'ensembl' biotypes in gene table");

		}

		return result;

	}

	// -------------------------------------------------------------------------

} // Biotypes
