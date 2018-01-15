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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.Species;

/**
 * Checks populations are entered as expected
 */
public class PopulationGenotype extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of PopulationGenotype
	 */
	public PopulationGenotype() {
	//	addToGroup("variation-release");
		setDescription("Check the two PopulationGenotype tables have the same populations");
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed
	 */
    public boolean run(final DatabaseRegistryEntry dbre) {
    boolean result = true;
    Connection con = dbre.getConnection();

    try {				
      // Check for novel populations
      String pop_stmt = "select count(*) from MTMP_population_genotype mpopg left outer join population_genotype popg on mpopg.population_id = popg.population_id where popg.variation_id is null";

      int size_rows = DBUtils.getRowCount(con,pop_stmt);
      if (size_rows > 0) {
        result = false;
        ReportManager.problem(this, con, String.valueOf(size_rows) + " lines in MTMP_population_genotype have populations not in population_genotype ");
      }

    } catch (Exception e) {
      ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
      result = false;
    }
    return result;
  } 
}
