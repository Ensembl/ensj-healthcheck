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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check if protein_coding genes have a canonical transcript that has a valid translation. See also canonical_transcript checks in
 * CoreForeignKeys.
 */
public class CanonicalTranscriptCoding extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of CanonicalTranscriptCoding.
	 */
	public CanonicalTranscriptCoding() {

		addToGroup("release");
		addToGroup("post_genebuild");
		setDescription("Check if protein_coding genes have a canonical transcript that has a valid translation. See also canonical_transcript checks in CoreForeignKeys.");
		setTeamResponsible("compara");

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

		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene g LEFT JOIN translation tr ON g.canonical_transcript_id=tr.transcript_id WHERE g.biotype='protein_coding' AND tr.transcript_id IS NULL");

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " protein_coding genes have canonical transcripts that do not have valid translations");

		} else {

			ReportManager.correct(this, con, "All protein_coding genes have canonical_transcripts that translate");
		}

		return result;

	} // run

} // CanonicalTranscriptCoding
