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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check if protein_coding genes have a canonical transcript that has a valid translation. See also canonical_transcript checks in
 * CoreForeignKeys.
 */
public class CanonicalTranscriptCoding extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of CanonicalTranscriptCoding.
	 */
	public CanonicalTranscriptCoding() {

		setDescription("Check if protein_coding genes have a canonical transcript that has a valid translation. Also check than number of canonical transcripts is correct. See also canonical_transcript checks in CoreForeignKeys.");
                setTeamResponsible(Team.CORE);
                setSecondTeamResponsible(Team.GENEBUILD);
	}

        public void types() {

                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);

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


                // --------------------------------
                // Check all canonical transcripts in a gene correspond to a transcript and all canonical_transcript_ids correspond to a gene

                result &= checkForOrphans(con, "gene", "canonical_transcript_id", "transcript", "transcript_id", true);
                int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM gene g, transcript t where g.canonical_transcript_id=" + "t.transcript_id and g.gene_id <> t.gene_id");
                if (rows > 0) {
                        // problem, the canonical transcript does not belong to the gene
                        String useful_sql = "SELECT g.gene_id,g.canonical_transcript_id FROM gene g, transcript t where g.canonical_transcript_id=" + "t.transcript_id and g.gene_id <> t.gene_id";
                        ReportManager.problem(this, con, rows + " rows in gene have a canonical transcript it doesn't belong to the gene" + " Try '" + useful_sql + "' to find out the offending genes");
                        result = false;
                }

                if (dbre.getType() == DatabaseType.SANGER_VEGA || dbre.getType() == DatabaseType.CORE) {
                        result &= checkBiotypes(dbre);
                }

                return result;

        }

        private boolean checkBiotypes(DatabaseRegistryEntry dbre) {

                boolean result = true;

                Connection con = dbre.getConnection();

		// --------------------------------
		// A gene that has at least one transcript.biotype='protein_coding' should have gene.biotype='protein_coding'
		String sql = "SELECT COUNT(*) FROM gene g WHERE g.gene_id IN (SELECT tr.gene_id FROM transcript tr WHERE tr.biotype='protein_coding') AND g.biotype NOT IN ('protein_coding', 'polymorphic_pseudogene')";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += "AND g.biotype!='polymorphic' AND g.biotype!='polymorphic_pseudogene' and (g.source='havana' or g.source='WU')";
		}
		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			String report = " genes with at least one protein_coding transcript do not have biotype protein_coding";
			if (dbre.getType() == DatabaseType.SANGER_VEGA) {
				report += " or polymorphic_pseudogene";
			}
			ReportManager.problem(this, con, rows + report);

		} else {

			ReportManager.correct(this, con, "All genes with protein_coding transcripts have protein_coding biotype");

		}

		// --------------------------------
		// Protein_coding transcripts should all have translations
		sql = "SELECT count(*) FROM transcript tr join gene g on tr.gene_id=g.gene_id WHERE tr.biotype='protein_coding' AND tr.transcript_id NOT IN (SELECT transcript_id from translation)";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			sql += " and (g.source='havana' or g.source='WU')";
		}
		rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " protein_coding transcripts do not have translations\nUSEFUL SQL: SELECT transcript.transcript_id,transcript.analysis_id FROM transcript LEFT JOIN translation ON transcript.transcript_id = translation.transcript_id WHERE transcript.biotype = 'protein_coding' and translation.transcript_id IS NULL; ");

		} else {

			ReportManager.correct(this, con, "All protein_coding transcripts have translations");
		}

		// --------------------------------
		// All genes should have a canonical transcript
		sql = "SELECT COUNT(*) FROM gene g WHERE g.canonical_transcript_id is NULL";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (g.source='havana' or g.source='WU')";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes do not have a canonical transcript");

		} else {

			ReportManager.correct(this, con, "All genes have a canonical transcript");

		}

		// --------------------------------
		// All canonical transcripts with a translation should belong to a gene with a biotype of 'protein_coding',
		// 'IG_C_gene','IG_D_gene','IG_J_gene', 'IG_V_gene' or 'RNA-Seq_gene'

		sql = "SELECT COUNT(*) FROM gene g WHERE g.canonical_transcript_id IN (SELECT tr.transcript_id FROM transcript tr, translation tl WHERE tr.transcript_id=tl.transcript_id) AND g.biotype NOT IN ('protein_coding','IG_C_gene','IG_D_gene','IG_J_gene','IG_V_gene','IG_LV_gene','RNA-Seq_gene','polymorphic_pseudogene','TR_C_gene','TR_J_gene','TR_V_gene','TR_D_gene','LRG_gene','nontranslating_cds'";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += ", 'polymorphic','IG_gene','TR_gene') and (g.source='havana' or g.source='WU')";
		} else {
			sql += ")";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes with canonical transcripts have the wrong biotype");

		} else {

			ReportManager.correct(this, con, "All genes with canonical transcripts have the correct biotype");

		}

		// --------------------------------
		// None of the transcripts that have a translation and have a biotype different to
		// ('protein_coding','IG_C_gene','IG_D_gene','IG_J_gene','IG_V_gene')) should be canonical transcripts to any gene.
		sql = "SELECT COUNT(distinct g.gene_id) FROM gene g WHERE g.canonical_transcript_id IN (select tr.transcript_id FROM transcript tr, translation tl WHERE tr.transcript_id=tl.transcript_id AND tr.biotype NOT IN ('protein_coding','IG_C_gene','IG_D_gene','IG_J_gene','IG_V_gene', 'IG_LV_gene', 'TR_C_gene','TR_J_gene','TR_V_gene', 'TR_D_gene','LRG_gene', 'nonsense_mediated_decay','polymorphic_pseudogene','non_stop_decay','nontranslating_cds'";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += ", 'polymorphic','IG_gene','TR_gene')) and (g.source='havana' or g.source='WU')";
		} else {
			sql += "))";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes have canonical transcripts with mismatched biotypes");

		} else {

			ReportManager.correct(this, con, "All genes have canonical transcripts with matching biotypes");

		}

		// --------------------------------
		// A gene that has gene.biotype='protein_coding' and has at least one transcript.biotype='protein_coding' should have a
		// canonical transcript.biotype='protein_coding'.
		sql = "SELECT count(distinct g.gene_id) FROM gene g JOIN transcript t USING (gene_id) WHERE g.gene_id IN (SELECT g.gene_id FROM gene g JOIN transcript t ON (g.canonical_transcript_id = t.transcript_id) WHERE g.biotype = 'protein_coding' AND t.biotype not in ('protein_coding', 'nonsense_mediated_decay')) AND t.biotype = 'protein_coding'";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (g.source='havana' or g.source='WU')";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes with at least one protein_coding transcript do not have a protein_coding canonical transcript");

		} else {

			ReportManager.correct(this, con, "All genes with at least one protein_coding transcript have a protein_coding canonical transcript");

		}

		// --------------------------------
		// If a gene is gene.biotype='protein_coding' but has no transcripts that are transcript.biotype='protein_coding', at least one
		// of the transcripts has to have a translation.
		sql = "SELECT count(distinct g.gene_id) FROM gene g JOIN transcript t USING (gene_id) JOIN translation p ON (t.canonical_translation_id = p.translation_id) WHERE g.biotype = 'protein_coding' AND g.gene_id NOT IN (SELECT gene_id FROM transcript WHERE biotype in ('protein_coding', 'nonsense_mediated_decay', 'non_stop_decay'))";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (g.source='havana' or g.source='WU')";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " protein_coding gene(s) may potentially be missing translations.");

		} else {

			ReportManager.correct(this, con, "All protein_coding genes with no protein_coding transcripts have at least one transcripts which translates");

		}

		// --------------------------------
		// check if protein_coding genes have a canonical transcript that has a valid translation
		sql = "SELECT COUNT(*) FROM gene g LEFT JOIN translation tr ON g.canonical_transcript_id=tr.transcript_id WHERE g.biotype='protein_coding' AND tr.transcript_id IS NULL";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (g.source='havana' or g.source='WU')";
		}
		rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " protein_coding genes have canonical transcripts that do not have valid translations");

		} else {

			ReportManager.correct(this, con, "All protein_coding genes have canonical_transcripts that translate");
		}

		// --------------------------------
		// check that the number of canonical translations is correct
		int numCanonical = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM transcript t1, translation p, transcript t2 WHERE t1.canonical_translation_id = p.translation_id AND p.transcript_id = t2.transcript_id");

		int numTotal = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM translation p, transcript t WHERE t.transcript_id = p.transcript_id");

		if (numCanonical != numTotal) {

			result = false;
			ReportManager.problem(this, con, "Number of canonical translations (" + numCanonical + ") is different from the total number of translations (" + numTotal + ")");

		} else {

			ReportManager.correct(this, con, "Number of canonical translations is correct.");
		}

		return result;

	}

} // CanonicalTranscriptCoding
