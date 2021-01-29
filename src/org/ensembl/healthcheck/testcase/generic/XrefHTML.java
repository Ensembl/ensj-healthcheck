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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for xrefs that have HTML markup in the display_label.
 */
public class XrefHTML extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of XrefHTML
	 */
	public XrefHTML() {

		setDescription("Check that there are no xrefs with HTML markup");
		setPriority(Priority.AMBER);
		setEffect("Causes HTML markup to be displayed unrendered");
		setFix("Manually fix affected xrefs.");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry entry to be checked.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref WHERE display_label LIKE '%<%>%<%/>%'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " xrefs appear to have HTML markup (<...>) in the display_label");
			result = false;
		} else {
			ReportManager.correct(this, con, "No xrefs appear to have HTML markup in the display_label");
		}

		return result;

	}

} // XrefHTML

