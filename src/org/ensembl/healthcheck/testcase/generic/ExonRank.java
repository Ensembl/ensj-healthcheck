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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Look for entries in the exon_transcript table that are duplicates apart from the rank.
 */

public class ExonRank extends SingleDatabaseTestCase {

	/**
	 * Create a new EonRank testcase.
	 */
	public ExonRank() {

		addToGroup("release");
		addToGroup("post_genebuild");
		setDescription("Look for entries in the exon_transcript table that are duplicates apart from the rank.");

	}

	/**
	 * Only run on core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		
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
		
		int rows = getRowCount(con, "SELECT COUNT(*) FROM exon_transcript et1, exon_transcript et2 WHERE et1.exon_id=et2.exon_id AND et1.transcript_id=et2.transcript_id AND et1.rank != et2.rank");

		if (rows > 0) {
			
			ReportManager.problem(this, con, rows + " rows in exon_transcript specify the same exon more than once in a transcript with a different rank");
			result = false;
		
		} else {
			
			ReportManager.correct(this, con, "All ranks OK");
			
		}
		
		return result;
		
	} // run

	// ----------------------------------------------------------------------

} // ExonRank

