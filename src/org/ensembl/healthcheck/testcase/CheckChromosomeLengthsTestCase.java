/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase;
import java.sql.*;
import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;
/**
 * Check that the chromosome lengths stored in various places are consistent.
 */
public class CheckChromosomeLengthsTestCase extends EnsTestCase {
	/**
	 * Creates a new instance of CheckChromosomeLengthsTestCase
	 */
	public CheckChromosomeLengthsTestCase() {
		addToGroup("post_genebuild");
		setDescription("Check that the chromosome lengths from the seq_region table agree with both the assembly table and the karyotype table.");
	}
	/**
	 * @return The test case result.
	 */
	public TestResult run() {
		boolean result = true;
		DatabaseConnectionIterator it = getDatabaseConnectionIterator();
		while (it.hasNext()) {
			Connection con = (Connection)it.next();
			String dbName = DBUtils.getShortDatabaseName(con);
			// check that there are some chromosomes
			// of course this may not necessarily be a problem for some species
			String chrSQL =
				"SELECT count(*) from seq_region sr, coord_system cs "
					+ "WHERE sr.coord_system_id=cs.coord_system_id AND cs.name='chromosome'";
			int rows = getRowCount(con, chrSQL);
			if (rows == 0) {
				result = false;
				ReportManager.problem(this, con, "No chromosomes in seq_region table");
			} else {
				ReportManager.correct(this, con, "seq_region table contains chromosomes");
			}
			AssemblyNameInfo assembly = new AssemblyNameInfo(con);
			String defaultAssembly = assembly.getMetaTableAssemblyDefault();
			logger.finest("assembly.default from meta table: " + defaultAssembly);
			// ---------------------------------------------------
			// Find any chromosomes that have different lengths in seq_region &
			// assembly, for the default assembly.
			// NB chromosome length should always be equal to (or possibly
			// greater than) the maximum assembly length
			// The SQL returns failures
			// ----------------------------------------------------
			String sql =
				"SELECT sr.name, sr.length "
					+ "FROM seq_region sr, assembly ass, coord_system cs "
					+ "WHERE cs.name='chromosome' "
					+ "AND sr.coord_system_id=cs.coord_system_id "
					+ "AND ass.asm_seq_region_id = sr.seq_region_id "
					+ "GROUP BY ass.asm_seq_region_id "
					+ "HAVING sr.length < MAX(ass.asm_end)";

			String[] chrs = getColumnValues(con, sql);
			if (chrs.length > 0) {
				result = false;
				ReportManager.problem(this, con, "Chromosome lengths are shorter in the seq_region table than in the assembly table:");
				for (int i = 0; i < chrs.length && i < 50; i++) {
					ReportManager.problem(this, con, " Chromosome " + chrs[i] + " is shorter in seq_region than in assembly");
				}
			} else {
				ReportManager.correct(
					this,
					con,
					"Chromosome lengths are equal or greater in the seq_region table compared to the assembly table");
			}
			// --------------------------------------------------
			// Find any chromosomes that have different lengths in karyotype &
			// chromosome tables.
			// The seq_region.length and karyotype.length should always be the
			// same.
			// The SQL returns failures
			// --------------------------------------------------
			String karsql =
				"SELECT sr.name, max(kar.seq_region_end), sr.length "
					+ "FROM seq_region sr, karyotype kar "
					+ "WHERE sr.seq_region_id=kar.seq_region_id "
					+ "GROUP BY kar.seq_region_id "
					+ "HAVING sr.length <> MAX(kar.seq_region_end)";
			int count = 0;
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(karsql);
				if (rs != null) {
					while (rs.next() && count < 50) {
						count++;
						String chrName = rs.getString(1);
						int karLen = rs.getInt(2);
						int chrLen = rs.getInt(3);
						String prob = "";
						int bp = 0;
						if (karLen > chrLen) {
							bp = karLen - chrLen;
							prob = "longer";
						} else {
							bp = chrLen - karLen;
							prob = "shorter";
						}
						ReportManager.problem(
							this,
							con,
							"Chromosome "
								+ chrName
								+ " is "
								+ bp
								+ "bp "
								+ prob
								+ " in the karyotype table than "
								+ "in the seq_region table");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (count == 0) {
				ReportManager.correct(this, con, "Chromosome lengths are the same" + " in karyotype and seq_region tables");
			}
			// -------------------------------------------
		} // while connection
		// ------------------------------------------
		return new TestResult(getShortTestName(), result);
	} // run
} // CheckChromosomeLengthsTestCase
