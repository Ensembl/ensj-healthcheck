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


package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;

/**
 * Check that the seq region table is the same in all necessary databases.
 */
public class CoordSystemAcrossSpecies extends MultiDatabaseTestCase {

	private DatabaseType[] types = { DatabaseType.CORE, DatabaseType.CDNA, DatabaseType.OTHERFEATURES, DatabaseType.RNASEQ };

	/**
	 * Creates a new instance of CoordSystemAcrossSpecies
	 */
	public CoordSystemAcrossSpecies() {

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

		boolean result = checkSQLAcrossSpecies("SELECT * FROM coord_system WHERE name !='lrg' ", dbr, types, false);

                if (!result) {
                        ReportManager.problem(this, "", "coord_system information not the same for some databases");
                } else {
                        ReportManager.correct(this, "", "coord_system information is the same for all databases for all species");
                }
                return result;

	} // run

} // CoordSystemAcrossSpecies

