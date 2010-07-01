/*
 Copyright (C) 2004 EBI, GRL
 
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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Healthcheck for the dna table. Should be empty in an est database
 */

public class DNAEmpty extends SingleDatabaseTestCase {

	/**
	 * Check the assembly_exception table.
	 */
	public DNAEmpty() {
		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that dna table is empty");
	}


	/**
	 * This applies to all core schema databases apart from 'core' and 'sanger_vega'
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.SANGER_VEGA);		

	}
	
	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = getRowCount(con, "SELECT COUNT(*) FROM dna");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "dna table contains " + rows + " rows - it should be empty unless it's a core database ");
		} else {
			ReportManager.correct(this, con, "dna table is empty");
		}

		return result;

	}

} // DNAEmpty
