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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks that the archive tables are up to date.
 */

public class Archive extends SingleDatabaseTestCase {

	/**
	 * Create a new Archive test case.
	 */
	public Archive() {

		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		setDescription("Checks the archive tables are up to date.");
		setHintLongRunning(true);
		setTeamResponsible(Team.CORE);
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		logger.fine("Checking deleted genes");
		result &= checkDeletedInGeneArchive(con, "gene", "G");
		logger.fine("Checking deleted transcripts");
		result &= checkDeletedInGeneArchive(con, "transcript", "T");
		logger.fine("Checking deleted translations");
		result &= checkDeletedInGeneArchive(con, "translation", "P");
		logger.fine("Checking changed translations");
		result &= checkChangedInGeneArchive(con, "translation", "P");
		logger.fine("Checking changed transcript");
		result &= checkChangedInGeneArchive(con, "transcript", "T");
		logger.fine("Checking changed genes");
		result &= checkChangedInGeneArchive(con, "gene", "G");

		return result;
	}

	/**
	 * Checks that all the changed _type_s are included in the gene_archive. A change has occured if the version is different.
	 * 
	 * @param con
	 *          connection on which to execute queries
	 * @param type
	 *          type of item deleted
	 * @param filter
	 *          substring to use use to filter relevant stableIDs, will be used in SQL as "%FILTER%".
	 * @return whether the test succeeded.
	 */
	private boolean checkChangedInGeneArchive(final Connection con, final String type, final String filter) {
		boolean result = true;

		String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) " + "FROM stable_id_event sie LEFT JOIN gene_archive ga " + "                     ON old_stable_id=" + type + "_stable_id "
				+ "WHERE sie.mapping_session_id=ga.mapping_session_id " + "     AND old_stable_id like \"%" + filter + "%\" "
				+ "     AND gene_stable_id is NULL " + "     AND new_stable_id=old_stable_id and old_version!=new_version;";

		String[] rows = DBUtils.getColumnValues(con, sql);
		if (rows.length > 0) {
			ReportManager.problem(this, con, rows.length + " deleted " + type + "s not in gene_archive ");
			result = false;
		}

		return result;

	}

	/**
	 * Checks that all the deleted _type_s are included in the gene_archive.
	 * 
	 * @param con
	 *          connection on which to execute queries
	 * @param type
	 *          type of item deleted
	 * @param filter
	 *          substring to use use to filter relevant stableIDs, will be used in SQL as "%FILTER%".
	 * @return whether the test succeeded.
	 */
	private boolean checkDeletedInGeneArchive(final Connection con, final String type, final String filter) {

		boolean result = true;

		String sql = "SELECT CONCAT(old_stable_id, \".\", old_version) " + "FROM stable_id_event sie LEFT JOIN gene_archive ga " + "                     ON old_stable_id=" + type + "_stable_id "
				+ "WHERE sie.mapping_session_id=ga.mapping_session_id " + "   AND old_stable_id like \"%" + filter + "%\""
				+ "   AND new_stable_id is NULL " + "   AND " + type + "_stable_id is NULL;";
		String[] rows = DBUtils.getColumnValues(con, sql);
		if (rows.length > 0) {
			ReportManager.problem(this, con, rows.length + " deleted " + type + "s not in gene_archive ");
			result = false;
		}

		return result;
	}

	// -----------------------------------------------------------------

}
