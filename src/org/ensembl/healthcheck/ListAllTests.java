/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Subclass of TestRunner that lists all tests.
 */
public class ListAllTests extends TestRunner {

    private String groupToList = "";

    private boolean showGroups = false;

    private boolean showDesc = false;

    private static boolean showGroupsOnly = false;

    private boolean listAllGroups = false;

    // -------------------------------------------------------------------------
    /**
     * Command-line run method.
     * 
     * @param args
     *          The command-line arguments.
     */
    public static void main(String[] args) {

        ListAllTests lat = new ListAllTests();

        logger.setLevel(Level.OFF);

        lat.parseCommandLine(args);

        Utils.readPropertiesFileIntoSystem("database.defaults.properties", false);

        if (showGroupsOnly) {

            lat.showAllTestsByGroup();

        } else {

            lat.listAllTests();

        }
    } // main

    // -------------------------------------------------------------------------

    private void parseCommandLine(String[] args) {

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-h")) {

                printUsage();
                System.exit(0);

            }

            if (args[i].equals("-g")) {

                showGroups = true;

            } else if (args[i].equals("-d")) {

                showDesc = true;

            } else if (args[i].equals("-groups")) {

                showGroupsOnly = true;

            } else {

                groupToList = args[i];

            }
        }

        if (groupToList.equals("")) {

            listAllGroups = true;

        }

    } // parseCommandLine

    // -------------------------------------------------------------------------

    private void printUsage() {

        System.out.println("\nUsage: ListTests -{h, g, d} group1 ... \n");
        System.out.println("  or:    ListTests -groups\n");
        System.out.println("Options:");
        System.out.println("  -h         This message.");
        System.out.println("  -g         Show groups associated with each test.");
        System.out.println("  -d         Show test description.");
        System.out.println("  -groups    Shows a list of the test groups and which tests are in each");
        System.out.println("  group1     List tests that are members of group1.");
        System.out.println("");
        System.out.println("If no groups are specified all available tests are listed");

    } // printUsage

    // -------------------------------------------------------------------------

    private void listAllTests() {

        if (listAllGroups) {
            System.out.println("All available tests:");
        } else {
            System.out.println("Tests in group " + groupToList + ":");
        }

        List tests = new DiscoveryBasedTestRegistry().findAllTests();

        Iterator it = tests.iterator();
        while (it.hasNext()) {
            EnsTestCase test = (EnsTestCase) it.next();
            if (listAllGroups || test.inGroup(groupToList) || test.getShortTestName().toLowerCase().equals(groupToList.toLowerCase())) {
                StringBuffer testline = new StringBuffer(test.getShortTestName());

                if (test.canRepair()) {
                    testline.append(" [ can repair] ");
                }

                if (showGroups) {
                    testline.append(" (");
                    List groups = test.getGroups();
                    java.util.Iterator gIt = groups.iterator();
                    while (gIt.hasNext()) {
                        String groupname = (String) gIt.next();
                        if (!groupname.equals(test.getShortTestName())) {
                            testline.append(groupname);
                            testline.append(",");
                        }
                    }
                    if (testline.charAt(testline.length() - 1) == ',') {
                        testline.deleteCharAt(testline.length() - 1);
                    }

                    testline.append(")");

                }

                if (showDesc) {
                    testline.append("\n" + test.getDescription() + "\n");
                }

                System.out.println(testline.toString());
            }

        }

    } // listAllTests

    // -------------------------------------------------------------------------

    private void showAllTestsByGroup() {

        List allTests = new DiscoveryBasedTestRegistry().findAllTests();
        String[] groups = listAllGroups(allTests);
        Arrays.sort(groups);

        // each test is technically a member of the group with the same name as
        // the test; this isn't interesting here so these are filtered out

        for (int i = 0; i < groups.length; i++) {

            String group = groups[i];

            // assume that tests have mixed case and group names are all lower
            // case
            if (group.equals(group.toLowerCase())) {

                StringBuffer buf = new StringBuffer();

                buf.append(group + ":\n");
                String[] tests = listTestsInGroup(allTests, group);
                Arrays.sort(tests);

                for (int j = 0; j < tests.length; j++) {

                    if (!tests[j].equals(group)) {
                        buf.append("\t" + tests[j] + "\n");
                    }

                } // tests

                System.out.println(buf.toString());

            }

        } // groups

    }
    // -------------------------------------------------------------------------

} // ListAllTests
