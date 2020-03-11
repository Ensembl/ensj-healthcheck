/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ensembl.healthcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Subclass of TestRunner that produces a web page containing descriptions of available tests.
 */
public class BuildTestLibrary extends TestRunner {

    private String template = "";

    // -------------------------------------------------------------------------
    /**
     * Command-line run method.
     * 
     * @param args
     *          The command-line arguments.
     */
    public static void main(final String[] args) {

        BuildTestLibrary btl = new BuildTestLibrary();

        btl.logger.setLevel(Level.OFF);

        btl.parseCommandLine(args);

        Utils.readPropertiesFileIntoSystem(getPropertiesFile(), false);

        btl.buildList();

    } // main

    // -------------------------------------------------------------------------

    private void parseCommandLine(final String[] args) {

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        if (args[0].equals("-h") || args.length == 0) {

            printUsage();
            System.exit(0);

        } else {

            template = args[0];

        }

    } // parseCommandLine

    // -------------------------------------------------------------------------

    private void printUsage() {

        System.out.println("\nUsage: BuildTestLibrary <template>\n");
        System.out.println("Options:");
        System.out.println(" None.");
        System.out.println("");
        System.out.println("Expects template file to exist and contain #TEST_TABLE# and #TIMESTAMP# placeholders.");
        System.out.println("Writes to test_list.html in same directory as template.");

    } // printUsage

    // -------------------------------------------------------------------------

    private void buildList() {

        try {

            // get the directory
            String dir = template.substring(0, template.lastIndexOf(File.separator));
            String outputFile = dir + File.separator + "test_list.html";
            PrintWriter out = new PrintWriter(new FileWriter(outputFile));

            // parse the template until we find the #TEST_TABLE# placeholder
            BufferedReader in = new BufferedReader(new FileReader(template));
            String line = in.readLine();
            while (line != null) {

                if (line.indexOf("#TEST_TABLE#") > -1) {
                    out.println(listAllTestsAsHTMLTable());

                } else if (line.indexOf("#TESTS_BY_GROUP#") > -1) {
                    out.println(listTestsByGroup());

                } else if (line.indexOf("#TIMESTAMP#") > -1) {
                    out.println(new Date());

                } else {
                    out.println(line);

                }

                line = in.readLine();

            }

            System.out.println("Wrote output to " + outputFile);
            in.close();
            out.flush();
            out.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // -------------------------------------------------------------------------

    private String listAllTestsAsHTMLTable() {

        StringBuffer buf = new StringBuffer();

        List tests = new DiscoveryBasedTestRegistry().findAllTests();

        Iterator it = tests.iterator();
        while (it.hasNext()) {

            // test name
            EnsTestCase test = (EnsTestCase) it.next();
            buf.append("<tr>");
            buf.append("<td class=\"lfooter\"><strong>" + test.getShortTestName() + "</strong></td>");

            // group(s)
            List groups = test.getGroups();
            Iterator gIt = groups.iterator();
            buf.append("<td>");
            while (gIt.hasNext()) {
                String group = (String) gIt.next();
                if (!group.equals(test.getShortTestName()) && !group.equals("all")) {
                    buf.append(group + "<br>");
                }
            }
            buf.append("</td>");

            // description
            buf.append("<td><font size=-1>" + test.getDescription() + "</font></td>");

            buf.append("</tr>\n");

        } // while tests

        return buf.toString();

    } // listAllTestsAsHTMLTable

    // -------------------------------------------------------------------------

    private String listTestsByGroup() {

        StringBuffer buf = new StringBuffer();

        List<EnsTestCase> allTests = new DiscoveryBasedTestRegistry().findAllTests();
        String[] groups = listAllGroups(allTests);

        for(String group: groups) {

            if (!group.equalsIgnoreCase("all") && group.indexOf("TestCase") < 0) {
                buf.append("<p><strong>" + group + "</strong></p>");
                String[] tests = listTestsInGroup(allTests, group);

                for(String t: tests) {
                    buf.append(t + "<br>");
                } // tests

            }

        } // groups

        return buf.toString();

    }

    // -------------------------------------------------------------------------

} // BuildTestLibrary
