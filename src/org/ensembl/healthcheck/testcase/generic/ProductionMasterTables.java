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

import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseType;

/**
 * Check that the content of the master_* tables in the production databases matches the equivalent table in this database.
 */

public class ProductionMasterTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionMasterTables() {

		addToGroup("production");
		addToGroup("release");
		setDescription("Check that the content of the master_* tables in the production databases matches the equivalent table in this database");
		setPriority(Priority.AMBER);
		setEffect("Discrepancies between tables can cause problems.");
		setFix("Resync tables");
		setTeamResponsible("Release coordinator");

	}

    /**
	 * This test Does not apply to sanger_vega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
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

		String[] tables = { "attrib_type", "misc_set", "external_db", "unmapped_reason" };

		List<String> exceptions = new ArrayList<String>();
		exceptions.add("is_current");
		exceptions.add("created_by");
		exceptions.add("created_at");
		exceptions.add("modified_by");
		exceptions.add("modified_at");

		for (String table : tables) {
			String key = table + "_id";
			result &= compareProductionTableWithExceptions(dbre, table, key, "master_" + table, key, exceptions);
		}

		/*
		// compare all columns of some tables
		String[] tables = { "attrib_type", "misc_set" };

		for (String table : tables) {

			String key = table + "_id";
			result &= compareProductionTable(dbre, table, key, "master_" + table, key);

		}
		
		// external_db is an exception as we don't want to consider db_release
		List<String> exceptions = new ArrayList<String>();
		exceptions.add("db_release");
		
		result &= compareProductionTableWithExceptions(dbre, "external_db", "external_db_id", "master_external_db", "external_db_id", exceptions);
		*/

		return result;

	} // run

} // ProductionMasterTables
