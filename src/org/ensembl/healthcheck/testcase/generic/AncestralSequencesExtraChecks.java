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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Additional checks for the ancestral sequences database (from compara).
 */

public class AncestralSequencesExtraChecks extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public AncestralSequencesExtraChecks() {

		addToGroup("compara-ancestral");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		setTeamResponsible(Team.COMPARA);
		setDescription("Additional checks for the ancestral sequences database (from compara).");

	}

	
	/**
	 * Only applies to core databases
	 */
	public void types() {
		
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);

	}
	
	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= checkForOrphansWithConstraint(con, "seq_region", "seq_region_id", "dna", "seq_region_id",
				"coord_system_id = (SELECT coord_system_id FROM coord_system WHERE attrib LIKE '%sequence_level%')");

		return result;
	}

} // AncestralSequencesExtraChecks
