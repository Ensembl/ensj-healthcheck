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

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Allows a GUI-controlled test to run in its own thread and update the GUI.
 */
public class GUITestRunnerThread extends Thread {

    private EnsTestCase testCase;

    private GuiTestRunnerFrame guiTestRunnerFrame;

    private ThreadGroup threadGroup;

    private int maxThreads;

    private boolean isRunning = false;

    private Object target;

    /**
     * Creates a new instance of GUITestRunnerThread
     * 
     * @param threadGroup The group which this test thread should be a member of.
     * @param testCase The test case that this thread should run (either single or multi).
     * @param target Either a DatabaseRegistryEntry or DatabaseRegistry depending on the type of
     *            testCase.
     * @param gtrf A reference to the parent GuiTestRunnerFrame to update as the thread runs.
     * @param maxThreads The maximum number of threads to run at any one time.
     */
    public GUITestRunnerThread(ThreadGroup threadGroup, EnsTestCase testCase, Object target, GuiTestRunnerFrame gtrf, int maxThreads) {

        super(threadGroup, "");

        this.testCase = testCase;
        this.guiTestRunnerFrame = gtrf;
        this.threadGroup = threadGroup;
        this.maxThreads = maxThreads;
        this.target = target;
        this.guiTestRunnerFrame = gtrf;

    }

    /**
     * Implementation of the Runnable interface.
     */
    public void run() {

        // wait until there aren't too many threads running
        while (runningThreadCount() >= maxThreads) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isRunning = true;
        boolean result;

        if (testCase instanceof SingleDatabaseTestCase) {

            DatabaseRegistryEntry dbre = (DatabaseRegistryEntry) target;
            String message = testCase.getShortTestName() + ": " + dbre.getName();
            guiTestRunnerFrame.updateProgressDialog(message);
            System.out.println("# starting " + message);
            result = ((SingleDatabaseTestCase) testCase).run(dbre);
            System.out.println("# done " + message);
            guiTestRunnerFrame.incrementNumberRun(1);
            guiTestRunnerFrame.updateProgressDialog();

        } else if (testCase instanceof MultiDatabaseTestCase) {

            DatabaseRegistry dbr = (DatabaseRegistry) target;
            String message = testCase.getShortTestName() + " ( " + dbr.getEntryCount() + " databases)";
            guiTestRunnerFrame.updateProgressDialog(message);
            result = ((MultiDatabaseTestCase) testCase).run(dbr);
            guiTestRunnerFrame.incrementNumberRun(dbr.getEntryCount());
            guiTestRunnerFrame.updateProgressDialog();
            
        }

        isRunning = false;

    }

    // -------------------------------------------------------------------------
    /**
     * Try to figure out how many tests are actually running; note that just because a test thread
     * has been started doesn't mean that the test is actually running.
     * 
     * @return The number of threads that are currently running.
     */
    public int runningThreadCount() {

        int result = 0;

        Thread[] allThreads = new Thread[threadGroup.activeCount()];
        int allThreadCount = threadGroup.enumerate(allThreads);

        for (int i = 0; i < allThreadCount; i++) {
            GUITestRunnerThread gtrt = (GUITestRunnerThread) allThreads[i];
            if (gtrt.isRunning()) {
                result++;
            }
        }

        //System.out.println(result + " threads running out of a total of " + threadGroup.activeCount());
        return result;

    } // runningThreadCount

    // -------------------------------------------------------------------------
    /**
     * Return whether this test is acutally running.
     * 
     * @return Whether the test is running or not.
     */
    public boolean isRunning() {

        return isRunning;

    }

    // -------------------------------------------------------------------------

} // GUITestRunnerThread
