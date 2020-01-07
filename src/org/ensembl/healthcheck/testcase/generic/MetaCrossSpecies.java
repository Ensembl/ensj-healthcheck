/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
		setDescription("Check meta table species, classification and taxonomy_id is the same in all DBs for each species");
		setTeamResponsible(Team.GENEBUILD);
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
