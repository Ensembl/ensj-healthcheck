/*
 Copyright (C) 2002 EBI, GRL

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

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.ensembl.healthcheck.util.Utils;

/**
 * TestRunner optimiesed for outputting results to HTML.
 */
public class WebTestRunner extends TestRunner implements Reporter {

    private boolean debug = true;

    private static final String CONFIG_FILE = "web.properties";
    
    //---------------------------------------------------------------------
    /**
     * Main run method.
     * 
     * @param args
     *          Command-line arguments.
     */
    private void run(String[] args) {

        ReportManager.setReporter(this);
        
        parseCommandLine(args);

        setupLogging();
        
        Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

        Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

        groupsToRun = getGroupsFromProperties();

        List databaseRegexps = getDatabasesFromProperties();
        
        TestRegistry testRegistry = new TestRegistry();
        
        DatabaseRegistry databaseRegistry = new DatabaseRegistry(databaseRegexps);
        if (databaseRegistry.getAll().length == 0) {
            logger.warning("Warning: no database names matched any of the database regexps given");
        }

        runAllTests(databaseRegistry, testRegistry);

        // TODO - output

        ConnectionPool.closeAll();

    } // run

    //---------------------------------------------------------------------
    
    /**
     * Command-line entry point.
     * 
     * @param args
     *          Command line args.
     */
    public static void main(String[] args) {

        new WebTestRunner().run(args);

    } // main
    //  -------------------------------------------------------------------------

    private void parseCommandLine(String[] args) {

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-h")) {

                printUsage();
                System.exit(0);

            } else if (args[i].equals("-debug")) {

                debug = true;
                logger.finest("Running in debug mode");

            }
        }

    } // parseCommandLine

    // -------------------------------------------------------------------------

    private void printUsage() {

        System.out.println("\nUsage: WebTestRunner {options} \n");
        System.out.println("Options:");
        System.out.println("  -h              This message.");
        System.out.println("  -debug          Print debugging info");

        // TODO - config file format

    }

    //---------------------------------------------------------------------

    private void setupLogging() {

        // stop parent logger getting the message
        logger.setUseParentHandlers(false);

        Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());

        logger.addHandler(myHandler);
        logger.setLevel(Level.WARNING); 

        if (debug) {

            logger.setLevel(Level.FINEST);

        }

    } // setupLogging

    //  -------------------------------------------------------------------------
    // Implementation of Reporter interface

    /**
     * Called when a message is to be stored in the report manager.
     * 
     * @param reportLine
     *          The message to store.
     */
    public void message(ReportLine reportLine) {

        // TODO - store or add to outputBuffer or something

    }

    //---------------------------------------------------------------------
    
    /**
     * Called just before a test case is run.
     * 
     * @param testCase
     *          The test case about to be run.
     * @param dbre
     *          The database which testCase is to be run on, or null of no/several databases.
     */
    public void startTestCase(EnsTestCase testCase, DatabaseRegistryEntry dbre) {

        // TODO - implement

    }

    //---------------------------------------------------------------------
    
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

    }
    
    //---------------------------------------------------------------------
    
    /**
     * Get a list of test groups by parsing the appropriate property.
     * @return the list of group or test names.
     */
    private List getGroupsFromProperties() {
        
        String val = System.getProperty("groups");
    
        return null; // TODO implement
        
    }
    
    //---------------------------------------------------------------------
    
    /**
     * Get a list of databases by parsing the appropriate property.
     * @return The list of database names or patterns.
     */
    private List getDatabasesFromProperties() {
        
        String val = System.getProperty("databases");
    
        return null; // TODO implement
        
    }
    
    //---------------------------------------------------------------------

} // WebTestRunner
