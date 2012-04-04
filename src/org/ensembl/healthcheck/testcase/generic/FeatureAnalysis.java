/*
 Copyright (C) 2003 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

    addToGroup("post_genebuild");
    addToGroup("release");
    addToGroup("pre-compara-handover");
    addToGroup("post-compara-handover");

    setHintLongRunning(true);
    setDescription("Check that features exist for the expected analyses.");
    setTeamResponsible(Team.GENEBUILD);
  }

  /**
   * This only applies to core and Vega databases.
   */
  public void types() {
    removeAppliesToType(DatabaseType.VEGA);
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
    result &= checkFeatureAnalyses(dbre, "protein_feature",
        proteinFeatureAnalyses);

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
              result = true;
            }
            //Unmapped object, object_xref & marker_feature are allowed to share
            else if ("unmapped_object".equals(featureTable) && ("object_xref".equals(priorTable) || "marker_feature".equals(priorTable))) {
              result = true;
            }
            //Operon & operon transcript are allowed to share
            else if ("operon".equals(featureTable) && "operon_transcript".equals(priorTable)) {
              result = true;
            }
            else {
              String msg = String.format("Analysis with ID %d is used in %s as well as %s", id, featureTable, priorTable);
              ReportManager.problem(this, con, msg);
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
          }
        }
      }

      // look at each analysis ID *from the analysis table* to see if it's
      // used
      // somewhere
      // some analyses may be listed in the analysis table but actually
      // used in
      // the otherfeatures database
      // so go and get the lis of analyses from the feature tables in the
      // otherfeatures database first
      Map<Integer,String> otherfeatureAnalyses = getAnalysesFromOtherDatabase(dbre,
          featureTables);

      Statement stmt = con.createStatement();
      ResultSet rs = stmt
          .executeQuery("SELECT analysis_id, logic_name FROM analysis");
      while (rs.next()) {
        int analysisID = rs.getInt("analysis_id");
        String logicName = rs.getString("logic_name");
        if (!analysesFromFeatureTables.containsKey(analysisID)
            && !otherfeatureAnalyses.containsKey(analysisID)) {
          ReportManager.problem(this, con, "Analysis with ID " + analysisID
              + ", logic name " + logicName
              + " is not used in any feature table");
          result = false;
        }
        else {
          ReportManager.correct(this, con, "Analysis with ID " + analysisID
              + ", logic name " + logicName + " is used in "
              + analysesFromFeatureTables.get(new Integer(analysisID)));
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

    logger.fine("Checking analyses for " + table + " in " + dbre.getName());

    boolean result = true;

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

  private Map<Integer, String> getAnalysesFromOtherDatabase(DatabaseRegistryEntry dbre,
      String[] featureTables) {

    Map<Integer, String> analyses = new HashMap<Integer, String>();

    String ofName = dbre.getName().replaceAll("core", "otherfeatures");
    DatabaseRegistry dr = dbre.getDatabaseRegistry();
    DatabaseRegistryEntry ofDBRE = dr.getByExactName(ofName);
    if (ofDBRE == null) {
      logger.info("Can't get otherfeatures database for " + dbre.getName());
      return analyses;
    }

    try {
      Connection con = ofDBRE.getConnection();
      // build cumulative list of analyses from feature tables
      for (int t = 0; t < featureTables.length; t++) {
        String featureTable = featureTables[t];
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT(analysis_id) FROM "
            + featureTable);
        while (rs.next()) {
          Integer analysisID = rs.getInt("analysis_id");
          analyses.put(analysisID, featureTable);
        }
        rs.close();
        stmt.close();
      }
    }
    catch (SQLException se) {
      se.printStackTrace();
    }

    return analyses;

  }

  // -----------------------------------------------------------------

} // FeatureAnalysis
