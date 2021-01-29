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

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that the attrib_type table is the same in all necessary databases.
 */
public class AttribTypeAcrossSpecies extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.OTHERFEATURES, DatabaseType.VEGA };

	/**
	 * Creates a new instance of AttribTypeTablesAcrossSpecies
	 */
	public AttribTypeAcrossSpecies() {

		setDescription("Check that the attrib_type table contains the same information for all databases with the same species.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	/**
	 * Make sure that the attrib_type tables are all the same.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return True if the attrib_type table is the same across all the species in the registry.
	 */
	public boolean run(DatabaseRegistry dbr) {

		return checkTableAcrossSpecies("attrib_type", dbr, types, "attrib_type tables all the same", "attrib_type tables are different", "");

	} // run

} // AttribTypeTablesAcrossSpecies
