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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.MapRowMapper;

/**
 * Check that features exist for expected analysis types, and that all analysis
 * types have features.
 */
public class FeatureAnalysis extends SingleDatabaseTestCase {

  // the following tables have an analysis_id column but are NOT really feature tables
  String[] featureTables = getCoreTablesWithAnalysisID();

  private String[] proteinFeatureAnalyses = { "prints", "pfscan", "signalp",
      "seg", "ncoils", "pfam", "tmhmm" };

  /**
   * Creates a new instance of FeatureAnalysis
   */
  public FeatureAnalysis() {

    setHintLongRunning(true);
    setDescription("Check that features exist for the expected analyses.");
    setTeamResponsible(Team.GENEBUILD);
  }

  /**
   * This only applies to core databases.
   */
  public void types() {
    removeAppliesToType(DatabaseType.CDNA);
  }

  /**
   * Run the test.
   * 
   * @param dbre
   *          The database to use.
   * @return true if the test passed.
   * 
   */
  public boolean run(DatabaseRegistryEntry dbre) {

    boolean result = true;

    // --------------------------------
    // First check that all analyses have some features
    result &= checkAnalysesHaveFeatures(dbre, featureTables);

    // --------------------------------
    // Check protein_feature
    result &= checkFeatureAnalyses(dbre, "protein_feature", proteinFeatureAnalyses);

    // -------------------------------
    // TODO - other feature tables

    // -------------------------------

    return result;

  } // run

  // ---------------------------------------------------------------------

  /**
   * Check that all the analyses in the analysis table have some features
   * associated.
   */
  private boolean checkAnalysesHaveFeatures(DatabaseRegistryEntry dbre,
      String[] featureTables) {

    boolean result = true;

    Connection con = dbre.getConnection();

    try {

      Map<Integer, String> analysesFromFeatureTables = new HashMap<Integer, String>();

      Map<Integer, String> analysesFromAnalysisTable = getLogicNamesFromAnalysisTable(con);

      // build cumulative list of analyses from feature tables
      for (String featureTable: featureTables) {
        logger.fine("Collecting analysis IDs from " + featureTable);
        String sql = String.format("SELECT DISTINCT(analysis_id), COUNT(*) AS count FROM %s GROUP BY analysis_id", featureTable);
        MapRowMapper<Integer, Integer> mapper = new DefaultMapRowMapper<Integer, Integer>(Integer.class, Integer.class);
        Map<Integer, Integer> tableAnalysis = getSqlTemplate(dbre).queryForMap(sql, mapper);
        
        for(Map.Entry<Integer, Integer> entry: tableAnalysis.entrySet()) {
          Integer id = entry.getKey();
          Integer count = entry.getValue();
          String priorTable = analysesFromFeatureTables.get(id);
          
          if(priorTable != null) {
            //Gene & transcript are allowed to share analysis id
            if("transcript".equals(featureTable) && "gene".equals(priorTable)) {
                // OK
            }
            //Unmapped object, object_xref & marker_feature are allowed to share
            else if ("unmapped_object".equals(featureTable) && ("object_xref".equals(priorTable) || "marker_feature".equals(priorTable))) {
                // OK
            }
            //Operon & operon transcript are allowed to share
            else if ("operon".equals(featureTable) && "operon_transcript".equals(priorTable)) {
                // OK
            }
            else {
              String msg = String.format("Analysis with ID %d is used in %s as well as %s", id, featureTable, priorTable);
              ReportManager.problem(this, con, msg);
              result = false;
            }
          }
          else {
            analysesFromFeatureTables.put(id, featureTable);
          }
          
          // check that each analysis actually exists in the analysis
          // table
          if(! analysesFromAnalysisTable.containsKey(id) && ! "object_xref".equals(featureTable)) {
            String msg = String.format("Analysis ID %d is used in %d rows in %s but is not present in the analysis table", id, count, featureTable);
            ReportManager.problem(this, con, msg);
            result = false;
          }
        }
      }

      // look at each analysis ID *from the analysis table* to see if it's
      // used
      // somewhere

      Statement stmt = con.createStatement();
      ResultSet rs = stmt
          .executeQuery("SELECT analysis_id, logic_name FROM analysis");
      while (rs.next()) {
        int analysisID = rs.getInt("analysis_id");
        String logicName = rs.getString("logic_name");
        if (!analysesFromFeatureTables.containsKey(analysisID)) {
          ReportManager.problem(this, con, "Analysis with ID " + analysisID
              + ", logic name " + logicName
              + " is not used in any feature table");
          result = false;
        }
      }
      rs.close();
      stmt.close();

    }
    catch (SQLException se) {
      se.printStackTrace();
    }

    return result;

  }

  // -----------------------------------------------------------------

  private boolean checkFeatureAnalyses(DatabaseRegistryEntry dbre,
      String table, String[] analyses) {
    boolean result = true;

    logger.fine("Checking analyses for " + table + " in " + dbre.getName());
    int tableCount = DBUtils.countRowsInTable(dbre.getConnection(), table);
    if(tableCount == 0) {
      logger.fine("Table "+table+" has no rows. Skipping");
      return true;
    }

    for (int i = 0; i < analyses.length; i++) {

      String sql = "SELECT COUNT(*) FROM " + table
          + ", analysis a WHERE LCASE(a.logic_name)='" + analyses[i]
          + "' AND a.analysis_id=" + table + ".analysis_id";
      Connection con = dbre.getConnection();
      int rows = DBUtils.getRowCount(con, sql);
      if (rows == 0) {
        ReportManager.problem(this, con, table
            + " has no features for analysis " + analyses[i]);
        result = false;
      }
      else {
        ReportManager.correct(this, con, table + " has features for analysis "
            + analyses[i]);
      }
    }

    return result;

  }

  // -----------------------------------------------------------------

} // FeatureAnalysis
