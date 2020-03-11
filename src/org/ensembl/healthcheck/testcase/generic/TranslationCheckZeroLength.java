/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
 * Check for 0 length translations
 */

public class TranslationCheckZeroLength extends SingleDatabaseTestCase {

	/**
	 * Create a new TranslationCheckZeroLength testcase
	 */
	public TranslationCheckZeroLength() {

		setDescription("Check for 0 length translations");
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
		//Get the seq_region_start and seq_region_end from exon tables and based on the strand calculate the cds_end and cds_start and then calculate the difference of them (cds_length)
		
		String sql = "SELECT t.*,exon_start.exon_id,exon_start.seq_region_strand ,exon_start.seq_region_start as exon_start_seq_start,exon_start.seq_region_end as exon_start_seq_end,"
				+ "exon_end.exon_id,exon_end.seq_region_strand,exon_end.seq_region_start as exon_end_seq_start,exon_end.seq_region_end as exon_end_seq_end,"
				+ "IF(exon_end.seq_region_strand>0,exon_end.seq_region_start+t.seq_end-1,exon_start.seq_region_end-t.seq_start+1) as cds_end, "
				+ "IF(exon_start.seq_region_strand>0,exon_start.seq_region_start+t.seq_start-1,exon_end.seq_region_end-t.seq_end+1) as cds_start, "
				+ "(IF(exon_end.seq_region_strand>0,exon_end.seq_region_start+t.seq_end-1,exon_start.seq_region_end-t.seq_start+1)  - IF(exon_start.seq_region_strand>0,exon_start.seq_region_start+t.seq_start-1,cast(exon_end.seq_region_end as signed)-t.seq_end+1)+1) as cds_length "
				+ "FROM translation t "
				+ "INNER JOIN exon exon_start on t.start_exon_id = exon_start.exon_id "
				+ "INNER JOIN exon exon_end on t.end_exon_id=exon_end.exon_id "
				+ "HAVING cds_length < 3";

		int rows = DBUtils.getRowCount(con, sql);
		if (rows != 0) {
			result = false;
			
			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while (rs != null && rs.next()) {
					String translation_stableID = rs.getString(7);
					ReportManager.problem(this, con, "Translation with stableID " +  translation_stableID  + " have 0 length translations.");
					
				} // while rs

				stmt.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			

		} else {

			ReportManager.correct(this, con, "No translations have 0 length");

		}

		return result;

	} // run

} // TranslationCheckZeroLength
