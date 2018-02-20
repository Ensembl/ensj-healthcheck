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
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Compare the counts of the number of rows in each table in this and the previous database.
 */

public class ComparePreviousVersionTableRows extends ComparePreviousVersionBase {

	/**
	 * Create a new XrefTypes testcase.
	 */
	public ComparePreviousVersionTableRows() {

		setDescription("Compare the counts of the number of rows in each table in this and the previous database");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	// ----------------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		Map rowCounts = new HashMap();

		Connection con = dbre.getConnection();

		String[] tables = getTableNames(con);
		tables = Utils.removeStringFromArray(tables, "meta");
		// we need a different query for xref, and only compare those that are not projected
		tables = Utils.removeStringFromArray(tables, "external_synonym");
                tables = Utils.removeStringFromArray(tables, "ontology_xref");
                tables = Utils.removeStringFromArray(tables, "object_xref");
                tables = Utils.removeStringFromArray(tables, "xref");

		for (int i = 0; i < tables.length; i++) {

			rowCounts.put(tables[i], new Integer(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + tables[i])));

		}
		rowCounts.put("external_synonym", new Integer(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM external_synonym e, xref x WHERE e.xref_id = x.xref_id AND x.info_type <> 'PROJECTION'")));
                rowCounts.put("ontology_xref", new Integer(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM ontology_xref go, object_xref ox, xref x WHERE go.object_xref_id = ox.object_xref_id AND ox.xref_id = x.xref_id AND x.info_type <> 'PROJECTION'")));
                rowCounts.put("object_xref", new Integer(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM object_xref ox, xref x WHERE ox.xref_id = x.xref_id AND x.info_type <> 'PROJECTION'")));
                rowCounts.put("xref", new Integer(DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref x WHERE x.info_type <> 'PROJECTION'")));

		return rowCounts;

	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "rows in";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 0.75;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ------------------------------------------------------------------------

} // ComparePreviousVersionTableRows
