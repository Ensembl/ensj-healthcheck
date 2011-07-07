/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for sequence that does not match GTACN.
 */

public class NonGTACNSequence extends SingleDatabaseTestCase {

	/**
	 * Create a new NonGTACNSequence testcase.
	 */
	public NonGTACNSequence() {

		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");

		setDescription("Check for sequence that does not match GTACN.");
		setPriority(Priority.AMBER);
		setEffect("May indicate the presence of ambiguity codes.");
		setFix("Change affected bases to Ns");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Only applies to core databases
	 */
	public void types() {
		
		setAppliesToType(DatabaseType.CORE);
		
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

			// if this query returns anything then something is wrong
			ResultSet rs = stmt.executeQuery("SELECT sr.seq_region_id, sr.name FROM seq_region sr, dna d WHERE sr.seq_region_id=d.seq_region_id AND d.sequence REGEXP '[^ATGCN]'");

			while (rs != null && rs.next()) {

				result = false;
				
				ReportManager.problem(this, con, String.format("%s (seq_region_id %s) has non GTACN sequence", rs.getString(2), rs.getLong(1)));

			} // while rs

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (result == true) {
			
			ReportManager.correct(this, con, "No entries in dna table have non-GTACN bases.");

		}
	
		return result;

	} // run

} // NonGTACNSequence
