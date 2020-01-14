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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the AUTO_INCREMENT flag is set for certain columns.
 */

public class AutoIncrement extends SingleDatabaseTestCase {

	String[] columns = { "alt_allele.alt_allele_id", "analysis.analysis_id", "assembly_exception.assembly_exception_id", "attrib_type.attrib_type_id", "coord_system.coord_system_id", "data_file.data_file_id",  
			"density_feature.density_feature_id", "density_type.density_type_id", "ditag.ditag_id", "ditag_feature.ditag_feature_id", "dna_align_feature.dna_align_feature_id", "exon.exon_id", "external_db.external_db_id", 
			"gene.gene_id", "intron_supporting_evidence.intron_supporting_evidence_id", "karyotype.karyotype_id", "map.map_id", "mapping_session.mapping_session_id", "marker.marker_id", "marker_feature.marker_feature_id", "marker_synonym.marker_synonym_id",
			"meta.meta_id", "misc_feature.misc_feature_id", "misc_set.misc_set_id", "object_xref.object_xref_id", "operon.operon_id", "peptide_archive.peptide_archive_id", "prediction_exon.prediction_exon_id",
			"prediction_transcript.prediction_transcript_id", "protein_align_feature.protein_align_feature_id", "protein_feature.protein_feature_id", 
			"repeat_consensus.repeat_consensus_id", "repeat_feature.repeat_feature_id", "seq_region.seq_region_id", "seq_region_synonym.seq_region_synonym_id", "simple_feature.simple_feature_id",
			"transcript.transcript_id", "translation.translation_id", "unmapped_object.unmapped_object_id", "unmapped_reason.unmapped_reason_id", "xref.xref_id" };

	/**
	 * Constructor.
	 */
	public AutoIncrement() {

		setTeamResponsible(Team.CORE);
		setDescription("Check that the AUTO_INCREMENT flag is set for certain columns.");

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

		try {

			Statement stmt = con.createStatement();

			for (String tableColumn : columns) {

				String[] tableAndColumn = tableColumn.split("\\.");
				String table = tableAndColumn[0];
				String column = tableAndColumn[1];

				ResultSet rs = stmt.executeQuery(String.format("SELECT %s FROM %s LIMIT 1", column, table));

				rs.first();
				ResultSetMetaData rsmd = rs.getMetaData();

				if (!rsmd.isAutoIncrement(1)) {

					ReportManager.problem(this, con, String.format("Column %s in %s should have the AUTO_INCREMENT flag set, but does not", column, table));
					result = false;

				}

				rs.close();

			}

			stmt.close();

		} catch (SQLException se) {
			se.printStackTrace();
		}

		return result;

	} // run

} // AutoIncrement
