/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNUsql
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck;

import java.util.*;
import java.awt.Color;
import java.util.logging.*;

import org.ensembl.healthcheck.util.*;

/**
 * Graphical test runner.
 */
public class GuiTestRunner extends TestRunner {
  
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  private GuiTestRunnerFrame gtrf;
  
  protected int maxThreads = 1;
  
  protected boolean forceDatabases = false;
  
  protected String preFilterRegexp = "";
  
  /**
   * Creates a new instance of GuiTestRunner
   */
  public GuiTestRunner() {
  }
  
  // -------------------------------------------------------------------------
  /**
   *
   */
  public static void main(String[] args) {
    
    GuiTestRunner gtr = new GuiTestRunner();
    
    gtr.openFrame();
    
    gtr.setupLogging();
    
    gtr.parseCommandLine(args);
    
    gtr.readPropertiesFile();
    
    gtr.initFrame();
    
  } // main
  
  // -------------------------------------------------------------------------
  
  private void openFrame() {
    
    gtrf = new GuiTestRunnerFrame(this);
    gtrf.show();
    
  } // openFrame
  
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
   * @param allTests The tests to run, as objects.
   * @param forceDatabases If true, use only the database name pattern specified
   * on the command line, <em>not</em> the regular expression built in to the test case.
   */
  protected void runAllTests(List allTests, List groups, GuiTestRunnerFrame gtrf) {
    
    ThreadGroup testThreads = new ThreadGroup("test_threads");
    
    Iterator it = allTests.iterator();
    while (it.hasNext()) {
      
      // wait for the number of threads to drop
      while (testThreads.activeCount() > 1000) { // XXX
        try {
          Thread.yield();
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      org.ensembl.healthcheck.testcase.EnsTestCase testCase = (org.ensembl.healthcheck.testcase.EnsTestCase)it.next();
      
      if (testCase.inGroups(groups)) {
        
        gtrf.setStatus("Running " + testCase.getShortTestName());
        if (preFilterRegexp != null) {
          testCase.setPreFilterRegexp(preFilterRegexp);
        }
        
        if (forceDatabases) {
          // override built-in database regexp with the one specified on the command line
          testCase.setDatabaseRegexp(preFilterRegexp);
        }
        
        Thread t = new Thread(testThreads, new GUITestRunnerThread(testCase, gtrf));
        System.out.println("active_count = " + testThreads.activeCount());
        t.start();
        
      }
      
    } // while it.hasNext()
    
    gtrf.setStatus("Done");
    
  }
  
  // runAllTests
  // -------------------------------------------------------------------------
  
  public void setMaxThreads(int t) {
    
    maxThreads = t;
    logger.finest("Set maxThreads to " + maxThreads);
    
  } // setMaxThreads
  
  public int getMaxThreads() {
    
    return maxThreads;
    
  } // getMaxThreads
  
  public void setForceDatabases(boolean b) {
    
    forceDatabases = b;
    logger.finest("Set forceDatabases to " + forceDatabases);
    
    
  } // setForceDatabases
  
  public boolean getForceDatabases() {
    
    return forceDatabases;
    
  } // getforceDatabase
  
  public void setPreFilterRegexp(String re) {
    
    preFilterRegexp = re;
    logger.finest("Set preFilterRegexp to " + preFilterRegexp);
    
  } // setPreFilterRegexp
  
  public String getPreFilterRegexp() {
    
    return preFilterRegexp;
    
  } // getPreFilterRegexp
  
  // -------------------------------------------------------------------------
  
} // GuiTestRunner
