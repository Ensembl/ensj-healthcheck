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

/*
 * Created on 09-Mar-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check if we have chromosomes but not karyotype_rank entries
 */
public class KaryotypeRankAttrib extends SingleDatabaseTestCase {

	/**
	 * Constructor for karyotype test case
	 */
	public KaryotypeRankAttrib() {

		setDescription("Check that karyotype and seq_region tables agree");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

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

		Connection con = dbre.getConnection();
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		boolean result = true;
		String sqlCS = "SELECT count(*) FROM coord_system " + "WHERE name = 'chromosome'";
		String sqlAttrib = "SELECT count(*) " + "FROM seq_region_attrib sa, attrib_type at "
				+ "WHERE at.attrib_type_id = sa.attrib_type_id " + "AND code = 'karyotype_rank'";
		int numChromCS = t.queryForDefaultObject(sqlCS, Integer.class);
		if (numChromCS > 0) {
			int numRankAttribs = t.queryForDefaultObject(sqlAttrib, Integer.class);
			if (numRankAttribs < 2) {
				result = false;
				ReportManager.problem(this, con, "Chromosome entry exists but no karyotype attrib is present");
			}
		}
		return result;
	}

} // Karyotype
