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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check for multiple components which overlap and are assembled to the same
 * thing. Note that multiple assembly is OK, overlapping components is not.
 */
public class AssemblyMultipleOverlap extends SingleDatabaseTestCase {

	private static final int MAX = 10; // maximum number of overlaps to print

	/**
	 * Creates a new instance of AssemblyMultipleOverlap.
	 */
	public AssemblyMultipleOverlap() {

		addToGroup("post_genebuild");
		addToGroup("release");

		setDescription("Check for multiple components which overlap and are assembled to the same thing.");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// get list of all multiply-assembled components
		String sql = "SELECT sr1.name AS cmp_sr_name, cs1.name AS cmp_cs, sr2.name AS asm_sr_name, cs2.name AS asm_cs, a.asm_seq_region_id, a.cmp_seq_region_id, COUNT(*) AS count "
				+ "FROM assembly a, seq_region sr1, seq_region sr2, coord_system cs1, coord_system cs2 "
				+ "WHERE a.cmp_seq_region_id = sr1.seq_region_id AND a.asm_seq_region_id = sr2.seq_region_id "
				+ "AND sr1.coord_system_id = cs1.coord_system_id AND sr2.coord_system_id = cs2.coord_system_id "
				+ "GROUP BY asm_seq_region_id, cmp_seq_region_id, asm_start, cmp_start, ori HAVING count > 1;";

		int overlapCount = 0;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			PreparedStatement cmpStmt = con
					.prepareStatement("SELECT asm_start, asm_end, ori FROM assembly WHERE asm_seq_region_id=? AND cmp_seq_region_id=? ORDER BY asm_start");

			while (rs.next()) {

				long asm_seq_region_id = rs.getLong("asm_seq_region_id");
				long cmp_seq_region_id = rs.getLong("cmp_seq_region_id");

				// get start, end for each component
				cmpStmt.setLong(1, asm_seq_region_id);
				cmpStmt.setLong(2, cmp_seq_region_id);
				ResultSet cmpRS = cmpStmt.executeQuery();

				// read all start/end/strand
				List startsL = new ArrayList();
				List endsL = new ArrayList();
				List strandsL = new ArrayList();
				while (cmpRS.next()) {

					startsL.add(new Long(cmpRS.getLong("asm_start")));
					endsL.add(new Long(cmpRS.getLong("asm_end")));
					strandsL.add(new Long(cmpRS.getLong("ori")));

				}

				cmpRS.close();

				// convert to arrays - easier to keep track
				long starts[] = Utils.listToArrayLong(startsL);
				long ends[] = Utils.listToArrayLong(endsL);
				long strands[] = Utils.listToArrayLong(strandsL);

				// check pairs for overlaps
				// note ORDER BY asm_start means we have less comparisions to do
				for (int i = 0; i < starts.length; i++) {
					for (int j = i + 1; j < starts.length; j++) {

						if (strands[i] == strands[j]) {

							if (starts[j] < ends[i]) {
								overlapCount++;
								if (overlapCount < MAX) {
								    //System.out.println("Overlap: cmp " + cmp_seq_region_id + " asm " + asm_seq_region_id + " " + starts[i] + " " + ends[i] + " " + starts[j] + " " + ends[j]);
								}
							}
						}

					}

				}

			}

			rs.close();

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		if (overlapCount > 0) {

			ReportManager.problem(this, con, overlapCount + " instances of multiple overlapping assembled components");
			result = false;

		} else {

			ReportManager.correct(this, con, "No multiply-assembled overlapping components");

		}

		return result;

	} // run

	// -------------------------------------------------------------------------

} // AssemblyMultipleOverlap
