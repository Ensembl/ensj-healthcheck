/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * An EnsEMBL Healthcheck test case that looks for entries in the GenomeDB table with the same name and assembly_default set to true for more than one.
 */


public class DuplicateGenomeDb extends SingleDatabaseTestCase {

	public DuplicateGenomeDb() {

		addToGroup("compara_homology"); 
		setDescription("Searches for species where assembly_default has been set to true more than once for the same name. This seems to happen when the contents of the GenomeDB table is copied from the master to the pan compara database. ");
		setTeamResponsible(Team.ENSEMBL_GENOMES);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 *  
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql = "SELECT "
			+ "	genome_db_id, "
			+ "	a.name, "
			+ "	assembly, "
			+ "	assembly_default "
			+ "FROM ( "
			+ "	SELECT NAME, count(*) AS cnt "
			+ "	FROM genome_db "
			+ "	WHERE assembly_default = 1 "
			+ "	GROUP BY NAME "
			+ "	HAVING cnt > 1 "
			+ ") a JOIN genome_db g ON (g.name = a.name) ORDER BY NAME; ";


		String[] genomeDbIds = DBUtils.getColumnValues(con, sql);

		if (genomeDbIds.length > 0) {
			result = false;
			ReportManager.problem(this, con,
					"Genome dbs were found with the same name with assembly_default set to true:");

			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);

				while (rs.next()) {
					ReportManager.problem(this, con, 
							"  genome_db_id: "      + rs.getString("genome_db_id")
							+ " a.name: "           + rs.getString("a.name")
							+ " assembly: "         + rs.getString("assembly")
							+ " assembly_default: " + rs.getString("assembly_default")
							);
				}

				rs.close();
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			ReportManager.correct(this, con, "PASSED test! :-D");
		}

		return result;
	}

} // DuplicateGenomeDb
