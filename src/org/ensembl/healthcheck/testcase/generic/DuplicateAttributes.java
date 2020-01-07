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
 * Check for duplicated rows in various *_attrib tables.
 */
public class DuplicateAttributes extends SingleDatabaseTestCase {

	double THRESHOLD = 0.0; // fraction of non-unique rows must be greater than this for a warning to occur

	String[] attribs = { "gene", "transcript", "translation", "seq_region", "misc" };

	/**
	 * Creates a new instance of DuplicateAttributes
	 */
	public DuplicateAttributes() {

		setDescription("Check for duplicated rows in various *_attrib tables.");
		setPriority(Priority.AMBER);
		setEffect("Many duplicates can cause serious performance problems.");
		setFix("Remove duplicated rows if appropriate.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		for (String attrib : attribs) {

			String table = attrib + "_attrib";
			String column = attrib.equals("misc") ? "misc_feature_id" : attrib + "_id";

			logger.finest("Checking " + table);

			int totalRows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table);

			if (totalRows == 0) { // avoid division by zero
				continue;
			}

			int uniqueRows = DBUtils.getRowCount(con, "SELECT COUNT(DISTINCT " + column + ", attrib_type_id, value) FROM " + table);

			int duplicates = totalRows - uniqueRows;

			if ((double) duplicates / (double) totalRows > THRESHOLD) {

				ReportManager.problem(this, con, table + " has " + totalRows + " rows in total but only " + uniqueRows + " are unique");
				result = false;

			} else {

				ReportManager.correct(this, con, "No duplicated rows in " + table);

			}
		}

		return result;

	} // run

} // DuplicateAttributes

