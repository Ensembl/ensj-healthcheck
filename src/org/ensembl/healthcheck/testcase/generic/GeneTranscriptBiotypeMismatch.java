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
 * Check for biotype mismatch between the genes and transcripts
 */

public class GeneTranscriptBiotypeMismatch extends SingleDatabaseTestCase {

	/**
	 * Create a new GeneTranscriptBiotypeMismatch testcase.
	 */
	public GeneTranscriptBiotypeMismatch() {

		setDescription("Check for biotype mismatch between the genes and transcripts");
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

		//Joining the gene table with the transcript table
		//Get the biotypes and compare the list 
		//Transcript biotype is limited to '%coding', which captures 'protein_coding' and 'unknown_likely_coding'
		//Gene biotype is limited to the list of non-coding biotypes (\'lincRNA\',\'snoRNA\',\'antisense\',\'snRNA\',\'Mt_tRNA\',\'processed_transcript\',\'macro_lncRNA\')";
		
		String sql = "SELECT g.gene_id,g.stable_id,g.biotype,t.transcript_id, t.stable_id, t.biotype "
				+ "FROM gene g "
				+ "INNER JOIN transcript t "
				+ "WHERE g.gene_id=t.gene_id "
				+ "AND t.biotype like \"%coding\" "
				+ "AND g.biotype in (\'lincRNA\',\'snoRNA\',\'antisense\',\'snRNA\',\'Mt_tRNA\',\'processed_transcript\',\'macro_lncRNA\')";


		int rows = DBUtils.getRowCount(con, sql);
		if (rows != 0) {
			result = false;
			
			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while (rs != null && rs.next()) {
					String transcript_stableID = rs.getString(5);
					String gene_stableID = rs.getString(2);
					String transcript_biotype = rs.getString(6);
					String gene_biotype = rs.getString(3);
					ReportManager.problem(this, con, "Transcripts with stableID " +  transcript_stableID  + " have coding biotype '" + transcript_biotype + "' that doesn't match with gene with stableID "+  gene_stableID +" non-coding biotype '" + gene_biotype + "'");
					
				} // while rs

				stmt.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			

		} else {

			ReportManager.correct(this, con, "There is no biotype mismatch between the genes and transcripts for coding types");

		}

		return result;

	} // run

} // GeneTranscriptBiotypeMismatch
