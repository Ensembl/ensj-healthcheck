package org.ensembl.healthcheck.testcase.production;

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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check consistency of foreign key relationships in the ensembl_production database.
 */

public class ProductionForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public ProductionForeignKeys() {

		addToGroup("release");
		setDescription("Check for broken foreign-key relationships in the production database.");
    setTeamResponsible("Core");


	}

	/**
	 * Look for broken foreign key relationships.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// ----------------------------
		// Check stable IDs all correspond to an existing object
		
		result &= checkForOrphans(con, "database_list", "species_id", "species_list", "species_id", true);

	
		return result;
	
	}
	
} // ProductionForeignKeys
