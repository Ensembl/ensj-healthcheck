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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for HGNCs that have been assigned as display labels more than one gene.
 */

public class HGNCMultipleGenes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of HGNCMultipleGenes.
	 */
	public HGNCMultipleGenes() {

		setDescription("Check for HGNCs that have been assigned as display labels more than one gene.");
		setTeamResponsible(Team.CORE);
	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// this has to be done the slow way, don't think there's a way to do
		// this all at once
		String sql = "SELECT DISTINCT(x.display_label), COUNT(*) AS count FROM gene g, xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name LIKE 'HGNC%' AND x.xref_id=g.display_xref_id and x.display_label not like '%1 to many)' ";
                sql += "and g.seq_region_id NOT in (select seq_region_id FROM seq_region_attrib sa, attrib_type at WHERE at.attrib_type_id = sa.attrib_type_id AND code = 'non_ref') ";
  				sql += " GROUP BY x.display_label";
				sql += " HAVING COUNT > 1";

		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 500) {

			ReportManager.problem(this, con,
					"More than " + rows + " HGNC symbols have been assigned to more than one gene");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"Most HGNC symbols only assigned to one gene, " + rows + " have been assigned to more than one gene");
		}

		return result;

	}

	// ----------------------------------------------------------------------

}
