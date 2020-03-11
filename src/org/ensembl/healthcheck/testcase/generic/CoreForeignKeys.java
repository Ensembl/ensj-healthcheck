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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class CoreForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public CoreForeignKeys() {

		setDescription("Check for broken foreign-key relationships.");
		setTeamResponsible(Team.CORE);
                setSecondTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Look for broken foreign key relationships.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if all foreign key relationships are valid.
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

		// for a sangervega db, ignore misc_featres whcih have no annotation
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			result &= checkForOrphansWithConstraint(con, "misc_feature", "misc_feature_id", "misc_attrib", "misc_feature_id",
					"misc_feature_id NOT IN (select mfms.misc_feature_id from misc_feature_misc_set as mfms join misc_set as ms on mfms.misc_set_id=ms.misc_set_id and ms.code='noAnnotation')");
		} else {
			result &= checkForOrphans(con, "misc_feature", "misc_feature_id", "misc_attrib", "misc_feature_id", true);
		}

		result &= checkForOrphans(con, "misc_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);

		result &= checkForOrphans(con, "assembly_exception", "seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "assembly_exception", "exc_seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "protein_feature", "translation_id", "translation", "translation_id", true);

		result &= checkForOrphans(con, "marker_synonym", "marker_id", "marker", "marker_id", true);

		result &= checkForOrphans(con, "translation_attrib", "translation_id", "translation", "translation_id", true);

		result &= checkForOrphans(con, "transcript_attrib", "transcript_id", "transcript", "transcript_id", true);

		/*
		 * // now redundant (done for all tables with analysis_id) result &= checkForOrphans(con, "analysis_id", "analysis",
		 * "analysis_id", true); result &= checkForOrphans(con, "transcript", "analysis_id", "analysis", "analysis_id", true);
		 */

		result &= checkForOrphans(con, "external_synonym", "xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "identity_xref", "object_xref_id", "object_xref", "object_xref_id", true);

		result &= checkForOrphans(con, "supporting_feature", "exon_id", "exon", "exon_id", true);

		result &= checkForOrphans(con, "translation", "transcript_id", "transcript", "transcript_id", true);

		result &= checkForOrphans(con, "ontology_xref", "object_xref_id", "object_xref", "object_xref_id", true);
		
		result &= checkForOrphans(con, "associated_xref", "object_xref_id", "object_xref", "object_xref_id", true);

		result &= checkForOrphans(con, "associated_xref", "xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "associated_xref", "source_xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "object_xref_id", "object_xref", "object_xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "master_xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "dependent_xref_id", "xref", "xref_id", true);

		// stable ID archive
		result &= checkForOrphansWithConstraint(con, "gene_archive", "peptide_archive_id", "peptide_archive", "peptide_archive_id", "peptide_archive_id != 0");
		result &= checkForOrphans(con, "peptide_archive", "peptide_archive_id", "gene_archive", "peptide_archive_id", true);
		result &= checkForOrphans(con, "stable_id_event", "mapping_session_id", "mapping_session", "mapping_session_id", false);
		result &= checkForOrphans(con, "gene_archive", "mapping_session_id", "mapping_session", "mapping_session_id", true);

		// ----------------------------
		// Check object xrefs point to existing objects
		String[] types = { "Gene", "Transcript", "Translation" };
		for (int i = 0; i < types.length; i++) {
			result &= checkKeysByEnsemblObjectType(con, "object_xref", types[i]);
		}

		// ----------------------------
		// Ensure that feature tables reference existing seq_regions
		String[] featTabs = getCoreFeatureTables();

		for (int i = 0; i < featTabs.length; i++) {
			String featTab = featTabs[i];
			// skip large tables as this test takes an inordinately long time
			// if (featTab.equals("protein_align_feature") || featTab.equals("dna_align_feature") || featTab.equals("repeat_feature")) {
			// continue;
			// }
			result &= checkForOrphans(con, featTab, "seq_region_id", "seq_region", "seq_region_id", true);
		}

		result &= checkForOrphans(con, "analysis_description", "analysis_id", "analysis", "analysis_id", true);

		result &= checkForOrphans(con, "gene_attrib", "gene_id", "gene", "gene_id", true);
		result &= checkForOrphans(con, "gene_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
		result &= checkForOrphans(con, "transcript_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
		result &= checkForOrphans(con, "translation_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);

		result &= checkForOrphans(con, "translation", "end_exon_id", "exon", "exon_id", true);
		result &= checkForOrphans(con, "translation", "start_exon_id", "exon", "exon_id", true);

		result &= checkForOrphans(con, "alt_allele", "gene_id", "gene", "gene_id", true);

		result &= checkForOrphans(con, "marker_map_location", "map_id", "map", "map_id", true);
		result &= checkForOrphans(con, "marker_map_location", "marker_id", "marker", "marker_id", true);
		result &= checkForOrphans(con, "marker_map_location", "marker_synonym_id", "marker_synonym", "marker_synonym_id", true);

		// 73 -> 74 core schema patch: qtl* removed
		// result &= checkForOrphans(con, "qtl_feature", "qtl_id", "qtl", "qtl_id", true);
		// result &= checkForOrphans(con, "qtl_synonym", "qtl_id", "qtl", "qtl_id", true);

		result &= checkForOrphans(con, "assembly", "asm_seq_region_id", "seq_region", "seq_region_id", true);

		result &= checkForOrphans(con, "unmapped_object", "unmapped_reason_id", "unmapped_reason", "unmapped_reason_id", true);
		result &= checkForOrphans(con, "unmapped_object", "analysis_id", "analysis", "analysis_id", true);

		result &= checkForOrphansWithConstraint(con, "supporting_feature", "feature_id", "dna_align_feature", "dna_align_feature_id", "feature_type = 'dna_align_feature'");

		result &= checkForOrphansWithConstraint(con, "supporting_feature", "feature_id", "protein_align_feature", "protein_align_feature_id", "feature_type = 'protein_align_feature'");

		result &= checkForOrphansWithConstraint(con, "transcript_supporting_feature", "feature_id", "dna_align_feature", "dna_align_feature_id", "feature_type = 'dna_align_feature'");

		result &= checkForOrphansWithConstraint(con, "transcript_supporting_feature", "feature_id", "protein_align_feature", "protein_align_feature_id", "feature_type = 'protein_align_feature'");

		result &= checkForOrphans(con, "density_feature", "density_type_id", "density_type", "density_type_id");

		result &= checkForOrphans(con, "prediction_exon", "prediction_transcript_id", "prediction_transcript", "prediction_transcript_id");

		// result &= checkForOrphans(con, "prediction_exon", "prediction_exon_id", "exon", "exon_id");

		result &= checkForOrphans(con, "marker", "display_marker_synonym_id", "marker_synonym", "marker_synonym_id");

		// optional relations
		// 73 -> 74 core schema patch: qtl* removed
		// result &= checkOptionalRelation(con, "qtl", "flank_marker_id_1", "marker", "marker_id");
		// result &= checkOptionalRelation(con, "qtl", "flank_marker_id_2", "marker", "marker_id");
		// result &= checkOptionalRelation(con, "qtl", "peak_marker_id", "marker", "marker_id");
		result &= checkOptionalRelation(con, "unmapped_object", "external_db_id", "external_db", "external_db_id");

		/*
		 * don't test
		 * 
		 * // too slow result &= checkForOrphans(con, "repeat_feature", "repeat_consensus_id", "repeat_consensus",
		 * "repeat_consensus_id");
		 */

		// ----------------------------
		// Check tables which reference the analysis table
		String[] analysisTabs = getCoreTablesWithAnalysisID();

		for(String analysisTab: analysisTabs) {
			// skip large tables as this test takes an inordinately long time
			if (analysisTab.equals("protein_align_feature") || analysisTab.equals("dna_align_feature") || analysisTab.equals("repeat_feature")) {
				continue;
			}
			
			String constraint = "analysis_id IS NOT NULL";
			if("object_xref".equals(analysisTab)) {
			  constraint += " and "+analysisTab+".analysis_id <> 0";
			}

			if (countOrphansWithConstraint(con, analysisTab, "analysis_id", "analysis", "analysis_id", constraint) > 0) {
				ReportManager.problem(this, con, "FAILED " + analysisTab + " -> analysis using FK analysis_id relationships");
				result = false;
			}

		}

		// end new tests


		// added by dr2: check that the foreign key display_marker_synonym_id points to a synonym
		// for the marker
		result &= checkDisplayMarkerSynonymID(con);

		return result;

	}


	// -------------------------------------------------------------------------
	public boolean checkKeysByEnsemblObjectType(Connection con, String baseTable, String type) {

		// Need to handle under scores in tables here
		// e.g. ProbeFeature > probe_feature
		String table = type.replaceAll("([a-z])([A-Z])", "$1_$2");
		table = table.toLowerCase();

		// Where is ensembl_object_id used?
		String column = baseTable.equals("object_xref") ? "ensembl_id" : "ensembl_object_id";

		return checkForOrphansWithConstraint(con, baseTable, column, table, table + "_id", "ensembl_object_type=\'" + type + "\'");

		/**
		 * Is this not just checkForOrphansWithConstraint?
		 * 
		 * 
		 * //int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + baseTable + " x LEFT JOIN " + table + " ON x." + column + "=" + table
		 * + "." + table + // "_id WHERE x.ensembl_object_type=\'" + type + "\' AND " + table + "." + table + "_id IS NULL");
		 * 
		 * 
		 * if (rows > 0) {
		 * 
		 * ReportManager.problem(this, con, rows + " rows in " + baseTable + " refer to non-existent " + table + "s"); return false;
		 * 
		 * } else {
		 * 
		 * ReportManager.correct(this, con, "All rows in " + baseTable + " refer to valid " + table + "s"); return true; }
		 **/

	} // checkKeysByEnsemblObjectType


	private boolean checkDisplayMarkerSynonymID(Connection con) {
		boolean result = true;

		// the foreign key has been checked before, but might not point to a marker_synonym
		// of this markers=
		int rows = DBUtils.getRowCount(con, "select count(*) from marker m where m.display_marker_synonym_id not in " + "(select ms.marker_synonym_id from marker_synonym ms where m.marker_id = ms.marker_id)");
		if (rows > 0) {
			// problem, there are markers that have display_marker_synonym_id that is not part of the
			// synonyms for the marker
			String useful_sql = "select m.marker_id, m.display_marker_synonym_id, ms1.marker_synonym_id, ms1.name from marker m, marker_synonym ms1 where m.marker_id = ms1.marker_id and m.display_marker_synonym_id not in (select ms.marker_synonym_id from marker_synonym ms where m.marker_id = ms.marker_id)";
			ReportManager.problem(this, con, rows + " rows in marker table have a display_marker_synonym that is not part of the synonyms for this marker" + " Try '" + useful_sql
					+ "' to find out the offending markers");
			result = false;
		}
		return result;
	} // checkDisplayMarkerSynonymID
} // CoreForeignKeys
