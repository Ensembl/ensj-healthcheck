/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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
 * Check for newlines & tabs in gene descriptions - causes problems for TSV dumping.
 */
public class DescriptionNewlines extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of DescriptionNewlines.
	 */
	public DescriptionNewlines() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("post-compara-handover");
		
		setDescription("Check for newlines and tabs in gene descriptions.");

		setPriority(Priority.AMBER);
		setEffect("Will cause problems for TSV file dumping and importing");
		setFix("Remove newlines and tabs; useful SQL for identifying affected genes:\nSELECT gene_id, stable_id, description FROM gene WHERE (LOCATE('\\n', description) > 0 or LOCATE('\\t', description) > 0);");
		setTeamResponsible(Team.CORE);

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

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM gene WHERE (LOCATE('\n', description) > 0 OR LOCATE('\t', description) > 0)");

		if (rows > 0) {

			result = false;
			ReportManager.problem(this, con, rows + " genes have newlines and/or tabs in their descriptions");

		} else {

			ReportManager.correct(this, con, "No genes have newlines or tabs in their descriptions");

		}
		return result;

	} // run

	// -------------------------------------------------------------------------

} // DescriptionNewlines
