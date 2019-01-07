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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for copied data in several tables.
 */

public class Denormalized extends SingleDatabaseTestCase {

  /**
   * Create an test that applies to a specific set of tables and databases.
   */
  public Denormalized() {

    setDescription("Check for broken denormalization.");
    setTeamResponsible(Team.VARIATION);

  }

  /**
   * Look for broken denormalization relationships.
   * 
   * @param dbre 
            The database to use.
   * @return true Ff all foreign key relationships are valid.
   */
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;

    Connection con = dbre.getConnection();
    try {
      // somatic
      if (dbre.getSpecies() == Species.HOMO_SAPIENS) {
        // Queries switched off because it was taking too long to run. Need to be run manually.
        //result &= checkForBadDenormalization(con, "variation", "variation_id", "somatic", "variation_feature", "variation_id", "somatic");
        //result &= checkForBadDenormalization(con, "variation_feature", "variation_feature_id", "somatic", "transcript_variation", "variation_feature_id", "somatic");
        result &= checkForBadDenormalization(con, "structural_variation", "structural_variation_id", "somatic", "structural_variation_feature", "structural_variation_id", "somatic");
      }
      // display
      // Queries switched off because it was taking too long to run. Need to be run manually.
      //result &= checkForBadDenormalization(con, "variation", "variation_id", "display", "variation_feature", "variation_id", "display");
      //result &= checkForBadDenormalization(con, "variation_feature", "variation_feature_id", "display", "transcript_variation", "variation_feature_id", "display");
    } catch (Exception e) {
      ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
      result = false;
    }
    if (result) {
      // if there were no problems, just inform for the interface to pick the HC
      ReportManager.correct(this, con, "Denormalized columns test passed without any problem");
    }

    return result;
  }
  
  /**
   * Verify denormalization relations, and fills ReportManager with useful sql if
   * necessary.
   * 
   * @param con
   *            A connection to the database to be tested. Should already be
   *            open.
   * @param table1
   *            With col1, specifies the first key to check.
   * @param col1
   *            First key to check.
   * @param col1d
   *            Column in table1 to check.
   * @param table2
   *            With col2, specifies the second key to check.
   * @param col2
   *            Second key to check.
   * @param col2d
   *            Column in table2 to check.
   * @return boolean true if everything is fine false otherwise
   */
  public boolean checkForBadDenormalization(Connection con, String table1, String col1, String col1d, String table2, String col2, String col2d) {
  
    boolean result = true;

    String sql = table1 + ", " + table2 + 
        " WHERE " + table1 + "." + col1 + " = " + table2 + "." + col2 + " AND " +
         table1 + "." + col1d + " != " + table2 + "." + col2d;
  
    String useful_sql = "SELECT " + table1 + "." + col1 + ", " + table1 + "." + col1d + ", " +
                                    table2 + "." + col2 + ", " + table2 + "." + col2d + " FROM " + sql;
  
    int count = DBUtils.getRowCount(con, "SELECT count(*) FROM " + sql);
    
    if (count != 0) {
      ReportManager.problem(this, con, "FAILED " + table1 + " -> "
          + table2 + " on the denormalization of " + col1d + " using the FK " + col1);
      ReportManager.problem(this, con, "FAILURE DETAILS: " + count
          + " " + col1d + " entries are different in " + table1 + " and " + table2);
      ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
      result = false;
    } else if (count < 0) {
      ReportManager.problem(this, con, "TEST NOT COMPLETED " + table1
          + " -> " + table2 + " using FK " + col1
          + ", look at the StackTrace if any");
      result = false;
    }

    return result;
  }
}

