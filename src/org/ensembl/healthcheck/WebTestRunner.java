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

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private boolean debug = false;

    private static final String CONFIG_FILE = "web.properties";

    private List outputBuffer = new ArrayList();

    long startTime;

    //---------------------------------------------------------------------
    /**
     * Main run method.
     * 
     * @param args
     *          Command-line arguments.
     */
    private void run(String[] args) {

        startTime = System.currentTimeMillis();

        ReportManager.setReporter(this);

        parseCommandLine(args);

        setupLogging();

        Utils.readPropertiesFileIntoSystem(PROPERTIES_FILE);

        Utils.readPropertiesFileIntoSystem(CONFIG_FILE);

        groupsToRun = getGroupsFromProperties();

        List databaseRegexps = getDatabasesFromProperties();

        outputLevel = setOutputLevelFromProperties();

        TestRegistry testRegistry = new TestRegistry();

        DatabaseRegistry databaseRegistry = new DatabaseRegistry(databaseRegexps);
        if (databaseRegistry.getAll().length == 0) {
            logger.warning("Warning: no database names matched any of the database regexps given");
        }

        runAllTests(databaseRegistry, testRegistry);

        printOutput();

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

    private void parseProperties() {

        if (System.getProperty("webtestrunner.groups") == null) {
            System.err.println("No tests or groups specified in " + CONFIG_FILE);
            System.exit(1);
        }

        if (System.getProperty("webtestrunner.databases") == null) {
            System.err.println("No databases specified in " + CONFIG_FILE);
            System.exit(1);
        }

        if (System.getProperty("webtestrunner.file") == null) {
            System.err.println("No output file specified in " + CONFIG_FILE);
            System.exit(1);
        }

        // TODO other properties

    }

    //---------------------------------------------------------------------

    /**
     * Get a list of test groups by parsing the appropriate property.
     * 
     * @return the list of group or test names.
     */
    private List getGroupsFromProperties() {

        String[] groups = System.getProperty("webtestrunner.groups").split(",");

        return Arrays.asList(groups);

    }

    //---------------------------------------------------------------------

    /**
     * Get a list of databases by parsing the appropriate property.
     * 
     * @return The list of database names or patterns.
     */
    private List getDatabasesFromProperties() {

        String[] dbs = System.getProperty("webtestrunner.databases").split(",");

        return Arrays.asList(dbs);

    }

    //  ---------------------------------------------------------------------

    private int setOutputLevelFromProperties() {

        String lstr = System.getProperty("webtestrunner.outputlevel").toLowerCase();

        if (lstr.equals("all")) {
            outputLevel = ReportLine.ALL;
        } else if (lstr.equals("none")) {
            outputLevel = ReportLine.NONE;
        } else if (lstr.equals("problem")) {
            outputLevel = ReportLine.PROBLEM;
        } else if (lstr.equals("correct")) {
            outputLevel = ReportLine.CORRECT;
        } else if (lstr.equals("warning")) {
            outputLevel = ReportLine.WARNING;
        } else if (lstr.equals("info")) {
            outputLevel = ReportLine.INFO;
        } else {
            System.err.println("Output level " + lstr + " not recognised; using 'all'");
        }

        return outputLevel;

    }

    //---------------------------------------------------------------------

    /**
     * Print formatted output held in outputBuffer to file specified in System property file.
     */
    private void printOutput() {

        String file = System.getProperty("webtestrunner.file");

        try {

            PrintWriter pw = new PrintWriter(new FileOutputStream(file));

            printHeader(pw);

            printSummaryByDatabase(pw);

            printSummaryByTest(pw);

            printReportsByDatabase(pw);

            printReportsByTest(pw);

            printFooter(pw);

            pw.close();

        } catch (Exception e) {
            System.err.println("Error writing to " + file);
            e.printStackTrace();
        }

    }

    //---------------------------------------------------------------------

    private void printHeader(PrintWriter pw) {

        print(pw, "<html>");
        print(pw, "<head>");
        print(pw, "<link REL=\"stylesheet\" HREF=\"http://www.ensembl.org/EnsEMBL.css\">");
        print(pw, "<title>Healthcheck Results</title>");
        print(pw, "</head>");
        print(pw, "<body>");

        print(pw, "<h1>Healthcheck Report</h1>");

    }

    //---------------------------------------------------------------------

    private void printReportsByDatabase(PrintWriter pw) {

        print(pw, "<h2>Detailed reports by database</h2>");

        Map reportsByDB = ReportManager.getAllReportsByDatabase();

        Set dbs = reportsByDB.keySet();
        Iterator it = dbs.iterator();
        while (it.hasNext()) {

            String database = (String) it.next();
            String link = "<a name=\"" + database + "\">";
            print(pw, "<h3>" + link + database + "</a></h3>");

            List reports = (List) reportsByDB.get(database);
            Iterator it2 = reports.iterator();
            String lastTest = "";
            while (it2.hasNext()) {

                ReportLine line = (ReportLine) it2.next();
                String test = line.getShortTestCaseName();
                
                if (!lastTest.equals("") && !test.equals(lastTest)) {
                    print(pw, "<p>");
                }
                lastTest = test;
                
                String linkTarget = "<a name=\"" + database + ":" + test + "\"></a> ";
                String s = linkTarget + getFontForReport(line) + "<strong>" + test + ": </strong>" + line.getMessage()
                        + "</font>" + "<br>";

                print(pw, s);

               

            } // while it2

        } // while it

        print(pw, "<hr>");

    }

    //---------------------------------------------------------------------
    private void printReportsByTest(PrintWriter pw) {

        print(pw, "<h2>Detailed reports by test case</h2>");

        Map reportsByTC = ReportManager.getAllReportsByTestCase();
        Set dbs = reportsByTC.keySet();
        Iterator it = dbs.iterator();
        while (it.hasNext()) {

            String test = (String) it.next();
            String link = "<a name=\"" + test + "\">";
            print(pw, "<h3>" + link + test + "</a></h3>");

            List reports = (List) reportsByTC.get(test);
            Iterator it2 = reports.iterator();
            String lastDB = "";
            while (it2.hasNext()) {

                ReportLine line = (ReportLine) it2.next();
                String database = line.getDatabaseName();
                
                if (!lastDB.equals("") && !database.equals(lastDB)) {
                    print(pw, "<p>");
                }
                lastDB = database;
                
                String linkTarget = "<a name=\"" + line.getShortTestCaseName() + ":" + database + "\"></a> ";
                String s = linkTarget + getFontForReport(line) + "<strong>" + database + ": </strong>"
                        + line.getMessage() + "</font>" + "<br>";

                print(pw, s);

            } // while it2
        } // while it

        print(pw, "<hr>");
    }

    //---------------------------------------------------------------------

    private String getFontForReport(ReportLine line) {

        String s1 = "";

        switch (line.getLevel()) {
        case (ReportLine.PROBLEM):
            s1 = "<font color='red'>";
            break;
        case (ReportLine.WARNING):
            s1 = "<font color='black'>";
            break;
        case (ReportLine.INFO):
            s1 = "<font color='grey'>";
            break;
        case (ReportLine.CORRECT):
            s1 = "<font color='green'>";
            break;
        default:
            s1 = "<font color='black'>";
        }

        return s1;

    }

    //---------------------------------------------------------------------

    private void printFooter(PrintWriter pw) {

        long runTime = System.currentTimeMillis() - startTime;
        String runStr = Utils.formatTimeString(runTime);
        print(pw, "<p>Test run was started at " + new Date(startTime).toString() + " and finished at "
                + new Date().toString() + "<br>");
        print(pw, " Run time " + runStr + "</p>");

        print(pw, "</body>");
        print(pw, "</html>");

        print(pw, "<hr>");

    }

    //---------------------------------------------------------------------

    private void print(PrintWriter pw, String s) {

        pw.write(s + "\n");

    }

    //---------------------------------------------------------------------

    private void printSummaryByDatabase(PrintWriter pw) {

        print(pw, "<h2>Summary by database</h2>");

        print(pw, "<table cellpadding='15' border='1'>");
        print(pw, "<tr><th>Database</th><th>Passed</th><th>Failed</th></tr>");

        Map reportsByDB = ReportManager.getAllReportsByDatabase();
        Set databases = reportsByDB.keySet();
        Iterator it = databases.iterator();
        while (it.hasNext()) {
            String database = (String) it.next();
            String link = "<a href=\"#" + database + "\">";
            int[] passesAndFails = ReportManager.countPassesAndFailsDatabase(database);
            String s = (passesAndFails[1] == 0) ? passFont() : failFont();
            String[] t = {link + "<strong>" + s + database + "</font><strong></a>",
                    passFont() + passesAndFails[0] + "</font>", failFont() + passesAndFails[1] + "</font>"};
            printTableLine(pw, t);
        }

        print(pw, "</table>");

        print(pw, "<hr>");

    }

    //  ---------------------------------------------------------------------

    private void printSummaryByTest(PrintWriter pw) {

        print(pw, "<h2>Summary by test</h2>");

        print(pw, "<table cellpadding=\"10\" border=\"1\">");
        print(pw, "<tr><th>Test</th><th>Passed</th><th>Failed</th></tr>");

        Map reports = ReportManager.getAllReportsByTestCase();
        Set tests = reports.keySet();
        Iterator it = tests.iterator();
        while (it.hasNext()) {
            String test = (String) it.next();
            String link = "<a href=\"#" + test + "\">";
            int[] passesAndFails = ReportManager.countPassesAndFailsTest(test);
            String s = (passesAndFails[1] == 0) ? passFont() : failFont();
            String[] t = {link + "<strong>" + s + test + "</font><strong></a>",
                    passFont() + passesAndFails[0] + "</font>", failFont() + passesAndFails[1] + "</font>"};
            printTableLine(pw, t);
        }

        print(pw, "</table>");

        print(pw, "<hr>");

    }

    //---------------------------------------------------------------------

    private void printTableLine(PrintWriter pw, String[] s) {

        pw.write("<tr>");

        for (int i = 0; i < s.length; i++) {
            pw.write("<td>" + s[i] + "</td>");
        }
        pw.write("</tr>\n");

    }

    //---------------------------------------------------------------------

    private String passFont() {

        return "<font color='green'>";

    }

    //---------------------------------------------------------------------

    private String failFont() {

        return "<font color='red'>";

    }
    //---------------------------------------------------------------------

} // WebTestRunner
