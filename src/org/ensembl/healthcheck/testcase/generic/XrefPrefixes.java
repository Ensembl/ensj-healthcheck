/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all xrefs of particular types have the correct prefix for dbprimary_acc and/or display_label.
 */

public class XrefPrefixes extends SingleDatabaseTestCase {

	/**
	 * Create a new XrefPrefixes testcase.
	 */
	public XrefPrefixes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
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
		int rows = getRowCount(con, "SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='MGI' AND x.dbprimary_acc NOT LIKE 'MGI:%'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " MGI xrefs do not have MGI: prefixes in the dbprimary_acc column");
			result = false;
		} else {
			ReportManager.correct(this, con, "All MGI xrefs have the correct prefix");
		}

		// --------------------------------
		// GO - dbprimary_acc and display_label should have GO: prefix
		rows = getRowCount(con,
				"SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='GO' AND (x.dbprimary_acc NOT LIKE 'GO:%' OR x.display_label NOT LIKE 'GO:%')");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " GO xrefs do not have GO: prefixes in the dbprimary_acc and/or display_label columns");
			result = false;
		} else {
			ReportManager.correct(this, con, "All GO xrefs have the correct prefix");
		}

		// --------------------------------
		// ZFIN - dbprimary_acc should begin with ZDB
		rows = getRowCount(con, "SELECT COUNT(*) FROM external_db e, xref x WHERE x.external_db_id=e.external_db_id AND e.db_name='ZFIN_ID' AND x.dbprimary_acc NOT LIKE 'ZDB%'");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " GO xrefs do not have GO: prefixes in the dbprimary_acc and/or display_label columns");
			result = false;
		} else {
			ReportManager.correct(this, con, "All GO xrefs have the correct prefix");
		}

		return result;

	} // run

} // XrefPrefixes
