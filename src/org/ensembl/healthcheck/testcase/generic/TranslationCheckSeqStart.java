/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for for translation with seq_start less than the start exon's length
 */

public class TranslationCheckSeqStart extends SingleDatabaseTestCase {

	/**
	 * Create a new TranslationCheckSeqStart testcase.
	 */
	public TranslationCheckSeqStart() {

		setDescription("Check for translation with seq_start less than the start exon's length");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

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

		//Join the translation table with the exon table twice (to select start exon and end exon details), 
		//Get the seq_region_start from exon tables and check if translation seq_start is greater than start exon's length
		
		String sql = "SELECT t.*,exon_start.exon_id,exon_start.seq_region_strand , exon_start.seq_region_end, exon_start.seq_region_start,"
				+ "exon_start.seq_region_end - exon_start.seq_region_start + 1 as exon_start_length,exon_end.exon_id,exon_end.seq_region_strand, exon_end.seq_region_end, exon_end.seq_region_start, "
				+ "exon_end.seq_region_end - exon_end.seq_region_start+1 as exon_end_length "
				+ "FROM translation t "
				+ "INNER JOIN exon exon_start on t.start_exon_id = exon_start.exon_id "
				+ "INNER JOIN exon exon_end on t.end_exon_id=exon_end.exon_id "
				+ "HAVING  t.seq_start > exon_start_length+1;";

		int rows = DBUtils.getRowCount(con, sql);
		if (rows != 0) {
			result = false;
			
			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while (rs != null && rs.next()) {
					String translation_stableID = rs.getString(7);
					int seq_start = rs.getInt(3);
					int exon_start_length =rs.getInt(15);
					ReportManager.problem(this, con, "Translation with stableID " +  translation_stableID  + " have seq_start ("+ seq_start + ")  greater than the length of start exon (" + exon_start_length + ")");
					
				} // while rs

				stmt.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			

		} else {

			ReportManager.correct(this, con, "All translations have seq_start greater than length of start exon");

		}

		return result;

	} // run

} // TranslationCheckSeqStart
