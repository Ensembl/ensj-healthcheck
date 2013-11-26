/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that the seq region table is the same in all necessary databases.
 */
public class CoordSystemAcrossSpecies extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.CDNA, DatabaseType.OTHERFEATURES };

	/**
	 * Creates a new instance of CoordSystemAcrossSpecies
	 */
	public CoordSystemAcrossSpecies() {

		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that the coord_system table is the same across all generic DBs; if not it will cause problems on the website.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Make sure that the seq_region tables are all the same.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return True if the seq_region_attrib table is the same across all the species in the registry.
	 */
	public boolean run(DatabaseRegistry dbr) {

		return checkTableAcrossSpecies("coord_system", dbr, types, "All coord_system tables are the same", "coord_system tables are different", " WHERE name !='lrg'");

	} // run

} // CoordSystemAcrossSpecies

