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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that there are no duplicates in the assembly table.
 */

public class DuplicateAssembly extends SingleDatabaseTestCase {

	/**
	 * Create a new DuplicateAssembly testcase.
	 */
	public DuplicateAssembly() {

		setDescription("Check that there are no duplicates in the assembly table");
		setTeamResponsible(Team.GENEBUILD);

	}

        /**
         * Data is only tested in core database, as the tables are in sync
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
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = DBUtils.getRowCount(con, "SELECT *, COUNT(*) AS c FROM assembly GROUP BY asm_seq_region_id, cmp_seq_region_id, asm_start, asm_end, cmp_start, cmp_end, ori HAVING c > 1");

		if (rows > 0) {

			ReportManager.problem(this, con, "At least " + rows + " duplicate rows in assembly table");
			result = false;

		} else {

			ReportManager.correct(this, con, "No duplicate rows in the assembly table");

		}

		return result;

	} // run

	/**
	 * Note more details can be obtained via:
	 * 
	 * SELECT a.*, sr1.name, cs1.name, sr2.name, cs2.name, COUNT(*) AS c FROM assembly a, seq_region sr1, seq_region sr2, coord_system
	 * cs1, coord_system cs2 WHERE a.cmp_seq_region_id = sr1.seq_region_id AND sr1.coord_system_id = cs1.coord_system_id AND
	 * a.asm_seq_region_id = sr2.seq_region_id AND sr2.coord_system_id = cs2.coord_system_id GROUP BY asm_seq_region_id,
	 * cmp_seq_region_id, asm_start, asm_end, cmp_start, cmp_end, ori HAVING c > 1;
	 */

} // DuplicateAssembly
