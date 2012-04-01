/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check for null biotypes, and also for any 'ensembl' biotypes - should be 'protein_coding'
 */
public class Biotypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Biotypes.
	 */
	public Biotypes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");

		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check for null biotypes, and also for any 'ensembl' biotypes - should be 'protein_coding'");

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
		result &= checkNull(con);
		result &= checkEnsembl(con);
		if (dbre.getType() != DatabaseType.VEGA && dbre.getType() != DatabaseType.SANGER_VEGA) {
			result &= checkGenesAndTranscripts(con);
		}

		result &= checkTypesFromFile(dbre);

		return result;

	} // run

	// -------------------------------------------------------------------------

	private boolean checkNull(Connection con) {

		return checkNoNulls(con, "gene", "biotype");

	}

	// -------------------------------------------------------------------------

	private boolean checkEnsembl(Connection con) {

		boolean result = true;

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM gene WHERE biotype='ensembl'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " genes have 'ensembl' biotypes - should probably be 'protein_coding'");
			result = false;

		} else {

			ReportManager.correct(this, con, "No 'ensembl' biotypes in gene table");

		}

		return result;

	}

	// -------------------------------------------------------------------------

	private boolean checkGenesAndTranscripts(Connection con) {

		boolean result = true;

		// be a bit more informative than just counting rows

		// get gene biotypes
		String[] geneBiotypes = DBUtils.getColumnValues(con, "SELECT DISTINCT(biotype) FROM gene");

		// check transcript biotypes for each one
		for (int i = 0; i < geneBiotypes.length; i++) {

			String geneBiotype = geneBiotypes[i];

			String[] mismatchedBiotypes = DBUtils.getColumnValues(con,
					String.format("SELECT DISTINCT(t.biotype) FROM transcript t, gene g WHERE g.gene_id=t.gene_id AND g.biotype != t.biotype AND g.biotype='%s'", geneBiotype));

			if (mismatchedBiotypes.length > 0) {

				result &= false;

				// get count for each one
				for (String transcriptBiotype : mismatchedBiotypes) {

					int rows = DBUtils.getRowCount(con,
							String.format("SELECT COUNT(DISTINCT g.gene_id) FROM transcript t, gene g WHERE g.gene_id=t.gene_id AND g.biotype='%s' AND t.biotype='%s'", geneBiotype, transcriptBiotype));
					ReportManager.problem(this, con, rows + " genes of biotype " + geneBiotype + " have transcripts with biotype " + transcriptBiotype);

				}

			} else {
				ReportManager.correct(this, con, "All genes with biotype " + geneBiotype + " have transcripts with matching biotypes");
			}

		}

		return result;

	} // -------------------------------------------------------------------------

	private boolean checkTypesFromFile(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// use a custom biotypes file if it's set in database.properties
		// otherwise do nothing
		String file = System.getProperty("biotypes.file");
		if (file != null) {
			logger.finest("Using custom biotypes file: " + file);
		} else {
			return true;
		}

		String[] allowedBiotypes = Utils.readTextFile(file);

		// check gene and transcript biotypes
		String[] tables = { "gene", "transcript" };

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			String[] biotypes = DBUtils.getColumnValues(con, "SELECT DISTINCT(biotype) FROM " + table);

			for (int j = 0; j < biotypes.length; j++) {

				String biotype = biotypes[j];

				if (!Utils.stringInArray(biotype, allowedBiotypes, false)) {
					ReportManager.problem(this, con, table + " contains invalid biotype '" + biotype + "'");
					result = false;
				}
			}

		}

		return result;

	}

	// -------------------------------------------------------------------------

} // Biotypes
