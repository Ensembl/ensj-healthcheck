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

import java.lang.Integer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that we dont't ship BlastZ
 * and LastZ alignments for the same species
 */

public class CheckPairAlignerUniqueMethod extends AbstractComparaTestCase {

	public CheckPairAlignerUniqueMethod() {
		setDescription("Check for duplicated LastZ/BlastZ alignments.");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		boolean result = true;

		/* Check if have both BLASTZ_NET and LASTZ_NET entries for the same species set */
		int numOfBLASTZ_LASTZSpeciesSets = DBUtils.getRowCount(con, "SELECT species_set_id, count(*) FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE TYPE in ('BLASTZ_NET', 'LASTZ_NET') GROUP BY species_set_id HAVING count(*) > 1");
		if (numOfBLASTZ_LASTZSpeciesSets > 0) {
			ReportManager.problem(this, con, "FAILED method_link_species_set table contains " + numOfBLASTZ_LASTZSpeciesSets + " entries with a BLASTZ_NET and LASTZ_NET entry for the same species_set");
			ReportManager.problem(this, con, "USEFUL SQL: SELECT species_set_id, count(*) FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE TYPE in ('BLASTZ_NET', 'LASTZ_NET') GROUP BY species_set_id HAVING count(*) > 1");
			result = false;
		}
		return result;

	}
} // CheckPairAlignerUniqueMethod
