/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		String sql = " SELECT tr.transcript_id, e.exon_id, tr.seq_region_start AS transcript_start, tr.seq_region_end AS transcript_end, "
		        + "MIN(e.seq_region_start) as min_exon_start, MAX(e.seq_region_end) AS max_exon_end "
		        + "FROM exon e, transcript tr, exon_transcript et "
		        + "WHERE e.exon_id=et.exon_id AND et.transcript_id=tr.transcript_id "
		        + "AND tr.transcript_id not in (select transcript_id from transcript_attrib inner join attrib_type using (attrib_type_id) where code='trans_spliced')"
		        + "GROUP BY et.transcript_id HAVING min_exon_start != transcript_start OR max_exon_end != transcript_end ";

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
