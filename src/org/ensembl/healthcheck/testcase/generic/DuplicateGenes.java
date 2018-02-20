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
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case which checks if any genes are obvious duplicates of each other (it might be all OK, but it's
 * worth a look!)
 */

public class DuplicateGenes extends SingleDatabaseTestCase {

	private static final int MAX_WARNINGS = 10;

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public DuplicateGenes() {

		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Check for (strongly likely to be) duplicate genes.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passes.
	 */

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String sql = "SELECT g.gene_id, g.seq_region_start AS start, g.seq_region_end AS end, g.seq_region_id AS chromosome_id, g.seq_region_strand AS strand, g.biotype, g.stable_id, g.analysis_id, g.display_xref_id, g.source, g.description, g.is_current, g.canonical_transcript_id "
				+ "             FROM gene g ORDER BY chromosome_id, strand, start, end";

		Connection con = dbre.getConnection();
		try {

			Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(sql);

			int geneStart, geneEnd, geneChromosome, geneId, geneStrand;
			int lastGeneId = 0;
			int lastGeneStart = -1;
			int lastGeneEnd = -1;
			int lastGeneChromosome = -1;
			int lastGeneStrand = -1;
			int duplicateGene = 0;

			int geneAnalysis = -1;
			int geneDisplayXref = -1;
			String geneSource = "";
			String geneDescription = "";
			int geneIsCurrent = -1;
			int geneCanonicalTranscript = -1;
			String geneCanonicalAnnotation = "";

			String geneBioType, geneStableID;
			String lastGeneBioType = "";
			String lastGeneStableID = "";

			int lastGeneAnalysis = -1;
			int lastGeneDisplayXref = -1;
			String lastGeneSource = "";
			String lastGeneDescription = "";
			int lastGeneIsCurrent = -1;
			int lastGeneCanonicalTranscript = -1;
			String lastGeneCanonicalAnnotation = "";

			boolean first = true;

			while (rs.next()) {

				// load the vars
				geneId = rs.getInt(1);
				geneStart = rs.getInt(2);
				geneEnd = rs.getInt(3);
				geneChromosome = rs.getInt(4);
				geneStrand = rs.getInt(5);
				geneBioType = rs.getString(6);
				geneStableID = rs.getString(7);

				geneAnalysis = rs.getInt(8);
				geneDisplayXref = rs.getInt(9);
				geneSource = rs.getString(10);
				geneDescription = rs.getString(11);
				geneIsCurrent = rs.getInt(12);
				geneCanonicalTranscript = rs.getInt(13);
				// canonical_annotation removed in 74
				// geneCanonicalAnnotation = rs.getString(15);

				if (!first) {
					if (lastGeneChromosome == geneChromosome
							&& lastGeneStart == geneStart
							&& lastGeneEnd == geneEnd
							&& lastGeneStrand == geneStrand
							&& geneBioType.equals(lastGeneBioType)) {

						duplicateGene++;
						if (duplicateGene < MAX_WARNINGS) {
							ReportManager.warning(this, con, "Gene " + geneStableID + " (" + geneBioType + " ID " + geneId + ") is duplicated - see gene " + lastGeneStableID + " (" + lastGeneBioType + " ID "
									+ lastGeneId + ")");
						}
					}
				} else {
					first = false;
				}

				lastGeneId = geneId;
				lastGeneStart = geneStart;
				lastGeneEnd = geneEnd;
				lastGeneChromosome = geneChromosome;
				lastGeneStrand = geneStrand;
				lastGeneBioType = geneBioType;
				lastGeneStableID = geneStableID;

				lastGeneAnalysis = geneAnalysis;
				lastGeneDisplayXref = geneDisplayXref;
				lastGeneSource = geneSource;
				lastGeneDescription = geneDescription;
				lastGeneIsCurrent = geneIsCurrent;
				lastGeneCanonicalTranscript = geneCanonicalTranscript;
				lastGeneCanonicalAnnotation = geneCanonicalAnnotation;

			} // while rs

			if (duplicateGene > 0) {
				ReportManager.problem(this, con, "Has " + duplicateGene + " duplicated genes.");
				result = false;
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		// EG return correct report line if all is OK
		if (result)
			ReportManager.correct(this, con, "No duplicate genes found");
		return result;

	}

} // DuplicateGenes
