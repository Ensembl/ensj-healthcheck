/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
 * Check if we have chromosomes but not mitochondrion
 */
public class AssemblyHasMt extends SingleDatabaseTestCase {

	/**
	 * Constructor for karyotype test case
	 */
	public AssemblyHasMt() {

		setDescription("Check that karyotype and seq_region tables agree");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core databases.
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
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		boolean result = true;
		String sqlCS = "SELECT count(*) FROM coord_system " + "WHERE name = 'chromosome'";

		String sqlMT = "SELECT count(*) " + "FROM seq_region_attrib sa, attrib_type at, seq_region s "
				+ "WHERE s.seq_region_id = sa.seq_region_id " + "AND at.attrib_type_id = sa.attrib_type_id "
				+ "AND code = 'karyotype_rank' " + "AND s.name IN ('MT', 'Mito', 'dmel_mitochondrion_genome', 'MtDNA')";
		int karyotype = t.queryForDefaultObject(sqlCS, Integer.class);
		if (karyotype > 0) {
			int mt = t.queryForDefaultObject(sqlMT, Integer.class);
			if (mt == 0) {
				result = false;
				ReportManager.problem(this, con,
						"Species has chromosomes but neither MT nor Mito " + "nor dmel_mitochondrion_genome nor MtDNA");
			}
		}
		return result;
	}

} // Karyotype
