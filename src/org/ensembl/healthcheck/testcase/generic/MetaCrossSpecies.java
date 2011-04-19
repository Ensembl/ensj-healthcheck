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

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check meta table species, classification and taxonomy_id is the same in all DBs for each species
 */

public class MetaCrossSpecies extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.CDNA, DatabaseType.OTHERFEATURES, DatabaseType.VEGA, DatabaseType.RNASEQ };

	/**
	 * Create a new instance of MetaCrossSpecies
	 */
	public MetaCrossSpecies() {
		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check meta table species, classification and taxonomy_id is the same in all DBs for each species");
		setTeamResponsible(Team.RELEASE_COORDINATOR);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Check various aspects of the meta table.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return True if the meta information is consistent within species.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = checkSQLAcrossSpecies("SELECT LCASE(meta_value) FROM meta WHERE meta_key ='species.classification' ORDER BY meta_id", dbr, types, false);
		if (!result) {
			ReportManager.problem(this, "", "meta information not the same for some databases");
		} else {
			ReportManager.correct(this, "", "meta information is the same for all databases for all species");
		}

		return result;

	}

} // MetaCrossSpecies
