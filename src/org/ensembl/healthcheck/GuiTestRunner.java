/*
  Copyright (C) 2004 EBI, GRL
 
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

import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Graphical test runner.
 */
public class GuiTestRunner extends TestRunner implements Reporter {
  
  /** The logger to use for this class */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  private GuiTestRunnerFrame gtrf;
  
  /** Default maximum number of test threads to run at any one time */
  protected int maxThreads = 4;
  
  /** The schemas to act upon */
  protected String[] selectedSchemas;
  
  // -------------------------------------------------------------------------
  /**
   * Command-line entry point.
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    
    GuiTestRunner gtr = new GuiTestRunner();
    
    gtr.openFrame();
    
    gtr.setupLogging();
    
    gtr.parseCommandLine(args);
    
    Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);
    
    gtr.readStoredSchemaInfo();
    
    ReportManager.setReporter(gtr);
    
    gtr.initFrame();
    
    gtr.showFrame();
    
  } // main
  
  // -------------------------------------------------------------------------
  
  private void openFrame() {
    
    gtrf = new GuiTestRunnerFrame(this);
    //gtrf.show();
    
  } // openFrame
  
  private void showFrame() {
    
    gtrf.show();
    
  } // showFrame
  
  // -------------------------------------------------------------------------
  
  private void initFrame() {
    
    List tests = findAllTests();
    
    gtrf.initTestPanel(null, null);
    
    gtrf.initGroupList(tests, listAllGroups(tests));
    
    ConnectionPool.closeAll();
    
  } // initFrame
  
  
  // -------------------------------------------------------------------------
  
  private void setupLogging() {
    
    logger.addHandler(new CallbackHandler(gtrf, new LogFormatter()));
    logger.addHandler(new MyStreamHandler(System.out, new LogFormatter()));
    
    logger.setLevel(Level.ALL);
    
    logger.info("Ready");
    
  } // setupLogging
  
  // -------------------------------------------------------------------------
  
  
  private void parseCommandLine(String[] args) {
    
    
  } // parseCommandLine
  
  // -------------------------------------------------------------------------
  /**
   * Run all the tests in a list.
   * @param groups The groups to run (as Strings)
   * @param gtrf A reference to the GuiTestRunnerFrame to update as the tests run.
   * @param allTests The tests to run, as objects.
   */
  protected void runAllTests(List allTests, List groups, GuiTestRunnerFrame gtrf) {
    
    ThreadGroup testThreads = new ThreadGroup("test_threads");
    
    Iterator it = allTests.iterator();
    while (it.hasNext()) {
      
      org.ensembl.healthcheck.testcase.EnsTestCase testCase = (org.ensembl.healthcheck.testcase.EnsTestCase)it.next();
      
      if (testCase.inGroups(groups)) {
        
        // set the regexp in each test case to be the actual name of the database, then run the test
        for (int i = 0; i < selectedSchemas.length; i++) {
          
          testCase.setDatabaseRegexp(selectedSchemas[i]);
          GUITestRunnerThread t = new GUITestRunnerThread(testThreads, testCase, gtrf, maxThreads);
          t.start(); // note that this will actually wait until < maxThreads are running before calling run()
                    
        }
      }
      
    } // while it.hasNext()
    
    /*
    // wait until all the tests have run, then print the sumamries
    while (testThreads.activeCount() > 0) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    gtrf.setStatus("Done");
    printReportsByTest(ReportLine.ALL);
     
    printReportsByDatabase(ReportLine.ALL);
     */
  }
  
  // runAllTests
  // -------------------------------------------------------------------------
  /**
   * Set the maximum number of test threads to run at one time.
   * @param t The new number of threads.
   */
  public void setMaxThreads(int t) {
    
    maxThreads = t;
    logger.finest("Set maxThreads to " + maxThreads);
    
  } // setMaxThreads
  
  /**
   * Get the maximum number of test threads to run at one time.
   * @return The number of threads.
   */
  public int getMaxThreads() {
    
    return maxThreads;
    
  } // getMaxThreads
  
  // -------------------------------------------------------------------------
  /**
   * Set the schemas to run the tests on.
   * @param s An array of schema names, as objects since this is how they are returned from a JList.
   */
  public void setSelectedSchemas(Object[] s) {
    
    selectedSchemas = new String[s.length];
    for (int i = 0; i < s.length; i++) {
      selectedSchemas[i] = (String)s[i];
      logger.finest("Added " + selectedSchemas[i] + " to list of schemas");
    }
    
  }
  
  // -------------------------------------------------------------------------
  
  // Implementation of Reporter interface
  
  public void message(ReportLine reportLine) {
    
    // TBC
    
  }
  
  public void startTestCase(EnsTestCase testCase) {
    
    // TBC
    
  }
  
  public void finishTestCase(EnsTestCase testCase, TestResult result) {
    
    // TBC
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Get a list of all the schemas. Used to produce the JList in the GUI.
   * @return An array of the schema names.
   */
  public String[] getSchemaList() {
    
    return getListOfDatabaseNames(".*");
    
  }
  
  // -------------------------------------------------------------------------
  
} // GuiTestRunner
