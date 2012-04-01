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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Healthcheck for the assembly_exception table.
 */

public class AssemblyExceptions extends SingleDatabaseTestCase {

	/**
	 * Check the assembly_exception table.
	 */
	public AssemblyExceptions() {
		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		setDescription("Check assembly_exception table");
		setTeamResponsible(Team.GENEBUILD);
		addToGroup("compara-ancestral");

	}

	/**
	 * Check the data in the assembly_exception table. Note referential integrity checks are done in CoreForeignKeys.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that seq_region_end > seq_region_start
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE seq_region_start > seq_region_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where seq_region_start > seq_region_end");
		}

		// check that exc_seq_region_start > exc_seq_region_end
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE exc_seq_region_start > exc_seq_region_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "assembly_exception has " + rows + " rows where exc_seq_region_start > exc_seq_region_end");
		}

		// If the assembly_exception table contains an exception of type 'HAP' then
		// there should be at least one seq_region_attrib row of type 'non-reference'
		if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM assembly_exception WHERE exc_type='HAP'") > 0) {

			if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM seq_region_attrib sra, attrib_type at WHERE sra.attrib_type_id=at.attrib_type_id AND at.code='non_ref'") == 0) {
				result = false;
				ReportManager.problem(this, con, "assembly_exception contains at least one exception of type 'HAP' but there are no seq_region_attrib rows of type 'non-reference'");
			}

		}

		if (result) {
			ReportManager.correct(this, con, "assembly_exception start/end co-ordinates make sense");
		}

		return result;

	}

} // AssemblyException
