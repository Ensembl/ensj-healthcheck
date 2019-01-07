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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check for multiple components which overlap and are assembled to the same thing. Note that multiple assembly is OK, overlapping
 * components is not.
 */
public class AssemblyMultipleOverlap extends SingleDatabaseTestCase {

	private static final int MAX = 10; // maximum number of overlaps to print

	/**
	 * Creates a new instance of AssemblyMultipleOverlap.
	 */
	public AssemblyMultipleOverlap() {

		setDescription("Check for multiple components which overlap and are assembled to the same thing.");
		setTeamResponsible(Team.GENEBUILD);
	}

        /**
         * Data is only tested in core database, as the tables are in sync
         */
        public void types() {

                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

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

		// get list of all multiply-assembled components
		String sql = "SELECT sr1.name AS cmp_sr_name, cs1.name AS cmp_cs, sr2.name AS asm_sr_name, cs2.name AS asm_cs, a.asm_seq_region_id, a.cmp_seq_region_id, COUNT(*) AS count "
				+ "FROM assembly a, seq_region sr1, seq_region sr2, coord_system cs1, coord_system cs2 " + "WHERE a.cmp_seq_region_id = sr1.seq_region_id AND a.asm_seq_region_id = sr2.seq_region_id "
				+ "AND sr1.coord_system_id = cs1.coord_system_id AND sr2.coord_system_id = cs2.coord_system_id " + "GROUP BY asm_seq_region_id, cmp_seq_region_id, asm_start, cmp_start, ori HAVING count > 1;";

		int overlapCount = 0;

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			PreparedStatement cmpStmt = con.prepareStatement("SELECT asm_start, asm_end, ori FROM assembly WHERE asm_seq_region_id=? AND cmp_seq_region_id=? ORDER BY asm_start");

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

					startsL.add(cmpRS.getLong("asm_start"));
					endsL.add(cmpRS.getLong("asm_end"));
					strandsL.add(cmpRS.getLong("ori"));

				}

				cmpRS.close();

				// convert to arrays - easier to keep track
				long starts[] = Utils.listToArrayLong(startsL);
				long ends[] = Utils.listToArrayLong(endsL);
				long strands[] = Utils.listToArrayLong(strandsL);

				// check pairs for overlaps
				// note ORDER BY asm_start means we have less comparisons to do
				for (int i = 0; i < starts.length; i++) {
					for (int j = i + 1; j < starts.length; j++) {

						if (strands[i] == strands[j]) {

							if (starts[j] < ends[i]) {
								overlapCount++;
								if (overlapCount < MAX) {
									// System.out.println("Overlap: cmp " + cmp_seq_region_id + " asm " + asm_seq_region_id + " " + starts[i] + " " +
									// ends[i] + " " + starts[j] + " " + ends[j]);
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
