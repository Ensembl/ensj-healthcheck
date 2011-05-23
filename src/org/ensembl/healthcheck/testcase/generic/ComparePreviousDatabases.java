/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all species and database types in the previous release are represented in the current release.
 */

public class ComparePreviousDatabases extends MultiDatabaseTestCase {

	/**
	 * Create a new instance of ComparePreviousDatabases
	 */
	public ComparePreviousDatabases() {

		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");

		setDescription("Check that all species and database types in the previous release are represented in the current release.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	/**
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return True if the meta information is consistent within species.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		// look at all databases on the secondary server, check that we have an equivalent
		DatabaseRegistry secondaryDBR = DBUtils.getSecondaryDatabaseRegistry();

		// need a full registry of all primary databases
		DatabaseRegistry primaryDBR = new DatabaseRegistry(null, null, null, false);

		// get the map of species, with associated set of types, for both primary and secondary servers
		Map<Species, Set<DatabaseType>> primarySpeciesAndTypes = primaryDBR.getSpeciesTypeMap();
		Map<Species, Set<DatabaseType>> secondarySpeciesAndTypes = secondaryDBR.getSpeciesTypeMap();

		for (Species s : secondarySpeciesAndTypes.keySet()) {

			if (s.equals(Species.UNKNOWN) || s.equals(Species.ANCESTRAL_SEQUENCES)) {
				continue;
			}

			// fail at once if there are no databases on the main server for this species at all
			if (!primarySpeciesAndTypes.containsKey(s)) {

				ReportManager.problem(this, "",
						String.format("Secondary server contains at least one database for %s (e.g. %s) but there are none on the primary server", s, (secondaryDBR.getAll(s))[0].getName()));
				result = false;

			} else {

				// now check by type
				for (DatabaseType t : secondarySpeciesAndTypes.get(s)) {

					Set<DatabaseType> primaryTypes = primarySpeciesAndTypes.get(s);

					if (!primaryTypes.contains(t)) {

						ReportManager.problem(this, "", String.format("Secondary server has a %s database for %s but there is no equivalent on the primary server", t, s));
						result = false;

					}
				}

			}

		}

		return result;

	}

} // ComparePreviousDatabases
