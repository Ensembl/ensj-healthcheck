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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case which checks if any genes are obvious
 * duplicates of each other (it might be all OK, but it's worth a look!)
 */

public class DuplicateGenes extends SingleDatabaseTestCase {

	private static final int MAX_WARNINGS = 10;

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public DuplicateGenes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setTeamResponsible("GeneBuilders");

	}

	/**
	 * This test only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Check for (strongly likely to be) duplicate genes.
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passes.
	 */

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String sql = "SELECT g.gene_id, g.seq_region_start AS start, g.seq_region_end AS end, g.seq_region_id AS chromosome_id, g.seq_region_strand AS strand, g.biotype, gsi.stable_id "
				+ "             FROM (gene g, gene_stable_id gsi) WHERE g.gene_id=gsi.gene_id ORDER BY chromosome_id, strand, start, end";

		Connection con = dbre.getConnection();
		try {

			Statement stmt = con.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(sql);

			int geneStart, geneEnd, geneChromosome, geneId, geneStrand;
			int lastGeneId = 0;
			int lastGeneStart = -1;
			int lastGeneEnd = -1;
			int lastGeneChromosome = -1;
			int lastGeneStrand = -1;
			int duplicateGene = 0;
			String geneBioType, geneStableID;
			String lastGeneBioType = "";
			String lastGeneStableID = "";

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

				if (!first) {
					if (lastGeneChromosome == geneChromosome
							&& lastGeneStart == geneStart
							&& lastGeneEnd == geneEnd
							&& lastGeneStrand == geneStrand
							&& geneBioType.equals(lastGeneBioType)) {
						duplicateGene++;
						if (duplicateGene < MAX_WARNINGS) {
							ReportManager.warning(this, con, "Gene "
									+ geneStableID + " (" + geneBioType
									+ " ID " + geneId
									+ ") is duplicated - see gene "
									+ lastGeneStableID + " (" + lastGeneBioType
									+ " ID " + lastGeneId + ")");
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

			} // while rs

			if (duplicateGene > 0) {
				ReportManager.problem(this, con, "Has " + duplicateGene
						+ " duplicated genes.");
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
			ReportManager.correct(this, con, "No duplicate exons found");
		return result;

	}

} // DuplicateGenes
