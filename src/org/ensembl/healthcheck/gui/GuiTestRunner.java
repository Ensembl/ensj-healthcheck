/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Reporter;
import org.ensembl.healthcheck.TestRegistry;
import org.ensembl.healthcheck.TestRunner;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.CallbackHandler;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * Graphical test runner.
 */
public class GuiTestRunner extends TestRunner implements Reporter {

    /** The logger to use for this class */
    protected static Logger logger = Logger.getLogger("HealthCheckLogger");

    /** Default maximum number of test threads to run at any one time */
    protected int maxThreads = 1;

    private boolean debug = false;

    private GuiTestRunnerFrame gtrf;

    // -------------------------------------------------------------------------
    /**
     * Command-line entry point.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        new GuiTestRunner().run(args);

    } // main

    //---------------------------------------------------------------------

    private void run(String[] args) {

        ReportManager.setReporter(this);

        parseCommandLine(args);

        Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

        DatabaseRegistry databaseRegistry = new DatabaseRegistry(".*");
        if (databaseRegistry.getEntryCount() == 0) {
            logger.warning("Warning: no databases found!");
        }

        gtrf = new GuiTestRunnerFrame(this, new TestRegistry(), databaseRegistry);

        setupLogging();

        gtrf.show();

    }

    // -------------------------------------------------------------------------

    private void setupLogging() {

        logger.setUseParentHandlers(false); // stop parent logger getting the message

        logger.addHandler(new CallbackHandler(gtrf, new LogFormatter()));
        logger.addHandler(new MyStreamHandler(System.out, new LogFormatter()));

        logger.setLevel(Level.WARNING); // default - only print important messages

        if (debug) {

            logger.setLevel(Level.FINEST);

        }

    } // setupLogging

    // -------------------------------------------------------------------------

    private void parseCommandLine(String[] args) {

        if (args.length > 0 && args[0].equals("-debug")) {

            debug = true;
            logger.finest("Running in debug mode");

        } // parseCommandLine
    }

    // -------------------------------------------------------------------------
    /**
     * Run all the tests in a list.
     * @param tests The tests to run.
     * @param databases The databases to run the tests on.
     * @param gtrf The test runner frame in which to display the results.
     */
    protected void runAllTests(EnsTestCase[] tests, DatabaseRegistryEntry[] databases, GuiTestRunnerFrame gtrf) {

        gtrf.setTestProgressDialogVisibility(true);

        ThreadGroup testThreads = new ThreadGroup("test_threads");

        DatabaseRegistry selectedDatabaseRegistry = new DatabaseRegistry(databases);

        int totalTestsToRun = tests.length * databases.length;

        gtrf.setTotalToRun(totalTestsToRun);

        // for each test, if it's a single database test we run it against each
        // selected database in turn
        // for multi-database tests, we create a new DatabaseRegistry containing
        // the selected tests and use that
        GUITestRunnerThread t = null;
        for (int i = 0; i < tests.length; i++) {

            EnsTestCase test = tests[i];
            if (test instanceof SingleDatabaseTestCase) {

                for (int j = 0; j < databases.length; j++) {

                    t = new GUITestRunnerThread(testThreads, test, databases[j], gtrf, getMaxThreads());
                    t.start();

                }

            } else if (test instanceof MultiDatabaseTestCase) {

                t = new GUITestRunnerThread(testThreads, test, selectedDatabaseRegistry, gtrf, getMaxThreads());
                t.start();

            }
            // TODO - warn about not running OrderedDatabaseTestCase
            // TODO - result handling

        }

        // TODO - wait until all tests have finished, print results, remove progress window
        while (testThreads.activeCount() > 0) {
            gtrf.repaint();
            gtrf.repaintTestProgressDialog();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        gtrf.setTestProgressDialogVisibility(false);

        printReportsByTest(outputLevel);

    } // runAllTests

    // -------------------------------------------------------------------------
    /**
     * Set the maximum number of test threads to run at one time.
     * 
     * @param t The new number of threads.
     */
    public void setMaxThreads(int t) {

        maxThreads = t;
        logger.finest("Set maxThreads to " + maxThreads);

    } // setMaxThreads

    /**
     * Get the maximum number of test threads to run at one time.
     * 
     * @return The number of threads.
     */
    public int getMaxThreads() {

        return maxThreads;

    } // getMaxThreads

    // -------------------------------------------------------------------------
    // Implementation of Reporter interface
    
    /**
     * Called when a message is to be stored in the report manager.
     * 
     * @param reportLine
     *          The message to store.
     */
    public void message(ReportLine reportLine) {

        // TBC

    }

    /**
     * Called just before a test case is run.
     * 
     * @param testCase
     *          The test case about to be run.
     * @param dbre
     *          The database which testCase is to be run on, or null of no/several databases.
     */
    public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

        // TBC

    }

    /**
     * Should be called just after a test case has been run.
     * 
     * @param testCase
     *          The test case that was run.
     * @param result
     *          The result of testCase.
     * @param dbre
     *          The database which testCase was run on, or null of no/several databases.
     */
    public void finishTestCase(EnsTestCase testCase, boolean result, DatabaseRegistryEntry dbre) {

        // TBC

    }

    // -------------------------------------------------------------------------
    /**
     * Get a list of all the schemas. Used to produce the JList in the GUI.
     * 
     * @return An array of the schema names.
     */
    public String[] getSchemaList() {

        return getListOfDatabaseNames(".*");

    }

    // -------------------------------------------------------------------------

} // GuiTestRunner
