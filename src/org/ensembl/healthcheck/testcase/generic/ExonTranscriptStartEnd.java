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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the start and end of genes and transcripts make sense.
 */
public class ExonTranscriptStartEnd extends SingleDatabaseTestCase {

	/**
	 * Create a new ExonTranscriptStartEnd test case.
	 */
	public ExonTranscriptStartEnd() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Checks that exon and transcript start/end agree");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// Check that the minimum exon seq_region_start in a transcript is the same as the
		// transcript's start
		// and that the maximum exon seq_region_start in a transcript it the same as the
		// transcript's end
		// The SQL below will return cases where this is not true
		String sql = " SELECT tr.transcript_id, e.exon_id, tr.seq_region_start AS transcript_start, tr.seq_region_end AS transcript_end, MIN(e.seq_region_start) as min_exon_start, MAX(e.seq_region_end) AS max_exon_end FROM exon e, transcript tr, exon_transcript et WHERE e.exon_id=et.exon_id AND et.transcript_id=tr.transcript_id GROUP BY et.transcript_id HAVING min_exon_start != transcript_start OR max_exon_end != transcript_end ";

		Connection con = dbre.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {

			stmt = dbre.getConnection().createStatement();
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				ReportManager.problem(this, con, "Min/max exon start/ends do not agree with transcript start/end in transcript " + rs.getLong(1));
				result = false;
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DBUtils.closeQuietly(rs);
			DBUtils.closeQuietly(stmt);
		}

		if (result) {
			ReportManager.correct(this, con, "All exon/transcript start/ends agree");
		}

		return result;

	}

}
