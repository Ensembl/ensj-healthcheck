/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.generic;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * EnsEMBL Healthcheck test case that ensures that the results of the SQL query <code>DESCRIBE external_db</code> are the same for a
 * set of databases.
 */

public class ExternalDBDescribe extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.VEGA };

	/**
	 * Create a new ExternalDBDescribe test case.
	 */
	public ExternalDBDescribe() {

		setDescription("Check that the external_db table is the same in all databases.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	/**
	 * Check that the external_db tables are the same for each matched database.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		for (DatabaseType type : types) {

			// build the list of databases to check; currently this is just so that the master_schema.*databases can be ignored
			List<DatabaseRegistryEntry> databases = new ArrayList<DatabaseRegistryEntry>();

			for (DatabaseRegistryEntry dbre : dbr.getAll(type)) {
				if (dbre.getName().matches("master_schema.*")) {
					continue;
				}

				databases.add(dbre);
			}

			// ignore db_release column as this is allowed to be different between species
			result &= checkSameSQLResult("SELECT external_db_id, db_name, status, priority, db_display_name, type FROM external_db ORDER BY external_db_id", databases.toArray(new DatabaseRegistryEntry[databases.size()]), false);
	
		}

		return result;

	} // run

} // ExternalDBDescribe
