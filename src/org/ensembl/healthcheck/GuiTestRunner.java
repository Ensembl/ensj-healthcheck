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
public class GuiTestRunner extends TestRunner{
  
  protected static Logger logger = Logger.getLogger("HealthCheckLogger");
  private GuiTestRunnerFrame gtrf;
  
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
    
    gtrf = new GuiTestRunnerFrame();
    gtrf.show();
    
  } // openFrame
  
  // -------------------------------------------------------------------------
  
  private void initFrame() {
    
    List tests = findAllTests();
    
    gtrf.initTestPanel(tests, groupsToRun);
    
    this.runAllTests(tests, false, gtrf);
    
    ConnectionPool.closeAll();
    
    gtrf.setStatus("Done");
    
  } // initFrame
  
  
  // -------------------------------------------------------------------------
  
  private void setupLogging() {
    
    logger.addHandler(new CallbackHandler(gtrf, new LogFormatter()));
    logger.setLevel(Level.INFO);
    
    logger.info("Ready");
    
  } // setupLogging
  
  // -------------------------------------------------------------------------
  
  
  private void parseCommandLine(String[] args) {
    
    for (int i=0; i < args.length; i++) {
      
      groupsToRun.add(args[i]);
      System.out.println("Will run tests in group " + args[i]);
    }
    
  } // parseCommandLine
  
  // -------------------------------------------------------------------------
  /**
   * Run all the tests in a list.
   * @param allTests The tests to run, as objects.
   * @param forceDatabases If true, use only the database name pattern specified
   * on the command line, <em>not</em> the regular expression built in to the test case.
   */
  protected void runAllTests(List allTests, boolean forceDatabases, GuiTestRunnerFrame gtrf) {
    
    Iterator it = allTests.iterator();
    while (it.hasNext()) {
      
      EnsTestCase testCase = (EnsTestCase)it.next();
      
      if (testCase.inGroups(groupsToRun)) {
        
        gtrf.setStatus("Running " + testCase.getShortTestName());
        if (preFilterRegexp != null) {
          testCase.setPreFilterRegexp(preFilterRegexp);
        }
        
        if (forceDatabases) {
          // override built-in database regexp with the one specified on the command line
          testCase.setDatabaseRegexp(preFilterRegexp);
        }
        
        gtrf.setTestButtonEnabled(testCase.getTestName(), true);
        
        TestResult tr = testCase.run();
        
        Color c = tr.getResult() ? new Color(0, 128, 0) : Color.RED;
                  
        gtrf.setTestButtonColour(testCase.getTestName(), c);
          
      }
    }
    
  } // runAllTests
  // -------------------------------------------------------------------------
  
} // GuiTestRunner
