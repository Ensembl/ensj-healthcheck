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

import java.sql.Connection;
import org.ensembl.healthcheck.util.DBUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that there are no blank db_display_name fields in external_db.
 */

public class ExternalDBDisplayName extends SingleDatabaseTestCase {

	/**
	 * Create a new PredictedXrefs testcase.
	 */
	public ExternalDBDisplayName() {

		setDescription("Check that there are no blank db_display_name fields in external_db");
		setTeamResponsible(Team.GENEBUILD);
	}
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
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

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM external_db WHERE db_display_name IS NULL OR db_display_name LIKE ' %'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " rows in external_db have null or blank db_display_name - this will mean their label is missing on the web page");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank db_display_name fields in external_db");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // ExternalDBDisplayName
