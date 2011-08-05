/*
 * Copyright (C) 2003 EBI, GRL
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

import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.util.Utils;

/**
 * Compare rows in the assembly table in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionAssembly extends ComparePreviousVersionBase {

	/**
	 * This class is used in test case MetaValues
	 */
	public ComparePreviousVersionAssembly() {
		setTeamResponsible(Team.RELEASE_COORDINATOR);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		
		Map rowCounts = new HashMap();

		rowCounts.put("assembly", new Integer(getRowCount(con, "SELECT COUNT(*) FROM assembly")));

		return rowCounts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "rows in";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {
		return 1;
	}

	protected boolean testUpperThreshold() {
		return true;
	}

	
	// ------------------------------------------------------------------------

} // ComparePreviousVersionAssembly
