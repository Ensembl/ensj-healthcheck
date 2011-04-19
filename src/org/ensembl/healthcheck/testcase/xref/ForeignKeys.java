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
package org.ensembl.healthcheck.testcase.xref;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check foreign key constraints in xref databases.
 */

public class ForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create a new ForeignKeys testcase.
	 */
	public ForeignKeys() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("xrefs");
		setDescription("Check foreign key constraints in xref databases.");
		setTeamResponsible(Team.CORE);

	}

	// ----------------------------------------------------------------------

	/**
	 * This only applies to xref databases
	 */
	public void types() {

		List<DatabaseType> types = new ArrayList<DatabaseType>();
		types.add(DatabaseType.XREF);
		setAppliesToTypes(types);

	}

	// ----------------------------------------------------------------------
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

		result &= check(con, "xref", "source", "source_id");

		result &= check(con, "xref", "species", "species_id");

		result &= check(con, "primary_xref", "xref", "xref_id");

		result &= check(con, "synonym", "xref", "xref_id");

		result &= check(con, "source_url", "source", "source_id");

		result &= check(con, "source_url", "species", "species_id");

		result &= checkForOrphans(con, "direct_xref", "general_xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "master_xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "dependent_xref_id", "xref", "xref_id", true);

		result &= checkForOrphans(con, "dependent_xref", "linkage_source_id", "source", "source_id", true);

		return result;

	} // run

	// ----------------------------------------------------------------------
	/**
	 * Shortened version of checkForOrphans; assumes column to be checked is same in both tables, and check is only "one way".
	 */
	private boolean check(Connection con, String table1, String table2, String column) {

		return checkForOrphans(con, table1, column, table2, column, true);

	}

	// ----------------------------------------------------------------------

} // ForeignKeys
