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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check for null biotypes, and also for any 'ensembl' biotypes - should be
 * 'protein_coding'
 */
public class Biotypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Biotypes.
	 */
	public Biotypes() {

		addToGroup("post_genebuild");
		addToGroup("release");

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
		if (dbre.getType() != DatabaseType.VEGA) {
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

		int rows = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE biotype='ensembl'");

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
		String[] geneBiotypes = getColumnValues(con, "SELECT DISTINCT(biotype) FROM gene");

		// check transcript biotypes for each one
		for (int i = 0; i < geneBiotypes.length; i++) {

			String geneBiotype = geneBiotypes[i];
			String sql = "SELECT DISTINCT(t.biotype) AS biotype, COUNT(*) AS count FROM transcript t, gene g WHERE g.gene_id=t.gene_id AND g.biotype != t.biotype AND g.biotype='"
					+ geneBiotype + "' GROUP BY t.biotype";
			boolean thisBiotypeOK = true;

			try {

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {

					int rows = rs.getInt("count");
					String transcriptBiotype = rs.getString("biotype");
					ReportManager.problem(this, con, rows + " genes of biotype " + geneBiotype + " have transcripts with biotype "
							+ transcriptBiotype);
					thisBiotypeOK = false;
					result &= thisBiotypeOK;

				}

				if (thisBiotypeOK) {
					ReportManager.correct(this, con, "All genes with biotype " + geneBiotype + " have transcripts with matching biotypes");
				}

			} catch (SQLException se) {
				se.printStackTrace();
			}

		}

		return result;

	} // -------------------------------------------------------------------------

	private boolean checkTypesFromFile(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String file = "biotypes.txt";
		
		// use different files for other database types
		if (dbre.getType() == DatabaseType.CDNA) {
			file = "biotypes_cdna.txt";
		} else if (dbre.getType() == DatabaseType.VEGA) {
			file = "biotypes_vega.txt";
		} else if (dbre.getType() == DatabaseType.OTHERFEATURES ) {
			file = "biotypes_otherfeatures.txt";
		} 
		
		// use a custom biotypes file if it's set in database.properties
		String customFile = System.getProperty("biotypes.file");
		if (customFile != null) {
			logger.finest("Using custom biotypes file: " + customFile);
			file = customFile;
		} 

		String[] allowedBiotypes = Utils.readTextFile(file);

		// check gene and transcript biotypes
		String[] tables = { "gene", "transcript" };

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			String[] biotypes = getColumnValues(con, "SELECT DISTINCT(biotype) FROM " + table);

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
