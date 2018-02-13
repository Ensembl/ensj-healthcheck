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
 * Check that the number of entries in the seq_region_attrib table specifying
 * that the MT chromosome should use codon table 2 matches the number of MT
 * chromosomes (may be several different assemblies)
 */
public class MTCodonTable extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of MTCodonTable
	 */
	public MTCodonTable() {

		setDescription("Check that the number of entries in the seq_region_attrib table specifying that the MT chromosome should use codon table 2 matches the number of MT chromosomes (may be several different assemblies)");
		setPriority(Priority.AMBER);
		setFix("Add seq_region_attribs");
		setTeamResponsible(Team.GENEBUILD);

	}

        /**
         * Data is only tested in core database, as the tables are in sync
         */
        public void types() {

                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

        }

	/**
	 * Run the test.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int numMTs = DBUtils
				.getRowCount(con,
						"SELECT COUNT(DISTINCT(seq_region_id)) FROM seq_region WHERE name = 'MT'");

		int numAttribs = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(DISTINCT(sra.seq_region_id)) FROM seq_region sr, seq_region_attrib sra, attrib_type att WHERE sr.seq_region_id=sra.seq_region_id AND sra.attrib_type_id=att.attrib_type_id AND att.code = 'codon_table' AND sra.value = '2' AND sr.name = 'MT'");

		if (numMTs != numAttribs) {

			ReportManager
					.problem(
							this,
							con,
							"There are "
									+ numMTs
									+ " seq_region entries named 'MT' but "
									+ numAttribs
									+ " seq_region_attribs specifying that they should use codon table 2");
			result = false;
		}

		return result;

	} // run

	// -----------------------------------------------------------------------

} // MTCodonTable

