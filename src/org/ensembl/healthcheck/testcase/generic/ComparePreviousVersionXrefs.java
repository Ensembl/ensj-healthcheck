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
 * Compare the xrefs in the current database with those from the equivalent database on the secondary server.
 */

public class ComparePreviousVersionXrefs extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionXrefs() {

		setDescription("Compare the xrefs in the current database with those from the equivalent database on the secondary server");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test Does not apply to sangervega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {

		String sql = "SELECT DISTINCT(e.db_name) AS db_name, COUNT(*) AS count" + " FROM external_db e, xref x, object_xref ox" + " WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND e.db_name not like 'GO' "
				+ getExcludeProjectedSQL(dbre) + " GROUP BY e.db_name";
		// System.out.println(sql);
		return getCountsBySQL(dbre, sql);

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "xrefs of type";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.70;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 1000;

        }

	// ----------------------------------------------------------------------

	private String getExcludeProjectedSQL(DatabaseRegistryEntry dbre) {

		String sql = " AND (x.info_type != 'PROJECTION' OR x.info_type IS NULL)";

		return sql;

	}

	// ----------------------------------------------------------------------

} // ComparePreviousVersionXrefs

