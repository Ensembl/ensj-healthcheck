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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for any retrotransposed transcripts that have translations (shouldn't be any).
 */

public class Retrotransposed extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public Retrotransposed() {

		addToGroup("post_genebuild");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check for any retrotransposed transcripts that have translations (shouldn't be any).");
		setTeamResponsible(Team.GENEBUILD);
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
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM transcript t, translation tln WHERE t.transcript_id=tln.transcript_id AND t.biotype = 'retrotransposed'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " transcripts with biotype 'retrotransposed' have translations.");
			result = false;

		} else {

			ReportManager.correct(this, con, "No retrotransposed transcripts have translations.");

		}

		return result;

	} // run

} // Retrotransposed
