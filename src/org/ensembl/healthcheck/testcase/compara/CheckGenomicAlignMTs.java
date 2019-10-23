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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that no MT is
 * left excluded from genomic-align blocks
 */

public class CheckGenomicAlignMTs extends SingleDatabaseTestCase {

	/**
	 * Create an CheckGenomicAlignMTs that applies to a specific set of
	 * databases.
	 */
	public CheckGenomicAlignMTs () {
		setDescription("Check the genome_dbs for a method_link_species_set are present in the genomic_aligns");
		setTeamResponsible(Team.COMPARA);
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

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= checkAlignmentsOnMT(con);

		return result;

	}

	public boolean checkAlignmentsOnMT(Connection comparaCon) {

		boolean result = true;

		String sql1 = "SELECT method_link_species_set.name, genome_db.name, method_link_species_set_id, dnafrag_id"
				+ " FROM method_link_species_set LEFT JOIN method_link USING (method_link_id)"
				+ " LEFT JOIN species_set USING (species_set_id)"
				+ " LEFT JOIN genome_db USING (genome_db_id)"
				+ " LEFT JOIN dnafrag ON (genome_db.genome_db_id = dnafrag.genome_db_id AND dnafrag.cellular_component = 'MT')"
				+ " WHERE (type NOT LIKE 'CACTUS_HAL%') AND (class LIKE 'GenomicAlignTree%' OR class LIKE 'GenomicAlign%multiple%') AND dnafrag.cellular_component = 'MT'";
		try {
			Statement stmt1 = comparaCon.createStatement();
			ResultSet rs1 = stmt1.executeQuery(sql1);
			while (rs1.next()) {
				String sql2 = "SELECT count(*) FROM genomic_align WHERE method_link_species_set_id = "
						+ rs1.getInt(3) + " AND dnafrag_id = " + rs1.getInt(4);
				Statement stmt2 = comparaCon.createStatement();
				ResultSet rs2 = stmt2.executeQuery(sql2);
				while (rs2.next()) {
					if (rs2.getInt(1) == 0) {
						result = false;
						ReportManager.problem(
								this,
								comparaCon,
								"The MT chromosome from " + rs1.getString(2)
										+ " is not present in the "
										+ rs1.getString(1) + " alignments");
					}
				}
			}
			rs1.close();
			stmt1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

} // CheckGenomicAlignMTs
