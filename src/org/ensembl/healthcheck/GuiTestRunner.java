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

import org.ensembl.healthcheck.util.*;

/**
 * Graphical test runner.
 */
public class GuiTestRunner extends TestRunner {
  
  /** The logger to use for this class */
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  private GuiTestRunnerFrame gtrf;
  
  /** Default maximum number of test threads to run at any one time */
  protected int maxThreads = 4;
  
  /** Whether or not to use only the pre-filter regexp */
  protected boolean forceDatabases = false;
  
  /** Pre-filter regexp; may be overridded on command line */
  protected String preFilterRegexp = "";
 
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
    
    gtr.readPropertiesFile();
    
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
        
        if (preFilterRegexp != null) {
          testCase.setPreFilterRegexp(preFilterRegexp);
        }
        
        if (forceDatabases) {
          // override built-in database regexp with the one specified on the command line
          testCase.setDatabaseRegexp(preFilterRegexp);
        }
        
        GUITestRunnerThread t = new GUITestRunnerThread(testThreads, testCase, gtrf, maxThreads);
        t.start(); // note that this will actually wait until < maxThreads are running before calling run()
        
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
   * Set whether or not to only use the pre-filter regexp.
   * @param b Whether or not to use the pre-filter regexp.
   */
  public void setForceDatabases(boolean b) {
    
    forceDatabases = b;
    logger.finest("Set forceDatabases to " + forceDatabases);
    
    
  } // setForceDatabases
  
  /** 
   * Get whether or not to only use the pre-filter regexp.
   * @return True if the pre-filter regecp only is to be used.
   */
  public boolean getForceDatabases() {
    
    return forceDatabases;
    
  } // getforceDatabase
  
  // -------------------------------------------------------------------------
  /** 
   * Set the pre-filter regexp.
   * @param re The new pre-filter regexp.
   */
  public void setPreFilterRegexp(String re) {
    
    preFilterRegexp = re;
    logger.finest("Set preFilterRegexp to " + preFilterRegexp);
    
  } // setPreFilterRegexp
  
  /** 
   * Get the value of the pre-filter regexp.
   * @return The pre-filter regexp (may be "")
   */
  public String getPreFilterRegexp() {
    
    return preFilterRegexp;
    
  } // getPreFilterRegexp
  
  // -------------------------------------------------------------------------
  
  
} // GuiTestRunner
