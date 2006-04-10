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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that there are no duplicates in the assembly table.
 */

public class DuplicateAssembly extends SingleDatabaseTestCase {

	/**
	 * Create a new DuplicateAssembly testcase.
	 */
	public DuplicateAssembly() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that there are no duplicates in the assembly table");

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

		int rows = getRowCount(
				con,
				"SELECT *, COUNT(*) AS c FROM assembly GROUP BY asm_seq_region_id, cmp_seq_region_id, asm_start, asm_end, cmp_start, cmp_end, ori HAVING c > 1");

		if (rows > 0) {

			ReportManager.problem(this, con, "At least " + rows + " duplicate rows in assembly table");
			result = false;

		} else {

			ReportManager.correct(this, con, "No duplicate rows in the assembly table");

		}

		return result;

	} // run

	/**
	 * Note more details can be obtained via: 
	 * 
	 * SELECT a.*, sr1.name, cs1.name, sr2.name, cs2.name, COUNT(*) AS c 
	 * FROM assembly a, seq_region sr1, seq_region sr2, coord_system cs1, coord_system cs2 
	 * WHERE a.cmp_seq_region_id = sr1.seq_region_id AND
	 *       sr1.coord_system_id = cs1.coord_system_id AND
	 *       a.asm_seq_region_id = sr2.seq_region_id AND
	 *       sr2.coord_system_id = cs2.coord_system_id 
	 * GROUP BY asm_seq_region_id,  cmp_seq_region_id, asm_start, asm_end, cmp_start, cmp_end, ori
	 * HAVING c > 1;
	 */
	
} // DuplicateAssembly
