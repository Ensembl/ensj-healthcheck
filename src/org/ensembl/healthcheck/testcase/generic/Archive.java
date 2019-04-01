/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
                removeAppliesToType(DatabaseType.CDNA);

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
