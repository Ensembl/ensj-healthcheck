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

package org.ensembl.healthcheck.gui;

import java.awt.Color;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Allows a GUI-controlled test to run in its own thread and update the GUI.
 */
public class GUITestRunnerThread extends Thread {
  
  private EnsTestCase testCase;
  private GuiTestRunnerFrame guiTestRunnerFrame;
  private ThreadGroup threadGroup;
  private int maxThreads;
  private boolean isRunning = false;
  
  /** 
   * Creates a new instance of GUITestRunnerThread
   * @param threadGroup The group which this test thread should be a member of.
   * @param testCase The test case that this thread should run.
   * @param gtrf A reference to the parent GuiTestRunnerFrame to update as the thread runs.
   * @param maxThreads The maximum number of threads to run at any one time.
   */
  public GUITestRunnerThread(ThreadGroup threadGroup, EnsTestCase testCase, GuiTestRunnerFrame gtrf, int maxThreads) {
    
    super(threadGroup, "");
    
    this.testCase = testCase;
    this.guiTestRunnerFrame = gtrf;
    this.threadGroup = threadGroup;
    this.maxThreads = maxThreads;
    
  }
  
  /** 
   * Implementation of the Runnable interface.
   */
  public void run() {
    
    // wait until there aren't too many threads running
    while (runningThreadCount() > maxThreads) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    // and then run the test
    
    guiTestRunnerFrame.setTestButtonEnabled(testCase.getTestName(), true);
    
    isRunning = true;
    //System.out.println("###In GUITestRunnerThread; preFilter=" + testCase.getPreFilterRegexp() + " databaseRegexp=" + testCase.getDatabaseRegexp());
    
    TestResult tr = testCase.run();
    
    isRunning = false;
    
    Color c = tr.getResult() ? new Color(0, 128, 0) : Color.RED;
    
    guiTestRunnerFrame.setTestButtonColour(testCase.getTestName(), c);
    
    guiTestRunnerFrame.setTestInfoWindowText(testCase.getTestName(), ReportManager.getReportsByTestCase(testCase.getTestName(), guiTestRunnerFrame.getOutputLevel()));
    
    guiTestRunnerFrame.repaint();
    
  }
  
  // -------------------------------------------------------------------------
  /**
   * Try to figure out how many tests are actually running; note that just because a test thread has been started doesn't mean that the test is actually running.
   * @return The number of threads that are currently running.
   */
  private int runningThreadCount() {
    
    int result = 0;

    Thread[] allThreads = new Thread[threadGroup.activeCount()];
    int allThreadCount = threadGroup.enumerate(allThreads);
    
    for (int i = 0; i < allThreadCount; i++) {
      GUITestRunnerThread gtrt = (GUITestRunnerThread)allThreads[i];
      if (gtrt.isRunning()) {
       result++; 
      }
    }
    
    System.out.println(result + " threads running out of a total of " + threadGroup.activeCount());
    return result;
    
  } // runningThreadCount
  
  // -------------------------------------------------------------------------
  /** 
   * Return whether this test is acutally running.
   * @return Whether the test is running or not.
   */
  public boolean isRunning() {
   
    return isRunning;
    
  }
  
  // -------------------------------------------------------------------------
  
} // GUITestRunnerThread