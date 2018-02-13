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

import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;

/**
 * Compare the go xrefs in the current database with those from the equivalent database on the secondary server, the species
 * projected from in previous release should appear in current as well.
 */

public class ComparePreviousVersionGOXrefs extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionGOXrefs() {

		setDescription("Compare the GO xrefs in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
         * This only applies to core databases.
	 */
	public void types() {
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);
	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		String sql = "SELECT substring_index(substring_index(info_text,' ',2),' ',-1) AS species, count(*) FROM xref x WHERE dbprimary_acc like 'GO:%' and info_type not in ('UNMAPPED', 'DEPENDENT') GROUP BY species";

		return getCountsBySQL(dbre, sql);

	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "GO xrefs projected from";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.1;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ----------------------------------------------------------------------

} // ComparePreviousVersionGOXrefs
