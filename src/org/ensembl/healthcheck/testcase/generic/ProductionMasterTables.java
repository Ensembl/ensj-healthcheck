/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the content of the master_* tables in the production databases matches the equivalent table in this database.
 */

public class ProductionMasterTables extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionMasterTables() {

		setDescription("Check that the content of the master_* tables in the production databases matches the equivalent table in this database");
		setPriority(Priority.AMBER);
		setEffect("Discrepancies between tables can cause problems.");
		setFix("Resync tables");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	/**
	 * This test Does not apply to sangervega dbs
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
                exceptions.add("db_release");

		for (String table : tables) {
			String key = table + "_id";
			result &= compareProductionTableWithExceptions(dbre, table, key, "master_" + table, key, exceptions);
		}

		return result;

	} // run

} // ProductionMasterTables
