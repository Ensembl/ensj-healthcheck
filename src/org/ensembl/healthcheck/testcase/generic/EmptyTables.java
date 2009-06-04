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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that all tables have data.
 */
public class EmptyTables extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of EmptyTablesTestCase
	 */
	public EmptyTables() {

		addToGroup("post_genebuild");
		addToGroup("release");

		setDescription("Checks that all tables have data");

		setTeamResponsible("GeneBuilders");
		
	}

	// ---------------------------------------------------------------------

	/**
	 * Define what tables are to be checked.
	 */
	private String[] getTablesToCheck(final DatabaseRegistryEntry dbre) {

		String[] tables = getTableNames(dbre.getConnection());
		Species species = dbre.getSpecies();
		DatabaseType type = dbre.getType();

		// ----------------------------------------------------
		if (type == DatabaseType.CORE || type == DatabaseType.VEGA) {

			// the following tables are allowed to be empty
			String[] allowedEmpty = { "alt_allele", "assembly_exception", "dnac", "density_feature", "density_type", "mapping_set", "seq_region_mapping", "unconventional_transcript_association", "oligo_array", "oligo_feature", "oligo_probe" };
			tables = remove(tables, allowedEmpty);

			// ID mapping related tables are checked in a separate test case
			String[] idMapping = { "gene_archive", "peptide_archive", "mapping_session", "stable_id_event" };
			tables = remove(tables, idMapping);

			// only rat has entries in QTL tables
			if (species != Species.RATTUS_NORVEGICUS) {
				String[] qtlTables = { "qtl", "qtl_feature", "qtl_synonym" };
				tables = remove(tables, qtlTables);
			}

			// seq_region_attrib only filled in for human and mouse
			if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS) {
				tables = Utils.removeStringFromArray(tables, "seq_region_attrib");
			}

			// map, marker etc
			if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS && species != Species.RATTUS_NORVEGICUS
					&& species != Species.DANIO_RERIO) {
				String[] markerTables = { "map", "marker", "marker_map_location", "marker_synonym", "marker_feature" };
				tables = remove(tables, markerTables);
			}

			// misc_feature etc
			if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS && species != Species.ANOPHELES_GAMBIAE) {
				String[] miscTables = { "misc_feature", "misc_feature_misc_set", "misc_set", "misc_attrib" };
				tables = remove(tables, miscTables);
			}

			// only certain species have a karyotype
			if (species != Species.ANOPHELES_GAMBIAE && species != Species.DROSOPHILA_MELANOGASTER && species != Species.HOMO_SAPIENS
					&& species != Species.MUS_MUSCULUS && species != Species.RATTUS_NORVEGICUS) {
				tables = Utils.removeStringFromArray(tables, "karyotype");
			}

			// for imported gene sets, supporting_feature is empty
			if (species == Species.TETRAODON_NIGROVIRIDIS || species == Species.SACCHAROMYCES_CEREVISIAE
					|| species == Species.CAENORHABDITIS_ELEGANS) {
				tables = Utils.removeStringFromArray(tables, "supporting_feature");
			}

			// only look for transcript & translation attribs in human, mouse, rat
			if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS && species != Species.RATTUS_NORVEGICUS) {
				tables = Utils.removeStringFromArray(tables, "transcript_attrib");
				tables = Utils.removeStringFromArray(tables, "translation_attrib");
			}

			// drosophila is imported, so no supporting features.
			if (species == Species.DROSOPHILA_MELANOGASTER) {
				tables = Utils.removeStringFromArray(tables, "supporting_feature");
			}

			// only human and mouse currently have ditag data
			if (species != Species.HOMO_SAPIENS && species != Species.MUS_MUSCULUS) {
				tables = Utils.removeStringFromArray(tables, "ditag");
				tables = Utils.removeStringFromArray(tables, "ditag_feature");
			}

			// don't check for unconventional transcript associations in species other
			// than human
			if (species != Species.HOMO_SAPIENS) {
				tables = Utils.removeStringFromArray(tables, "unconventional_transcript_association");
			}

			// we don't have many gene_attribs yet
			tables = Utils.removeStringFromArray(tables, "gene_attrib");

			// ----------------------------------------------------

		} else if (type == DatabaseType.EST || type == DatabaseType.OTHERFEATURES) {

			// Only a few tables need to be filled in EST
			String[] est = { "dna_align_feature", "meta_coord", "meta", "coord_system" };
			tables = est;

			// ----------------------------------------------------

		} else if (type == DatabaseType.ESTGENE) {

			// Only a few tables need to be filled in ESTGENE
			String[] estGene = { "gene", "transcript", "exon", "meta_coord", "coord_system", "gene_stable_id", "exon_stable_id",
					"translation_stable_id", "transcript_stable_id", "karyotype" };
			tables = estGene;

			// ----------------------------------------------------

		} else if (type == DatabaseType.CDNA) {

			// Only a few tables need to be filled in cDNA databases
			String[] cdna = { "assembly", "attrib_type", "dna_align_feature", "meta", "meta_coord", "seq_region", "seq_region_attrib" };
			tables = cdna;

		}

		// -----------------------------------------------------
		// many tables are allowed to be empty in vega databases
		if (type == DatabaseType.VEGA) {

			String[] allowedEmpty = { "affy_array", "affy_feature", "affy_probe", "analysis_description", "dna", "external_synonym",
					"go_xref", "identity_xref", "karyotype", "map", "marker", "marker_feature", "marker_map_location", "marker_synonym",
					"misc_attrib", "misc_feature", "misc_feature_misc_set", "misc_set", "prediction_exon", "prediction_transcript",
					"repeat_consensus", "repeat_feature", "simple_feature", "transcript_attrib",
					"transcript_supporting_feature", "translation_attrib", "unconventional_transcript_association" };
			tables = remove(tables, allowedEmpty);

		}

		return tables;

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that every table has more than 0 rows.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] tables = getTablesToCheck(dbre);
		Connection con = dbre.getConnection();

		// if there is only one coordinate system then there's no assembly
		if (getRowCount(con, "SELECT COUNT(*) FROM coord_system") == 1) {
			tables = Utils.removeStringFromArray(tables, "assembly");
			logger.finest(dbre.getName() + " has only one coord_system, assembly table can be empty");
		}

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			// logger.finest("Checking that " + table + " has rows");

			if (!tableHasRows(con, table)) {

				ReportManager.problem(this, con, table + " has zero rows");
				result = false;

			}
		}

		if (result) {
			ReportManager.correct(this, con, "All required tables have data");
		}

		return result;

	} // run

	
	// -----------------------------------------------------------------

	private String[] remove(final String[] src, final String[] tablesToRemove) {

		String[] result = src;

		for (int i = 0; i < tablesToRemove.length; i++) {
			result = Utils.removeStringFromArray(result, tablesToRemove[i]);
		}

		return result;

	}

	// -----------------------------------------------------------------

} // EmptyTablesTestCase
