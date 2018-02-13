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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks to ensure all genes, transcripts, translations and exons have unique
 * stable IDs
 * 
 * <p>
 * Group is <b>check_stable_ids </b>
 * </p>
 * 
 * <p>
 * To be run after the stable ids have been assigned.
 * </p>
 */
public class StableID extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of StableID.
	 */
	public StableID() {

		setDescription("Checks stable_id data is valid.");
		setPriority(Priority.RED);
		setEffect("Compara will have invalid stable IDs.");
		setFix("Re-run stable ID mapping or fix manually.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
	}

	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
    removeAppliesToType(DatabaseType.OTHERFEATURES);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= checkStableIDs(con, "exon");
		result &= checkStableIDs(con, "translation");
		result &= checkStableIDs(con, "transcript");
		result &= checkStableIDs(con, "gene");

		return result;
	}

	/**
	 * Checks that the typeName_stable_id table is valid. The table is valid if
	 * it has >0 rows, and there are no orphan references between typeName table
	 * and typeName_stable_id. Also prints some example data from the
	 * typeName_stable_id table via ReportManager.info().
	 * 
	 * @param con
	 *            connection to run queries on.
	 * @param typeName
	 *            name of the type to check, e.g. "exon"
	 * @return true if the table and references are valid, otherwise false.
	 */
	public boolean checkStableIDs(Connection con, String typeName) {

		boolean result = true;

		String stableIDtable = typeName;
		int nullStableIDs = DBUtils.getRowCount(con, "SELECT COUNT(1) FROM "
				+ stableIDtable + " WHERE stable_id IS NULL");

		if (nullStableIDs > 0) {
			ReportManager.problem(this, con, stableIDtable
					+ " table has NULL stable_ids");
			result = false;
		}

		// check for duplicate stable IDs
		// to find which records are duplicated use
		// SELECT exon_id, stable_id, COUNT(*) FROM exon_stable_id GROUP BY
		// stable_id HAVING COUNT(*) > 1;
		// this will give the internal IDs for *one* of each of the duplicates
		// if there are only a few then reassign the stable IDs of one of the
		// duplicates
		int duplicates = DBUtils.getRowCount(con,
				"SELECT COUNT(stable_id)-COUNT(DISTINCT stable_id) FROM "
						+ stableIDtable + " WHERE stable_id not like 'LRG%'");
		if (duplicates > 0) {
			ReportManager.problem(this, con, stableIDtable + " has "
					+ duplicates
					+ " duplicate stable IDs (versions not checked)");
			result = false;
		}

		return result;
	}

}
