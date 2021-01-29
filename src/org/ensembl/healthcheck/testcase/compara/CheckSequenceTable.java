/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that checks the "sequence" table
 */

public class CheckSequenceTable extends SingleDatabaseTestCase {

	public CheckSequenceTable() {
		setDescription("Check for the sequence table of an ensembl_compara database.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();

		if (!tableHasRows(con, "sequence")) {
			ReportManager.correct(this, con, "NO ENTRIES in sequence table, so nothing to test IGNORED");
			return true;
		}

		boolean result = true;
		result &= checkCountIsZero(con, "sequence", "sequence='' or sequence is NULL");
		result &= checkCountIsZero(con, "sequence", "length='' or length=0 or length is NULL");
		result &= checkCountIsZero(con, "sequence", "length!=length(sequence)");
		return result;
	}

} // CheckSequenceTable
