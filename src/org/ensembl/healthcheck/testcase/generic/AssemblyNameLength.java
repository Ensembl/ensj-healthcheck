/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks that meta_value for key assembly.name is not longer than 16 characters - the current display width limit on the website.
 */
public class AssemblyNameLength extends SingleDatabaseTestCase {
	
	public AssemblyNameLength() {

		addToGroup("pre-compara-handover");	
                addToGroup("post-compara-handover");
		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check that meta_value for key assembly.name is not longer than 16 characters");
	}

	/**
	 * Checks that meta_value for key assembly.name is not longer than 16 characters.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {
	
		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='assembly.name'");
		if (rows == 0) {
			ReportManager.problem(this, con, "No entry in meta table for assembly.name");
			return false;
		} else {
			int asemblyNameLength = Integer.valueOf(DBUtils.getRowColumnValue(con, "SELECT LENGTH(meta_value) FROM meta WHERE meta_key='assembly.name'")).intValue();

			if (asemblyNameLength > 16 ) {
				ReportManager.problem(this, con, "assembly.name value in Meta table is longer than 16 characters");
				return false;			
			}		
		
		}

		return result;

	} // run

	
} // AssemblyNameLength
