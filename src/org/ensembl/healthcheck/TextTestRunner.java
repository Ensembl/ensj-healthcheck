/*
  Copyright (C) 2003 EBI, GRL

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

import org.ensembl.healthcheck.util.*;

import java.io.*;

import java.sql.*;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Subclass of TestRunner intended for running tests from the command line.
 */
public class TextTestRunner extends TestRunner {

  private static String version = "$Id$";
  private boolean forceDatabases = false;
  private boolean debug = false;

  // -------------------------------------------------------------------------

  /**
   * Command-line run method.
   * @param args The command-line arguments.
   */
  public static void main(String[] args) {

    TextTestRunner ttr = new TextTestRunner();

    System.out.println(ttr.getVersion());

    ttr.parseCommandLine(args);

    ttr.setupLogging();

    ttr.readPropertiesFile();

    ttr.runAllTests(ttr.findAllTests(), ttr.forceDatabases);

    ConnectionPool.closeAll();

    ttr.printReportsByTest(ttr.outputLevel);

    ttr.printReportsByDatabase(ttr.outputLevel);

  }

  // main
  // -------------------------------------------------------------------------
  private void printUsage() {

    System.out.println("\nUsage: TextTestRunner {-d regexp} {-force} {-output none|problem|correct|summary|info|all} {group1} {group2} ...\n");
    System.out.println("Options:");
    System.out.println("  -d regexp       Use the given regular expression to decide which databases to use.");
    System.out.println("  -force          Run the named tests on the databases matched by -d, without ");
    System.out.println("                  taking into account the regular expressions built into the tests themselves.");
    System.out.println("  -h              This message.");
    System.out.println("  -output level   Set output level; level can be one of ");
    System.out.println("                  none      nothing is printed");
    System.out.println("                  problem   only problems are reported");
    System.out.println("                  correct   only correct results (and problems) are reported");
    System.out.println("                  summary   only summary info (and problems, and correct reports) are reported");
    System.out.println("                  info      info (and problem, correct, summary) messages reported");
    System.out.println("                  all       everything is printed");
    System.out.println("  -config file    Read config from file (in ensj-healthcheck dir) rather than database.properties");
    System.out.println("  -debug          Print debugging info (for developers only)");
    System.out.println("  group1          Names of groups of test cases to run.");
    System.out.println("                  Note each test case is in a group of its own with the name of the test case.");
    System.out.println("                  This allows individual tests to be run if required.");
    System.out.println("");

  }
  
  /**
   * Return the CVS version string for this class.
   * @return The version.
   */
  public String getVersion() {

    // strip off first and last few chars of version since these are only used by CVS
    return version.substring(5, version.length() - 2);

  }

  // -------------------------------------------------------------------------
  private void parseCommandLine(String[] args) {

    if (args.length == 0) {

      printUsage();
      System.exit(1);

    } else {

      for (int i = 0; i < args.length; i++) {

        if (args[i].equals("-h")) {

          printUsage();
          System.exit(0);

        } else if (args[i].equals("-output")) {

          setOutputLevel(args[++i]);
          System.out.println("Set output level to " + outputLevel);

        } else if (args[i].equals("-debug")) {

          debug = true;

        } else if (args[i].equals("-d")) {

          i++;
          preFilterRegexp = args[i];
          System.out.println("Will pre-filter database names on " + preFilterRegexp);

        } else if (args[i].equals("-force")) {

          forceDatabases = true;
          System.out.println("Will use ONLY databases specified by -d");

        } else if (args[i].equals("-config")) {

          i++;
          propertiesFileName = args[i];
          System.out.println("Will read properties from " + propertiesFileName);

        } else {

          groupsToRun.add(args[i]);
          System.out.println("Will run tests in group " + args[i]);

        }

      }

      if (forceDatabases && (preFilterRegexp == null)) {

        System.err.println("You have requested -force but not specified a database name regular expression with -d");
        System.exit(1);

      }

    }

  }

  // parseCommandLine
  // -------------------------------------------------------------------------
  private void setupLogging() {

    logger.setUseParentHandlers(false);// stop parent logger getting the message

    Handler myHandler = new MyStreamHandler(System.out, new LogFormatter());

    logger.addHandler(myHandler);
    logger.setLevel(Level.WARNING);// default - only print important messages

    if (debug) {

      logger.setLevel(Level.FINEST);

    }

    //logger.info("Set logging level to " + logger.getLevel().getName());
  }

  // setupLogging
  // -------------------------------------------------------------------------
}


// TextTestRunner
