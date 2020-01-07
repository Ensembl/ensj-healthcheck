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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all xrefs of particular types have the correct prefix for dbprimary_acc and/or display_label.
 */

public class XrefPrefixes extends SingleDatabaseTestCase {

	/**
	 * Create a new XrefPrefixes testcase.
	 */
	public XrefPrefixes() {

		setDescription("Check that all xrefs of particular types have the correct prefix for dbprimary_acc and/or display_label.");
		setPriority(Priority.AMBER);
		setEffect("Web display of xrefs will be broken");
		setFix("Re-run xref system or manually fix affected xrefs.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
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

		// --------------------------------
		// MGI - dbprimary_acc should have MGI: prefix
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='MGI' AND x.dbprimary_acc NOT LIKE 'MGI:%'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " MGI xrefs do not have MGI: prefixes in the dbprimary_acc column");
			result = false;
		} else {
			ReportManager.correct(this, con, "All MGI xrefs have the correct prefix");
		}

		// --------------------------------
		// GO - dbprimary_acc and display_label should have GO: prefix
		rows = DBUtils.getRowCount(con,
				"SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='GO' AND (x.dbprimary_acc NOT LIKE 'GO:%' OR x.display_label NOT LIKE 'GO:%')");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " GO xrefs do not have GO: prefixes in the dbprimary_acc and/or display_label columns");
			result = false;
		} else {
			ReportManager.correct(this, con, "All GO xrefs have the correct prefix");
		}

		// --------------------------------
		// ZFIN - dbprimary_acc should begin with ZDB
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='ZFIN_ID' AND x.dbprimary_acc NOT LIKE 'ZDB%'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " ZFIN xrefs do not have ZDB: prefixes in the dbprimary_acc and/or display_label columns");
			result = false;
		} else {
			ReportManager.correct(this, con, "All ZFIN xrefs have the correct prefix");
		}

		return result;

	} // run

} // XrefPrefixes
