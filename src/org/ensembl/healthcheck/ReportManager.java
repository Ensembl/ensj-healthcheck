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
 * ReportManager is the main class for reporting in the Ensj Healthcheck system. It provides methods
 * for storing reports - single items of information - and retrieving them in various formats.
 */
public class ReportManager {
  
  /** A hash of lists keyed on the test name. */
  protected static Map reportsByTest = new HashMap();
  /** A hash of lists keyed on the database name */
  protected static Map reportsByDatabase = new HashMap();
  /** The logger to use for this class */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  
  protected static Reporter reporter;

  public static void setReporter( Reporter rep ) {
      reporter = rep;
  }
	
    
    public static void startTestCase( EnsTestCase testCase ) {
	if( reporter != null ) {
	    reporter.startTestCase( testCase );
	}
    }

    public static void finishTestCase( EnsTestCase testCase, TestResult result ) {
	if( reporter != null ) {
	    reporter.finishTestCase( testCase, result );
	}
    }

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
    
    if( reporter != null ) {
	reporter.message( report );
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
    
    // this may be called when there is no DB connection
    String dbName = (con == null) ? "no_database" : DBUtils.getShortDatabaseName(con);
    
    add(new ReportLine(testCase.getTestName(), dbName, level, message));
    
  } // report
  
  // -------------------------------------------------------------------------
  /**
   * Convenience method for storing reports, intended to be easy to call from an EnsTestCase.
   * @param testCase The test case filing the report.
   * @param dbName The name of the database involved.
   * @param level The level of this report.
   * @param message The message to be reported.
   */
  public static void report(EnsTestCase testCase, String dbName, int level, String message) {
    
    add(new ReportLine(testCase.getTestName(), dbName, level, message));
    
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
   * Store a ReportLine with a level of ReportLine.PROBLEM.
   * @param testCase The test case filing the report.
   * @param dbName The name of the database involved.
   * @param message The message to be reported.
   */
  public static void problem(EnsTestCase testCase, String dbName, String message) {
    
    report(testCase, dbName, ReportLine.PROBLEM, message);
    
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
   * Store a ReportLine with a level of ReportLine.INFO.
   * @param testCase The test case filing the report.
   * @param dbName The name of the database involved.
   * @param message The message to be reported.
   */
  public static void info(EnsTestCase testCase, String dbName, String message) {
    
    report(testCase, dbName, ReportLine.INFO, message);
    
  } // info
  
  /**
   * Store a ReportLine with a level of ReportLine.SUMMARY.
   * @param testCase The test case filing the report.
   * @param con The database connection involved.
   * @param message The message to be reported.
   */
  public static void warning(EnsTestCase testCase, Connection con, String message) {
    
    report(testCase, con, ReportLine.WARNING, message);
    
  } // summary
  
  /**
   * Store a ReportLine with a level of ReportLine.SUMMARY.
   * @param testCase The test case filing the report.
   * @param dbName The name of the database involved.
   * @param message The message to be reported.
   */
  public static void warning(EnsTestCase testCase, String dbName, String message) {
    
    report(testCase, dbName, ReportLine.WARNING, message);
    
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
  
  /**
   * Store a ReportLine with a level of ReportLine.CORRECT.
   * @param testCase The test case filing the report.
   * @param dbName The name of the database involved.
   * @param message The message to be reported.
   */
  public static void correct(EnsTestCase testCase, String dbName, String message) {
    
    report(testCase, dbName, ReportLine.CORRECT, message);
    
  } // summary
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on test case name.
   * @return The HashMap of all the reports, keyed on test case name.
   */
  public static Map getAllReportsByTestCase() {
    
    return reportsByTest;
    
  } // getAllReportsByTestCase
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on test case name.
   * @param level The ReportLine level (e.g. PROBLEM) to filter on.
   * @return The HashMap of all the reports, keyed on test case name.
   */
  public static Map getAllReportsByTestCase(int level) {
    
    return filterMap(reportsByTest, level);
    
  } // getAllReportsByTestCase
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on database name.
   * @return The HashMap of all the reports, keyed on database name.
   */
  public static Map getAllReportsByDatabase() {
    
    return reportsByDatabase;
    
  } // getReportsByDatabase
  
  // -------------------------------------------------------------------------
  /**
   * Get a HashMap of all the reports, keyed on test case name.
   * @param level The ReportLine level (e.g. PROBLEM) to filter on.
   * @return The HashMap of all the reports, keyed on test case name.
   */
  public static Map getAllReportsByDatabase(int level) {
    
    return filterMap(reportsByDatabase, level);
    
  } // getAllReportsByTestCase
  
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
  /** Filter a list of ReportLines so that only certain entries are returned.
   * @param list The list to filter.
   * @param level All reports with a priority above this level will be returned.
   * @return A list of the ReportLines that have a level >= that specified.
   */
  public static List filterList(List list, int level) {
    
    ArrayList result = new ArrayList();
    
    if (list != null) {
      Iterator it = list.iterator();
      while (it.hasNext()) {
        ReportLine line = (ReportLine)it.next();
        if (line.getLevel() >= level) {
          result.add(line);
        }
      }
    } 
    
    return result;
    
  } // filterList
  
  // -------------------------------------------------------------------------
  /** Filter a HashMap of lists of ReportLines so that only certain entries are returned.
   * @param map The list to filter.
   * @param level All reports with a priority above this level will be returned.
   * @return A HashMap with the same keys as map, but with the lists filtered by level.
   */
  public static Map filterMap(Map map, int level) {
    
    HashMap result = new HashMap();
    
    Set keySet = map.keySet();
    Iterator it = keySet.iterator();
    while (it.hasNext()) {
      String key = (String)it.next();
      List list = (List)map.get(key);
      result.put(key, filterList(list, level));
    }
    
    return result;
    
  } // filterList
  // -------------------------------------------------------------------------
  
} // ReportManager
