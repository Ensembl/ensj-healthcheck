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
import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for columns of type ENUM that have blank values - probably means there was a problem importing them.
 */

public class BlankEnums extends SingleDatabaseTestCase {

	/**
	 * Create a new BlankEnums testcase.
	 */
	public BlankEnums() {

		setDescription("Check for columns of type ENUM that have blank values - probably means there was a problem importing them.");
		setPriority(Priority.AMBER);
		setEffect("Will have blank values where NULL or one of the enum values is expected.");
		setFix("Re-import after identifying source of problem - possibly the word NULL in import files instead of \\N");
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

		String[] tables = DBUtils.getTableNames(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];

			List columnsAndTypes = DBUtils.getTableInfo(con, table, "enum");
			Iterator it = columnsAndTypes.iterator();
			while (it.hasNext()) {

				String[] columnAndType = (String[]) it.next();
				String column = columnAndType[0];

				int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=''");

				if (rows > 0) {

					ReportManager.problem(this, con, rows + " rows in " + table + "." + column + " are blank, should be NULL or one of the ENUM values");
					result = false;

				}

			}

		}

		return result;

	} // run

} // BlankEnums
