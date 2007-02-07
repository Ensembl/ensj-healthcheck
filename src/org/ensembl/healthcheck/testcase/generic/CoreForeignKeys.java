/*
 * Copyright (C) 2004 EBI, GRL
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * realtionships.
 */

public class CoreForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public CoreForeignKeys() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("id_mapping");
		setDescription("Check for broken foreign-key relationships.");

	}

	/**
	 * Look for broken foreign key realtionships.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true Ff all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// ----------------------------

		result &= checkForOrphans(con, "exon", "exon_id", "exon_transcript", "exon_id", false);

		result &= checkForOrphans(con, "transcript", "transcript_id", "exon_transcript", "transcript_id", false);

		result &= checkForOrphans(con, "gene", "gene_id", "transcript", "gene_id", false);

		result &= checkForOrphans(con, "object_xref", "xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "xref", "external_db_id", "external_db", "external_db_id", true);

		result &= checkForOrphans(con, "dna", "seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "seq_region", "coord_system_id", "coord_system", "coord_system_id", true);

		result &= checkForOrphans(con, "assembly", "cmp_seq_region_id", "seq_region", "seq_region_id", true);
                
		result &= checkForOrphans(con, "marker_feature", "marker_id", "marker", "marker_id", true);

		result &= checkForOrphans(con, "seq_region_attrib", "seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "seq_region_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);

		result &= checkForOrphans(con, "misc_feature_misc_set", "misc_feature_id", "misc_feature", "misc_feature_id", true);

		result &= checkForOrphans(con, "misc_feature_misc_set", "misc_set_id", "misc_set", "misc_set_id", true);

		result &= checkForOrphans(con, "misc_feature", "misc_feature_id", "misc_attrib", "misc_feature_id", true);

		result &= checkForOrphans(con, "misc_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);

		result &= checkForOrphans(con, "assembly_exception", "seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "assembly_exception", "exc_seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "protein_feature", "translation_id", "translation", "translation_id", true);

		result &= checkForOrphans(con, "marker_synonym", "marker_id", "marker", "marker_id", true);

		result &= checkForOrphans(con, "translation_attrib", "translation_id", "translation", "translation_id", true);

		result &= checkForOrphans(con, "transcript_attrib", "transcript_id", "transcript", "transcript_id", true);

		result &= checkForOrphans(con, "regulatory_factor_coding", "regulatory_factor_id", "regulatory_factor", "regulatory_factor_id",
				true);

		result &= checkForOrphans(con, "regulatory_feature", "regulatory_factor_id", "regulatory_factor", "regulatory_factor_id", true);

		/*
		// now redundant (done for all tables with analysis_id)
		result &= checkForOrphans(con, "regulatory_feature", "analysis_id", "analysis", "analysis_id", true);
		result &= checkForOrphans(con, "transcript", "analysis_id", "analysis", "analysis_id", true);
		*/

		result &= checkForOrphans(con, "external_synonym", "xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "identity_xref", "object_xref_id", "object_xref", "object_xref_id", true);

		result &= checkForOrphans(con, "supporting_feature", "exon_id", "exon", "exon_id", true);

		result &= checkForOrphans(con, "translation", "transcript_id", "transcript", "transcript_id", true);

		result &= checkForOrphans(con, "go_xref", "object_xref_id", "object_xref", "object_xref_id", true);

		// stable ID archive
		result &= checkForOrphansWithConstraint(con, "gene_archive", "peptide_archive_id", "peptide_archive", "peptide_archive_id", "peptide_archive_id != 0");
		result &= checkForOrphans(con, "peptide_archive", "peptide_archive_id", "gene_archive", "peptide_archive_id", true);
		result &= checkForOrphans(con, "stable_id_event", "mapping_session_id", "mapping_session", "mapping_session_id", false);
		result &= checkForOrphans(con, "gene_archive", "mapping_session_id", "mapping_session", "mapping_session_id", true);

		// ----------------------------
		// Check regulatory features point to existing objects
		String[] rfTypes = { "Gene", "Transcript", "Translation" };
		for (int i = 0; i < rfTypes.length; i++) {
			result &= checkRegulatoryFeatureKeys(con, rfTypes[i]);
		}

		// ----------------------------
		// Check stable IDs all correspond to an existing object
		String[] stableIDtypes = { "gene", "transcript", "translation", "exon" };
		for (int i = 0; i < stableIDtypes.length; i++) {
			result &= checkStableIDKeys(con, stableIDtypes[i]);
		}

		// ----------------------------
		// Ensure that feature tables reference existing seq_regions
		String[] featTabs = getCoreFeatureTables();

		for (int i = 0; i < featTabs.length; i++) {
			String featTab = featTabs[i];
			// skip large tables as this test takes an inordinately long time
			if (featTab.equals("protein_align_feature") || featTab.equals("dna_align_feature") || featTab.equals("repeat_feature")) {
				continue;
			}
			result &= checkForOrphans(con, featTab, "seq_region_id", "seq_region", "seq_region_id", true);
		}


		// new tests (added by Patrick 07.02.2007)
		
		result &= checkForOrphans(con, "analysis_description", "analysis_id", "analysis", "analysis_id", true);

		result &= checkForOrphans(con, "gene_attrib", "gene_id", "gene", "gene_id", true);
		result &= checkForOrphans(con, "gene_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
		result &= checkForOrphans(con, "transcript_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
		result &= checkForOrphans(con, "translation_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);

		result &= checkForOrphans(con, "translation", "end_exon_id", "exon", "exon_id", true);
		result &= checkForOrphans(con, "translation", "start_exon_id", "exon", "exon_id", true);

		result &= checkForOrphans(con, "regulatory_factor_coding", "gene_id", "gene", "gene_id", true);
		result &= checkForOrphans(con, "regulatory_factor_coding", "transcript_id", "transcript", "transcript_id", true);

		result &= checkForOrphans(con, "alt_allele", "gene_id", "gene", "gene_id", true);

		result &= checkForOrphans(con, "marker_map_location", "map_id", "map", "map_id", true);
		result &= checkForOrphans(con, "marker_map_location", "marker_id", "marker", "marker_id", true);
		result &= checkForOrphans(con, "marker_map_location", "marker_synonym_id", "marker_synonym", "marker_synonym_id", true);

		result &= checkForOrphans(con, "qtl_feature", "qtl_id", "qtl", "qtl_id", true);
		result &= checkForOrphans(con, "qtl_synonym", "qtl_id", "qtl", "qtl_id", true);

		result &= checkForOrphans(con, "assembly", "asm_seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "unmapped_object", "external_db_id", "external_db", "external_db_id", true);
		result &= checkForOrphans(con, "unmapped_object", "unmapped_reason_id", "unmapped_reason", "unmapped_reason_id", true);

		result &= checkForOrphans(con, "transcript_supporting_feature", "transcript_id", "transcript", "transcript_id", true);

		result &= checkForOrphansWithConstraint(con, "supporting_feature", "feature_id", "dna_align_feature", "dna_align_feature_id",
			"feature_type = 'dna_align_feature'");

		result &= checkForOrphansWithConstraint(con, "supporting_feature", "feature_id", "protein_align_feature", "protein_align_feature_id",
			"feature_type = 'protein_align_feature'");

		result &= checkForOrphansWithConstraint(con, "transcript_supporting_feature", "feature_id", "dna_align_feature", "dna_align_feature_id",
			"feature_type = 'dna_align_feature'");

		result &= checkForOrphansWithConstraint(con, "transcript_supporting_feature", "feature_id", "protein_align_feature", "protein_align_feature_id",
			"feature_type = 'protein_align_feature'");

		result &= checkForOrphans(con, "oligo_feature", "oligo_probe_id", "oligo_probe", "oligo_probe_id", true);

		result &= checkForOrphans(con, "oligo_probe", "oligo_array_id", "oligo_array", "oligo_array_id", true);

		result &= checkForOrphans(con, "density_feature", "density_type_id", "density_type", "density_type_id");

		result &= checkForOrphans(con, "prediction_exon", "prediction_transcript_id", "prediction_transcript", "prediction_transcript_id");

		/* don't test

		// too slow
		result &= checkForOrphans(con, "repeat_feature", "repeat_consensus_id", "repeat_consensus", "repeat_consensus_id");

		// relation not mandatory
		result &= checkForOrphans(con, "qtl", "flank_marker_id_1", "marker", "marker_id", true);
		result &= checkForOrphans(con, "qtl", "flank_marker_id_2", "marker", "marker_id", true);
		result &= checkForOrphans(con, "qtl", "peak_marker_id", "marker", "marker_id", true);

		*/

		// ----------------------------
		// Check tables which reference the analysis table
		String[] analysisTabs = getCoreTablesWithAnalysisID();

		for (int i = 0; i < analysisTabs.length; i++) {
			String analysisTab = analysisTabs[i];
			// skip large tables as this test takes an inordinately long time
			if (analysisTab.equals("protein_align_feature") || analysisTab.equals("dna_align_feature") || analysisTab.equals("repeat_feature")) {
				continue;
			}
			result &= checkForOrphans(con, analysisTab, "analysis_id", "analysis", "analysis_id", true);
		}


		// end new tests


		return result;

	}

	// -------------------------------------------------------------------------
	private boolean checkStableIDKeys(Connection con, String type) {

		if (tableHasRows(con, type + "_stable_id")) {

			return checkForOrphans(con, type, type + "_id", type + "_stable_id", type + "_id", false);

		}

		return true;

	} // checkStableIDKeys

	// -------------------------------------------------------------------------
	private boolean checkRegulatoryFeatureKeys(Connection con, String type) {

		String table = type.toLowerCase();

		int rows = getRowCount(con, "SELECT COUNT(*) FROM regulatory_feature_object rfo LEFT JOIN " + table
				+ " ON rfo.ensembl_object_id=" + table + "." + table + "_id WHERE rfo.ensembl_object_type=\'" + type
				+ "\' AND rfo.ensembl_object_id IS NULL");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " in regulatory_feature refer to non-existent " + table + "s");
			return false;

		} else {

			ReportManager.correct(this, con, "All rows in regulatory_feature refer to valid " + table + "s");
			return true;
		}

	} // checkRegulatoryFeatureKeys

	// -------------------------------------------------------------------------

} // CoreForeignKeys
