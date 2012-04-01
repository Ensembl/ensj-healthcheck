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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for Uniprot xrefs that have "Unreviewed" as the primary DB accession.
 */

public class UnreviewedXrefs extends SingleDatabaseTestCase {

	/**
	 * Create a new UnreviewedXrefs testcase.
	 */
	public UnreviewedXrefs() {

		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");
		
		setDescription("Check for Uniprot xrefs that have 'Unreviewed' as the primary DB accession.");
		setPriority(Priority.AMBER);
		setEffect("Affected xrefs will have broken hyperlinks, also problems for downstream pipelines.");
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
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name LIKE 'UniProt%' AND x.dbprimary_acc='Unreviewed';");

		if (rows > 0) {
			ReportManager.problem(this, con, rows + " UniProt xrefs have \'Unreviewed\' as the primary accession.");
			result = false;
		} else {
			ReportManager.correct(this, con, "No Uniprot xrefs have \'Unreviewed\' as the accession.");
		}

		return result;

	} // run

} // UnreviewedXrefs
