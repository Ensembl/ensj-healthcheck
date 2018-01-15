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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Compare the seq_region synonyms in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionRegionSynonyms extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionRegionSynonyms() {

		setDescription("Compare the seq_region synonyms in the current database with those from the equivalent database on the secondary server");
		setPriority(Priority.AMBER);
		setEffect("Indicates that the synonyms have changed; should not happen unless the assembly has changed");
		setTeamResponsible(Team.GENEBUILD);
		setSecondTeamResponsible(Team.RELEASE_COORDINATOR);

	}

	// ----------------------------------------------------------------------

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

                return getCountsBySQL(dbre, "SELECT DISTINCT(db_name), COUNT(*) FROM seq_region_synonym ss, external_db e WHERE ss.external_db_id = e.external_db_id GROUP BY db_name");

	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 1;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ------------------------------------------------------------------------

} // ComparePreviousVersionRegionSynonyms
