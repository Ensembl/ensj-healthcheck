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

import org.ensembl.healthcheck.testcase.*;

/**
 * description
 */
public class TestRunnerThread implements Runnable {
  
  private EnsTestCase testCase;
  private ThreadGroup threadGroup;
  private int maxThreads;
  
  /** Creates a new instance of TestRunnerThread
   * @param testCase The test case to run.
   * @param threadGroup The ThreadGroup to run this test in.
   * @param maxThreads The maximum number of threads to run.
   */
  public TestRunnerThread(EnsTestCase testCase, ThreadGroup threadGroup, int maxThreads) {
    
    this.testCase = testCase;
    this.threadGroup = threadGroup;
    this.maxThreads = maxThreads;
    
  }
  
  // -------------------------------------------------------------------------

  /**
   * Implementation of Runnable; start the thread if there aren't too many running.
   **/
  public void run() {
    
    // wait until there aren't too many threads running
    while (threadGroup.activeCount() > maxThreads) {
      try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
    }
    
    // and then run the test
    testCase.run();
    
  }
  
  // -------------------------------------------------------------------------

} // TestRunnerThread
