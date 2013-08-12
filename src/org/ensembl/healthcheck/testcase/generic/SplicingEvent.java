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
 * Check that all tables have data.
 */
public class SplicingEvent extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of SplicingEventTestCase
	 */
	public SplicingEvent() {


		setDescription("Checks that splicing_event table has been updated.");

		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	// ---------------------------------------------------------------------
	/**
	 * Only applies to core databases
	 */
	public void types() {
		
		setAppliesToType(DatabaseType.CORE);
		
	}

	// ---------------------------------------------------------------------
	/**
	 * Check that table splicing_event has been updated.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
	      
	    if (tableHasRows(con, "splicing_event") && tableHasRows(con,"gene") ) {
	            result &= checkCountIsZero(con,"splicing_event","gene_id NOT IN (SELECT gene_id FROM gene)");
	    } 

		return result;

	} // run


} // SplicingEventTestCase
