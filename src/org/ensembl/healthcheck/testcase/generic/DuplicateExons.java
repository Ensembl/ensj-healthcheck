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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case which checks all exon of a gene are on the same strand and in the correct order in their
 * transcript..
 */

public class DuplicateExons extends SingleDatabaseTestCase {

	private static final int MAX_WARNINGS = 10;

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public DuplicateExons() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Check for duplicate exons.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passes.
	 */

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String sql = "SELECT e.exon_id, e.phase, e.seq_region_start AS start, e.seq_region_end AS end, e.seq_region_id AS chromosome_id, e.end_phase, e.seq_region_strand AS strand "
				+ ", t.gene_id AS gene_id " + "  FROM exon e, exon_transcript et, transcript t " + " WHERE e.exon_id=et.exon_id and et.transcript_id = t.transcript_id "
				+ " ORDER BY chromosome_id, gene_id, strand, start, end, phase, end_phase";

		Connection con = dbre.getConnection();
		try {

			Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(sql);

			int exonStart, exonEnd, exonPhase, exonChromosome, exonID, exonEndPhase, exonStrand, exonGeneId;
			int lastExonStart = -1;
			int lastExonEnd = -1;
			int lastExonPhase = -1;
			int lastExonChromosome = -1;
			int lastExonEndPhase = -1;
			int lastExonStrand = -1;
			int lastExonID = -1;
			int lastExonGeneId = -1;
			int duplicateExon = 0;

			boolean first = true;

			while (rs.next()) {

				// load the vars
				exonID = rs.getInt("exon_id");
				exonPhase = rs.getInt("phase");
				exonStart = rs.getInt("start");
				exonEnd = rs.getInt("end");
				exonChromosome = rs.getInt("chromosome_id");
				exonEndPhase = rs.getInt("end_phase");
				exonStrand = rs.getInt("strand");
				exonGeneId = rs.getInt("gene_id");

				if (!first) {
					if (lastExonChromosome == exonChromosome && lastExonStart == exonStart && lastExonEnd == exonEnd && lastExonPhase == exonPhase && lastExonStrand == exonStrand
							&& lastExonEndPhase == exonEndPhase && lastExonGeneId != exonGeneId) {
						duplicateExon++;
						if (duplicateExon <= MAX_WARNINGS) {
							ReportManager.warning(this, con, "Exon " + exonID + " in gene " + exonGeneId + " is a duplicate of exon " + lastExonID);
						}
					}
				} else {
					first = false;
				}

				lastExonStart = exonStart;
				lastExonEnd = exonEnd;
				lastExonChromosome = exonChromosome;
				lastExonPhase = exonPhase;
				lastExonEndPhase = exonEndPhase;
				lastExonStrand = exonStrand;
				lastExonID = exonID;
				lastExonGeneId = exonGeneId;

			} // while rs

			if (duplicateExon > 0) {
				ReportManager.problem(this, con, "Has at least " + duplicateExon + " duplicated exons.");
				result = false;
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		// EG write correct report line if all OK
		if (result) {
			ReportManager.correct(this, con, "No duplicate exons found");
		}

		return result;

	}

} // DuplicateExons
