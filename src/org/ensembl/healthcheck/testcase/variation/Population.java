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
public class Population extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Population
	 */
	public Population() {
		addToGroup("variation-release");
		setDescription("Check the Populations are entered as expected");
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
      // Check for populations without sizes
      String size_stmt = "select count(*) from population pop, sample_population sp where pop.population_id = sp.population_id and pop.size is null;";

      int size_rows = DBUtils.getRowCount(con,size_stmt);
      if (size_rows > 0) {
        result = false;
        ReportManager.problem(this, con, String.valueOf(size_rows) + " populations have no stored size");
      }

      // Check for populations with freqs_from_gts set for mouse and human
      if (dbre.getSpecies() == Species.MUS_MUSCULUS || dbre.getSpecies() == Species.HOMO_SAPIENS) {

        String freq_stmt = "select count(*) from population pop where pop.freqs_from_gts = 1;";
        int freq_rows = DBUtils.getRowCount(con,freq_stmt);
        if (freq_rows == 0) {
          result = false;
          ReportManager.problem(this, con, "No populations have freqs_from_gts set ");
        }
      }
      // check dislay groups are set
      if (dbre.getSpecies() == Species.HOMO_SAPIENS) {
         String display_stmt = "select count(distinct display_group_id) from population ";
         int display_groups = DBUtils.getRowCount(con,display_stmt);
        if (display_groups != 3) {
          result = false;
          ReportManager.problem(this, con, "Only " + display_groups + " display groups set for current populations");
        }
      }


    } catch (Exception e) {
      ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
      result = false;
    }
    return result;
  } 
}
