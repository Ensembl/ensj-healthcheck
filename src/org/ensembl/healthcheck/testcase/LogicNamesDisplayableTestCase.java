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

package org.ensembl.healthcheck.testcase;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check that the logic names in the analysis table are displayable.
 * Currently reads the list of displayable logc names from a text file.
 * Current set of logic names is stored at
 *  http://www.ensembl.org/Docs/wiki/html/EnsemblDocs/LogicNames.html
 */
public class LogicNamesDisplayableTestCase extends EnsTestCase {
  
  // a list of the tables to check the analysis_id in
  private String[] featureTables = { "gene", "prediction_transcript", 
                                     "dna_align_feature", "marker_feature", 
                                     "protein_feature", "qtl_feature", 
                                     "repeat_feature", "simple_feature",
                                     "protein_align_feature" };
				     
  
  private static final String LOGIC_NAMES_FILE = "logicnames.txt";
  private static final boolean CASE_SENSITIVE = false;
  
  /**
   * Creates a new instance of LogicNamesDisplayableTestCase
   */
  public LogicNamesDisplayableTestCase() {
    String[] cols = { "logic_name", "analysis_id" };
    addCondition(new HasTableColumnsCondition("analysis", cols));
    addToGroup("post_genebuild");
    setDescription("Checks that all logic names in analysis are displayable");
    setHintLongRunning(true);
  }
  

  public TestResult run() {
    // DatabaseConnectionIterator it = getMatchingSchemaIterator();
    DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    boolean result = true;
    
    while (it.hasNext()) {
      try {
        Connection con = (Connection)it.next();
        result = (result && checkLogicNames(con));
        result = (result && checkProteinFeatureAnalysis(con));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  



  /**
   * Looks at analysis IDs in feature tables and checks the logic names 
   * they are associated with will be displayed by the web code.
   * The list of valid logic names is currently at
   * http://www.ensembl.org/Docs/wiki/html/EnsemblDocs/LogicNames.html
   * Note that this test case actually uses the names from the file 
   * logicnames.txt
   * which currently has to be manually created from the above URL.
   *
   * @return 
   */
  private boolean checkLogicNames(Connection con) throws SQLException {
    boolean result = true;
    String message = CASE_SENSITIVE ? 
        "Logic name comparison is case sensitive" : 
        "Logic name comparison is NOT case sensitive";
    logger.info(message);

    // read the file containing the allowed logic names
    String[] allowedLogicNames = Utils.readTextFile(LOGIC_NAMES_FILE);
    logger.fine("Read " + allowedLogicNames.length + 
                " logic names from " + LOGIC_NAMES_FILE);

    // cache logic_names by analysis_id
    Map logicNamesByAnalID = new HashMap();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT analysis_id, logic_name"
                                     +" FROM analysis");
    while (rs.next()) {
      logicNamesByAnalID.put(rs.getString("analysis_id"), 
                             rs.getString("logic_name"));
    }
        
    for (int t = 0; t < featureTables.length; t++) {
      String featureTableName = featureTables[t];
      logger.finest("Analysing features in " + featureTableName);

      // get analysis IDs
      String[] analysisIDs = 
        getColumnValues(con, "SELECT DISTINCT analysis_id FROM " 
                        + featureTableName);

      // check each analysis ID
      for (int i = 0; i < analysisIDs.length; i++) {
        /* check that there is an entry in the analysis table with this 
         * ID (i.e. foreign key integrity check)
         */
        if (logicNamesByAnalID.get(analysisIDs[i]) == null) {
          ReportManager.problem(this, con, "Feature table " + featureTableName 
                                + " refers to non-existant analysis with ID " 
                                + analysisIDs[i]);
          result = false;
        } else {
          // check that logic name corresponding to this analysis id is valid
          String logicName = (String)logicNamesByAnalID.get(analysisIDs[i]);
          if (!Utils.stringInArray(logicName, allowedLogicNames, 
                                   CASE_SENSITIVE)) {
            ReportManager.problem(this, con, "Feature table " 
                 + featureTableName + " has features with logic name "
                 + logicName + " which will not be drawn");
            result = false;
          }
        } 
      }
    }
    rs.close();
    stmt.close();
    return result;
  }


       
  /*
   * Does a set of analysis checks for the protein feature table
   * This table has some bizarre requirements for the associated analysis
   * which will hopefully change at some point.  In the meantime
   * this healthcheck will verify that those requirements are
   * met.
   */
  private boolean 
  checkProteinFeatureAnalysis(Connection con) throws SQLException {
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(
             "SELECT a.analysis_id, a.gff_feature, a.gff_source " 
           + "FROM analysis a, protein_feature pf "
           + "WHERE a.analysis_id = pf.analysis_id "
           + "GROUP BY a.analysis_id");

    boolean noProblems = true;

    while(rs != null && rs.next()) {
      int    analysisId = rs.getInt(1);
      String gffFeat   = rs.getString(2).toUpperCase();
      String gffSource = rs.getString(3).toUpperCase();
            
      if(gffSource.equals("PRINTS") || gffSource.equals("PFAM") ||
         gffSource.equals("PROSITE")|| gffSource.equals("PROFILE")) {

        /* gff_feature must be domain */
        if(!gffFeat.equals("DOMAIN")) {
          ReportManager.problem(this, con, "protein_feature" 
                     + " analysis with analysis_id = " + analysisId
                      + " and gffSource = " + gffSource
                      + " must have gffSource eq 'DOMAIN'");
          noProblems = false;
        }   
      } else if(gffSource.equals("BLASTP") || gffSource.equals("SEG") ||
                gffSource.equals("TMHMM")  || gffSource.equals("NCOILS") 
                || gffSource.equals("SIGNALP")) {             

        /* gff_feature must not be domain */                
        if(gffFeat.equals("DOMAIN")) {	    
            ReportManager.problem(this, con, "protein_feature" 
		      + " analysis with analysis_id = " + analysisId
                      + " and gffSource = " + gffSource
                      + " must have gffSource ne 'DOMAIN'");
            noProblems = false;
        }
      } else if(!gffSource.equals("SUPERFAMILY")) {
        /* problem... not a known gffsource */
        ReportManager.problem(this, con, "protein_feature" 
                      + " analysis with analysis_id = " + analysisId
                      + " has unknown gff_source = '" + gffSource + "'");
        noProblems = false;
      }
    }

    if(noProblems) {
        ReportManager.correct(this, con,"protein_feature analysis table "
                              + "entries look OK");
    } 

    return noProblems;
  }


} // LogicNamesDisplayableTestCase
