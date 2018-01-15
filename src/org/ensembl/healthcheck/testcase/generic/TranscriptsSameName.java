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
import java.text.DecimalFormat;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for genes with more than one transcript where all the transcripts have the same display_xref_id.
 */

public class TranscriptsSameName extends SingleDatabaseTestCase {

	private static int THRESHOLD = 60; // give error if more than this percentage

	// of transcripts have the same name

	/**
	 * Create a new TranscriptsSameName testcase.
	 */
	public TranscriptsSameName() {

		setDescription(" Check for genes with more than one transcript where all the transcripts have the same display_xref_id.");
		setPriority(Priority.AMBER);
		setEffect("Web display and all other uses of xrefs are broken");
		setFix("Recalculate display xrefs");
		setTeamResponsible(Team.CORE);
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

		// first get total number of genes that have more than one transcript
		// note we have to force the use DBUtils.getRowCountFast here because of the nature
		// of the query
		int totalGenes = DBUtils.getRowCountFast(con, "SELECT COUNT(1) FROM (SELECT g.gene_id FROM gene g, transcript t WHERE t.gene_id=g.gene_id GROUP BY g.gene_id HAVING COUNT(*) > 1) AS c");

		try {

			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT g.gene_id, t.transcript_id, t.display_xref_id FROM gene g, transcript t WHERE t.gene_id=g.gene_id AND t.display_xref_id IS NOT NULL ORDER BY g.gene_id");

			long previousGeneID = -1;
			long previousDisplayXrefID = -1;

			long lastCountedGeneID = -1;

			int sameNameTranscriptCount = 0;

			while (rs != null && rs.next()) {

				long geneID = rs.getLong(1);

				long displayXrefID = rs.getLong(3);

				if (geneID == previousGeneID && displayXrefID == previousDisplayXrefID && lastCountedGeneID != geneID) {
					sameNameTranscriptCount++;
					lastCountedGeneID = geneID;
				}

				previousGeneID = geneID;
				previousDisplayXrefID = displayXrefID;

			} // while rs

			stmt.close();

			double percentage = 100 * ((double) sameNameTranscriptCount / (double) totalGenes);

			String percentageStr = new DecimalFormat("##.#").format(percentage);

			if (percentage > THRESHOLD) {
				ReportManager.problem(this, con, percentageStr + "% of genes with more than one transcript have identically-named transcripts");
				result = false;
			} else {
				ReportManager.correct(this, con, "Only " + percentageStr + "% genes with more than one transcript have identically-named transcripts");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	} // run

} // TranscriptsSameName
