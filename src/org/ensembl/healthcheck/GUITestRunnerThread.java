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

import java.awt.Color;

import org.ensembl.healthcheck.testcase.*;

/**
 * description
 */
public class GUITestRunnerThread implements Runnable {
  
  private EnsTestCase testCase;
  private GuiTestRunnerFrame guiTestRunnerFrame;
  
  /**
   * Creates a new instance of GUITestRunnerThread
   */
  public GUITestRunnerThread(EnsTestCase testCase, GuiTestRunnerFrame gtrf) {
    
    this.testCase = testCase;
    this.guiTestRunnerFrame = gtrf;
    
  }
  
  public void run() {
    
    guiTestRunnerFrame.setTestButtonEnabled(testCase.getTestName(), true);
    
    TestResult tr = testCase.run();
    
    Color c = tr.getResult() ? new Color(0, 128, 0) : Color.RED;
    
    guiTestRunnerFrame.setTestButtonColour(testCase.getTestName(), c);
    
    guiTestRunnerFrame.repaint();
    
  }
  
} // GUITestRunnerThread