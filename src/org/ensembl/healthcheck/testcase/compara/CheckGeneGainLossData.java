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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check compara Gene Gain/Loss tables.
 */

public class CheckGeneGainLossData extends SingleDatabaseTestCase {

	public CheckGeneGainLossData() {
		setDescription("Check that we have data coming from ncRNA and protein gain/loss trees");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql_main = "SELECT member_type, count(*)" + 
			" FROM gene_tree_root gtr JOIN CAFE_gene_family cgf ON(gtr.root_id=cgf.gene_tree_root_id)" +
			" WHERE gtr.tree_type = 'tree' GROUP BY gtr.member_type";

		int numRows = DBUtils.getRowCount(con, sql_main);
		if (numRows > 2) {
			ReportManager.problem(this, con, "FAILED Gene Gain/Loss Data test. Either ncRNA or protein trees don't have gene Gain/Loss trees.");
			ReportManager.problem(this, con, "FAILURE DETAILS: There are less than 2 member_types [protein/ncRNA] having gene Gain/Loss trees.");
			ReportManager.problem(this, con, "USEFUL SQL: " + sql_main);
			return false;

		}
		return true;
	}

}
