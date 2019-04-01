/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check that gencode basic attributes are present
 */

public class AttribValues extends SingleDatabaseTestCase {

	/**
	 * Create a new testcase.
	 */
	public AttribValues() {

		setDescription(
				"Check that some attributes have been added (currently, tsl, appris, gencode and refseq attributes)");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Only applies to core dbs.
	 */
	public void types() {

		List types = new ArrayList();

		types.add(DatabaseType.CORE);
		types.add(DatabaseType.PRE_SITE);

		setAppliesToTypes(types);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (!dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.MUS_MUSCULUS)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.RATTUS_NORVEGICUS)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.SUS_SCROFA)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.DANIO_RERIO)) {
			return result;
		}

		// Gencode and TSL attributes are only for human and mouse
		if (dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.MUS_MUSCULUS)) {
			result &= gencodeAttrib(con);
			result &= tslAttrib(dbre);
		}
		result &= refseqAttrib(con);
		result &= apprisAttrib(dbre);

		return result;

	}

	protected boolean gencodeAttrib(Connection con) {

		boolean result = true;
		int gencodeGenes = DBUtils.getRowCount(con,
				"SELECT COUNT(distinct gene_id) FROM transcript t, attrib_type at, transcript_attrib ta WHERE t.transcript_id = ta.transcript_id AND at.attrib_type_id=ta.attrib_type_id AND at.code='gencode_basic'");

		int rows = DBUtils.getRowCount(con,
				"SELECT COUNT(distinct gene_id) FROM transcript WHERE biotype NOT IN ('LRG_gene')");

		if (rows > gencodeGenes) {
			ReportManager.problem(this, con,
					(rows - gencodeGenes) + " genes do not have any transcripts with the gencode_basic attribute\n");
			result = false;
		} else {
			ReportManager.correct(this, con, rows + " gencode basic transcript attributes found");
		}

		return result;
	}

	protected boolean refseqAttrib(Connection con) {

		boolean result = true;
		int genes = DBUtils.getRowCount(con,
				"SELECT COUNT(distinct g.gene_id) FROM gene g, seq_region s, coord_system cs WHERE g.seq_region_id = s.seq_region_id AND "
						+ "s.coord_system_id = cs.coord_system_id AND cs.name = 'chromosome' AND cs.attrib = 'default_version' AND s.name NOT LIKE 'LRG%' "
						+ "AND s.name != 'MT' AND s.seq_region_id NOT IN (SELECT seq_region_id FROM assembly_exception WHERE exc_type in ('PATCH_NOVEL', 'PATCH_FIX', 'HAP'))");

		int refseqGenes = DBUtils.getRowCount(con,
				"SELECT COUNT(distinct g.gene_id) FROM gene g, seq_region s, coord_system cs, gene_attrib ga, attrib_type at WHERE g.seq_region_id = s.seq_region_id AND "
						+ "s.coord_system_id = cs.coord_system_id AND cs.name = 'chromosome' AND cs.attrib = 'default_version' AND s.name NOT LIKE 'LRG%' "
						+ "AND s.name != 'MT' AND s.seq_region_id NOT IN (SELECT seq_region_id FROM assembly_exception WHERE exc_type in ('PATCH_NOVEL', 'PATCH_FIX', 'HAP')) "
						+ "AND g.seq_region_id = s.seq_region_id AND ga.gene_id = g.gene_id AND ga.attrib_type_id = at.attrib_type_id AND code = 'refseq_compare'");

		if (genes > refseqGenes) {
			ReportManager.problem(this, con, (genes - refseqGenes) + " genes do not have the refseq_compare attribute");
			result = false;
		} else {
			ReportManager.correct(this, con, refseqGenes + " genes found with refseq_compare attribute");
		}

		return result;

	}

	protected boolean apprisAttrib(DatabaseRegistryEntry dbre) {

		boolean result = true;
		Connection con = dbre.getConnection();
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);

		String chromosomeSql = "SELECT DISTINCT s.name FROM seq_region s, seq_region_attrib sa, attrib_type at WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND code = 'karyotype_rank'";
		String codingSql = "SELECT count(distinct g.stable_id) FROM gene g, seq_region s WHERE g.seq_region_id = s.seq_region_id AND s.name = ? AND biotype = 'protein_coding'";
		String apprisSql = "SELECT count(distinct g.stable_id) FROM gene g, seq_region s, transcript t, transcript_attrib ta, attrib_type a WHERE g.seq_region_id = s.seq_region_id AND s.name = ? AND g.biotype = 'protein_coding' AND g.gene_id=t.gene_id AND t.transcript_id=ta.transcript_id AND ta.attrib_type_id=a.attrib_type_id AND a.code like 'appris%'";

		// If no data available at all, exit early
		String hasApprisSql = "SELECT count(*) FROM transcript t, transcript_attrib ta, attrib_type a WHERE t.transcript_id = ta.transcript_id AND ta.attrib_type_id = a.attrib_type_id AND code like 'appris%'";
		int hasAppris = DBUtils.getRowCount(con, hasApprisSql);
		if (hasAppris == 0) {
			ReportManager.problem(this, con, "No appris attributes found, have you imported the new data?");
			return false;
		}

		List<String> chromosomes = t.queryForDefaultObjectList(chromosomeSql, String.class);
		for (String chromosome : chromosomes) {
			int codingCount = t.queryForDefaultObject(codingSql, Integer.class, chromosome);
			int apprisCount = t.queryForDefaultObject(apprisSql, Integer.class, chromosome);
			if (apprisCount < codingCount * 0.95) {
				ReportManager.problem(this, con, chromosome + " has " + codingCount + " protein coding genes but only "
						+ apprisCount + " have a transcript-attrib like 'appris%'");
				result = false;
			}
		}

		if (result) {
			ReportManager.correct(this, con, "Found correct number of Appris attributes on all chromosomes");
		}

		return result;
	}

	protected boolean tslAttrib(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);

		String chromosomeSql = "SELECT DISTINCT s.name FROM seq_region s, seq_region_attrib sa, attrib_type at WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND code = 'karyotype_rank'";
		String transcriptSql = "SELECT count(distinct t.stable_id) from seq_region s, transcript t WHERE t.seq_region_id = s.seq_region_id and s.name = ?";
		String tslSql = "SELECT count(distinct t.stable_id) from seq_region s, transcript t, transcript_attrib ta, attrib_type a WHERE t.seq_region_id = s.seq_region_id AND t.transcript_id = ta.transcript_id AND ta.attrib_type_id = a.attrib_type_id AND a.code like 'tsl%' AND s.name = ?";

		// If no data available, exit early
		String hasTslSql = "SELECT count(*) FROM transcript t, transcript_attrib ta, attrib_type a WHERE t.transcript_id = ta.transcript_id AND ta.attrib_type_id = a.attrib_type_id AND code like 'tsl%'";
		int hasTsl = DBUtils.getRowCount(con, hasTslSql);
		if (hasTsl == 0) {
			ReportManager.problem(this, con, "No tsl attributes found, have you imported the new data?");
			return false;
		}

		String patchSql = "SELECT count(*) FROM transcript t, assembly_exception ax, transcript_attrib ta, attrib_type a "
				+ "WHERE t.seq_region_id = ax.seq_region_id AND t.transcript_id = ta.transcript_id AND ta.attrib_type_id = a.attrib_type_id AND code like 'tsl%'";
		int patchCount = DBUtils.getRowCount(con, patchSql);
		ReportManager.info(this, con, "There are " + patchCount + " transcripts with TSL attributes on patches");

		List<String> chromosomes = t.queryForDefaultObjectList(chromosomeSql, String.class);
		for (String chromosome : chromosomes) {
			int transcriptCount = t.queryForDefaultObject(transcriptSql, Integer.class, chromosome);
			int tslCount = t.queryForDefaultObject(tslSql, Integer.class, chromosome);
			if (tslCount < transcriptCount * 0.95) {
				ReportManager.problem(this, con, chromosome + " has " + transcriptCount + " transcripts but only "
						+ tslCount + " have a transcript-attrib like 'tsl%'");
				result = false;
			}
		}

		if (result) {
			ReportManager.correct(this, con, "Found correct number of TSL attributes on all chromosomes");
		}

		return result;
	}

	// ----------------------------------------------------------------------

} // AttribValues
