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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for any genes/exons that are suspiciously large; > 2Mb for genes, > 0.5Mb for exons. Length assumed to be end-start+1, i.e.
 * including introns.
 */

public class BigGeneExon extends SingleDatabaseTestCase {

	private static long GENE_WARN = 1000000; // warn if length greater than this

	private static long GENE_ERROR = 3000000; // throw if length greater than this

	private static long GENE_ENORMOUS = 15000000; // really complain if length greater than this

	private static long EXON_ERROR = 500000; // warn if length greater than this

	/**
	 * Create a new BigGeneExon testcase.
	 */
	public BigGeneExon() {

		setDescription("Check for suspiciously long genes & exons");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.RNASEQ);

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

		// gene - warning
		String sql = "SELECT COUNT(*) FROM gene WHERE (seq_region_end-seq_region_start+1) >= " + GENE_WARN + " AND (seq_region_end-seq_region_start+1) < " + GENE_ERROR;
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (source='havana' or source='WU')";
		}
		int rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {

			ReportManager.info(this, con, rows + " genes are longer than " + GENE_WARN + " bases but less than " + GENE_ERROR + " bases");

		} else {

			ReportManager.correct(this, con, "No genes longer than " + GENE_WARN + " bases but less than " + GENE_ERROR + " bases");

		}

		// gene - error
		sql = "SELECT gene_id FROM gene WHERE (seq_region_end-seq_region_start+1) >= " + GENE_ERROR;
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (source='havana' or source='WU')";
		}

		String[] longIDs = DBUtils.getColumnValues(con, sql);

		if (longIDs.length > 0) {

			String s = longIDs.length > 1 ? "s are " : " is ";
			ReportManager.problem(this, con, longIDs.length + " gene" + s + "longer than " + GENE_ERROR + " bases");
			printLongGeneDetails(con, longIDs);
			result = false;

		} else {

			ReportManager.correct(this, con, "No genes longer than " + GENE_ERROR + " bases");

		}

		// gene - really long
		sql = "SELECT gene_id FROM gene WHERE (seq_region_end-seq_region_start+1) >= " + GENE_ENORMOUS;
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU
			sql += " and (source='havana' or source='WU')";
		}

		longIDs = DBUtils.getColumnValues(con, sql);

		if (longIDs.length > 0) {

			String s = longIDs.length > 1 ? "s are " : " is ";
			ReportManager.problem(this, con, longIDs.length + " gene" + s + "longer than " + GENE_ENORMOUS + " bases - this can't be right!");
			printLongGeneDetails(con, longIDs);
			result = false;

		} else {

			ReportManager.correct(this, con, "No genes longer than " + GENE_ENORMOUS + " bases");

		}

		// exon - error
		sql = "SELECT COUNT(*) FROM exon WHERE (seq_region_end-seq_region_start+1) >= " + EXON_ERROR;

		rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {

			ReportManager.problem(this, con, rows + " exons are longer than " + EXON_ERROR + " bases");
			result = false;

		} else {

			ReportManager.correct(this, con, "No exons longer than " + EXON_ERROR + " bases");

		}

		return result;

	} // run

	// ------------------------------------------------------------------------------------

	private void printLongGeneDetails(Connection con, String[] longIDs) {

		for (int i = 0; i < longIDs.length; i++) {

			String id = longIDs[i];

			// can't do one single query as not all genes may have
			// display_xrefs/descriptions
			String length = DBUtils.getRowColumnValue(con, "SELECT (seq_region_end-seq_region_start+1) AS length FROM gene WHERE gene_id=" + id);
			String stableID = DBUtils.getRowColumnValue(con, "SELECT stable_id FROM gene WHERE gene_id=" + id);
			String name = DBUtils.getRowColumnValue(con, "SELECT x.display_label FROM gene g, xref x WHERE x.xref_id=g.display_xref_id AND g.gene_id=" + id);
			String description = DBUtils.getRowColumnValue(con, "SELECT description FROM gene WHERE gene_id=" + id);

			String str = "Gene " + stableID;
			if (name != null && name.length() > 0) {
				str += " (" + name + ")";
			}
			str += " has length " + length;
			if (description != null && description.length() > 0) {
				str += " (" + description + ")";
			}

			ReportManager.problem(this, con, str);

		}

	}

	// ------------------------------------------------------------------------------------

} // BigGeneExon
