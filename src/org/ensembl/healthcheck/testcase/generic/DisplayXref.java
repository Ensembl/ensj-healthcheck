/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that some display_xrefs are set. Also check for numeric EntrezGenes being used as display xrefs.
 */

public class DisplayXref extends SingleDatabaseTestCase {

	/**
	 * Create a new DisplayXref testcase.
	 */
	public DisplayXref() {

		setDescription("Check that display_xrefs are set OK");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that some (not necessarily all) genes and transcripts have valid
		// display_xref_ids
		// TODO - is checking that there is at least 1 OK or should there be a
		// minimum of say 100?
		String[] types = { "gene", "transcript" };
		for (int i = 0; i < types.length; i++) {

			int total = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + types[i]);

			if (DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + types[i] + " WHERE display_xref_id IS NOT NULL AND display_xref_id > 0") == 0) {

				ReportManager.problem(this, con, "No " + types[i] + "s with valid display_xref_id");
				result = false;

			}

			// no display_xref_id should be 0
			int zeroDX = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + types[i] + " WHERE display_xref_id = 0");
			if (zeroDX > 0) {

				ReportManager.problem(this, con, zeroDX + " " + types[i] + " display_xrefs are set to 0 - should be set to NULL");
				result = false;

			}

			// check for display_xref_ids that don't point to xrefs
			// can't use countOrphans() here as we need to rule out cases where both
			// are null

			int orphans = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + types[i] + " LEFT JOIN xref ON " + types[i] + ".display_xref_id=xref.xref_id WHERE " + types[i]
					+ ".display_xref_id IS NOT NULL AND xref.xref_id IS NULL");

			if (orphans > 0) {

				ReportManager.problem(this, con, orphans + " " + types[i] + "s (out of a total of " + total + ") have display_xref_ids pointing to non-existent xrefs.");
				result = false;

			}

		}

		// check for numeric EntrezGenes being used as display_xrefs
		int numeric = DBUtils.getRowCount(con,
				"SELECT COUNT(*) FROM gene g, xref x, external_db e WHERE e.external_db_id=x.external_db_id AND g.display_xref_id=x.xref_id AND e.db_name='EntrezGene' AND x.display_label REGEXP '^[0-9]+$'");

		if (numeric > 0) {

			ReportManager.problem(this, con, numeric + " genes have display_xrefs that are from EntrezGene and are numeric");
			result = false;

		}

		if (result) {

			ReportManager.correct(this, con, "All display_xrefs OK");

		}

		return result;

	} // run

} // DisplayXref
