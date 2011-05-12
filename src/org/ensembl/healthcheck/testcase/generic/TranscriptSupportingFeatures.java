/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that transcripts which need supporting features have them.
 */

public class TranscriptSupportingFeatures extends SingleDatabaseTestCase {

	List<String> allowedNoSupporting = new ArrayList<String>();

	/**
	 * Create a new TranscriptSupportingFeatures testcase.
	 */
	public TranscriptSupportingFeatures() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that transcripts which need supporting features have them.");
		setPriority(Priority.AMBER);
		setTeamResponsible(Team.GENEBUILD);

		allowedNoSupporting.add("BGI_Augustus_geneset");
		allowedNoSupporting.add("BGI_Genewise_geneset");
		allowedNoSupporting.add("BGI_Genscan_geneset");
		allowedNoSupporting.add("LRG_import");
		allowedNoSupporting.add("MT_genbank_import");
		allowedNoSupporting.add("Medaka_Genome_Project");
		allowedNoSupporting.add("adipose_rnaseq");
		allowedNoSupporting.add("adrenal_rnaseq");
		allowedNoSupporting.add("blood_rnaseq");
		allowedNoSupporting.add("brain_rnaseq");
		allowedNoSupporting.add("breast_rnaseq");
		allowedNoSupporting.add("ccds_import");
		allowedNoSupporting.add("colon_rnaseq");
		allowedNoSupporting.add("gorilla_RNASeq");
		allowedNoSupporting.add("havana");
		allowedNoSupporting.add("havana_ig_gene");
		allowedNoSupporting.add("heart_rnaseq");
		allowedNoSupporting.add("kidney_rnaseq");
		allowedNoSupporting.add("lung_rnaseq");
		allowedNoSupporting.add("lymph_rnaseq");
		allowedNoSupporting.add("liver_rnaseq");
		allowedNoSupporting.add("ncRNA");
		allowedNoSupporting.add("ovary_rnaseq");
		allowedNoSupporting.add("oxford_FGU");
		allowedNoSupporting.add("prostate_rnaseq");
		allowedNoSupporting.add("refseq_human_import");
		allowedNoSupporting.add("refseq_mouse_import");
		allowedNoSupporting.add("singapore_est");
		allowedNoSupporting.add("singapore_gene");
		allowedNoSupporting.add("skeletal_rnaseq");
		allowedNoSupporting.add("testes_rnaseq");
		allowedNoSupporting.add("thyroid_rnaseq");
		allowedNoSupporting.add("zfish_RNASeq");
		allowedNoSupporting.add("ncRNA_pseudogene");

	}

	public void types() {

		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);

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

		// list of transcript analysis logic_names which are allowed to not have supporting features
		String allowed = "'" + StringUtils.join(allowedNoSupporting, "','") + "'";

		String sql = String
				.format(
						"SELECT COUNT(*) FROM transcript t LEFT JOIN transcript_supporting_feature tsf ON t.transcript_id = tsf.transcript_id JOIN analysis a ON a.analysis_id=t.analysis_id WHERE a.analysis_id=t.analysis_id and tsf.transcript_id IS NULL AND a.logic_name NOT IN (%s) AND t.biotype NOT IN ('rRNA')",
						allowed);

		int rows = getRowCount(con, sql);

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " transcripts which should have transcript_supporting_features do not have them\nUseful SQL: " + sql);
			result = false;

		} else {

			ReportManager.correct(this, con, "All transcripts that require supporting features have them");
		}

		return result;

	} // run

} // TranscriptSupportingFeatures
