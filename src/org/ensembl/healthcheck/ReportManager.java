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

package org.ensembl.healthcheck;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Handles test reporting.
 */
public class ReportManager {
  
  /** A hash of lists keyed on the test name. */
  protected static Map reportsByTest = new HashMap();    
  /** A hash of lists keyed on the database name */
  protected static Map reportsByDatabase = new HashMap(); 
  /** The logger to use for this class */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  // -------------------------------------------------------------------------
  /** 
   * Add a test case report.
   * @param report The ReportLine to add.
   */
  public static void add(ReportLine report) {
    
    String testCaseName = report.getTestCaseName();
    String databaseName = report.getDatabaseName();
    
    ArrayList lines;
    
    // add to each hash
    if (testCaseName != null && testCaseName.length() > 0) {
      // create the lists if they're not there already
      if (reportsByTest.get(testCaseName) == null) {
        lines = new ArrayList();
        lines.add(report);
        reportsByTest.put(testCaseName, lines);
      } else {
        // get the relevant list, update it, and re-add it
        lines = (ArrayList)reportsByTest.get(testCaseName);
        lines.add(report);
        reportsByTest.put(testCaseName, lines);
      }
      
    } else {
      logger.warning("Cannot add report with test case name not set");
    }
    
    if (databaseName != null && databaseName.length() > 0) {
      // create the lists if they're not there already
      if (reportsByDatabase.get(databaseName) == null) {
        lines = new ArrayList();
        lines.add(report);
        reportsByDatabase.put(databaseName, lines);
      } else {
        // get the relevant list, update it, and re-add it
        lines = (ArrayList)reportsByDatabase.get(databaseName);
        lines.add(report);
        reportsByDatabase.put(databaseName, lines);
      }
      
    } else {
      logger.warning("Cannot add report with database name not set");
    }
    
    
  } // add
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method for storing reports, intended to be easy to call from an EnsTestCase.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param level The level of this report.
   * @param message The message to be reported.
   */
  public static void report(EnsTestCase testCase, Connection con, int level, String message) {
    
    add(new ReportLine(testCase.getTestName(), DBUtils.getShortDatabaseName(con), level, message));
    
  } // report
  
  // -------------------------------------------------------------------------
  /**
   * Store a ReportLine with a level of ReportLine.INFO.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param message The message to be reported.
   */
  public static void problem(EnsTestCase testCase, Connection con, String message) {
    
    report(testCase, con, ReportLine.PROBLEM, message);
    
  } // problem
  
  /**
   * Store a ReportLine with a level of ReportLine.INFO.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param message The message to be reported.
   */
  public static void info(EnsTestCase testCase, Connection con, String message) {
    
    report(testCase, con, ReportLine.INFO, message);
    
  } // info
  
  /**
   * Store a ReportLine with a level of ReportLine.SUMMARY.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param message The message to be reported.
   */
  public static void summary(EnsTestCase testCase, Connection con, String message) {
    
    report(testCase, con, ReportLine.SUMMARY, message);
    
  } // summary
  
   /**
   * Store a ReportLine with a level of ReportLine.CORRECT.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param message The message to be reported.
   */
  public static void correct(EnsTestCase testCase, Connection con, String message) {
    
    report(testCase, con, ReportLine.CORRECT, message);
    
  } // summary
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on test case name.
   * @return The HashMap of all the reports, keyed on test case name.
   */
  public static Map getAllReportsByTestCase() {
    
    return reportsByTest;
    
  } // getReportsByTestCase
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on database name.
   * @return The HashMap of all the reports, keyed on database name.
   */
  public static Map getAllReportsByDatabase() {
    
    return reportsByDatabase;
    
  } // getReportsByDatabase
  
  // -------------------------------------------------------------------------
  /** Get a list of all the reports corresponding to a particular test case.
   * @return A List of the results (as a list) corresponding to test.
   * @param testCaseName The test case to filter by.
   * @param level The minimum level of report to include, e.g. ReportLine.INFO
   */
  public static List getReportsByTestCase(String testCaseName, int level) {
    
    List allReports =  (List)reportsByTest.get(testCaseName);
    
    return filterList(allReports, level);
    
  } // getReportsByTestCase
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of all the reports corresponding to a particular database.
   * @param databaseName The database to report on.
   * @param level The minimum level of report to include, e.g. ReportLine.INFO
   * @return A List of the ReportLines corresponding to database.
   */
  public static List getReportsByDatabase(String databaseName, int level) {
    
    return filterList((List)reportsByDatabase.get(databaseName), level);
    
  } // getReportsByDatabase
  
  // -------------------------------------------------------------------------
  /** Filter a list or ReportLines so that only certain entries are returned.
   * @param list The list to filter.
   * @param level All reports with a priority above this level will be returned.
   * @return A list of the ReportLines that have a level >= that specified.
   */
  public static List filterList(List list, int level) {
    
    ArrayList result = new ArrayList();
    
    Iterator it = list.iterator();
    while (it.hasNext()) {
      ReportLine line = (ReportLine)it.next();
      if (line.getLevel() >= level) {
        result.add(line);
      }
    }
    
    return result;
    
  } // filterList
  // -------------------------------------------------------------------------
  
} // ReportManager
