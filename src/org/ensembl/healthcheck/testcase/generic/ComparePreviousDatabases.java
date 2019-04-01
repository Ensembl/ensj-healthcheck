/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all species and database types in the previous release are
 * represented in the current release.
 */

public class ComparePreviousDatabases extends MultiDatabaseTestCase {

	/**
	 * Create a new instance of ComparePreviousDatabases
	 */
	public ComparePreviousDatabases() {

		setDescription(
				"Check that all species and database types in the previous release are represented in the current release.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	/**
	 * @param dbr
	 *            The database registry containing all the specified databases.
	 * @return True if the meta information is consistent within species.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		// look at all databases on the secondary server, check that we have an
		// equivalent
		DatabaseRegistry secondaryDBR = DBUtils.getSecondaryDatabaseRegistry();

		// need a full registry of all primary databases
		DatabaseRegistry primaryDBR = new DatabaseRegistry(null, null, null, false);

		// get the map of species, with associated set of types, for both primary and
		// secondary servers
		Map<String, Set<DatabaseType>> primarySpeciesAndTypes = primaryDBR.getSpeciesTypeMap();
		Map<String, Set<DatabaseType>> secondarySpeciesAndTypes = secondaryDBR.getSpeciesTypeMap();

		for (String s : secondarySpeciesAndTypes.keySet()) {

			if (s.equals(DatabaseRegistryEntry.UNKNOWN) || s.equals(DatabaseRegistryEntry.ANCESTRAL_SEQUENCES)) {
				continue;
			}

			// fail at once if there are no databases on the main server for this species at
			// all
			if (!primarySpeciesAndTypes.containsKey(s)) {

				ReportManager.problem(this, "", String.format(
						"Secondary server contains at least one database for %s (e.g. %s) but there are none on the primary server",
						s, (secondaryDBR.getAll(s))[0].getName()));
				result = false;

			} else {

				// now check by type
				for (DatabaseType t : secondarySpeciesAndTypes.get(s)) {

					Set<DatabaseType> primaryTypes = primarySpeciesAndTypes.get(s);

					if (!primaryTypes.contains(t)) {

						ReportManager.problem(this, "", String.format(
								"Secondary server has a %s database for %s but there is no equivalent on the primary server",
								t, s));
						result = false;

					}
				}

			}

		}

		return result;

	}

} // ComparePreviousDatabases
