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

/**
 * <p>Title: RunCheck.java</p>
 * <p>Description: Main control class for running healthchecks.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

import java.util.*;
import java.io.*;
import java.sql.*;
import java.util.logging.*;

import org.ensembl.healthcheck.util.*;

public class RunCheck {

  private String dbRegexp = "";
  private String[] dbNames;
  private ArrayList suites;

  private static Logger logger = Logger.getLogger("org.ensembl.healthcheck.RunCheck");

  private Properties dbProps;

  public RunCheck() {
  }

  public static void main(String[] args) {

    // set up logging
    // remove any existing handlers
    /*
    Handler[] handlers = logger.getHandlers();
    for (int i = 0; i < handlers.length; i++) {
      System.out.println(i);
      logger.removeHandler(handlers[i]);
    }
    logger.addHandler(new StreamHandler(System.out, new LogFormatter()));
*/

    RunCheck rc = new RunCheck();

    rc.parseCommandLine(args);

    rc.init();

    rc.iterateOverDatabases();

    rc.finish();

  } // main

  private void parseCommandLine(String[] args) {

    ArrayList suites = new ArrayList();

    if (args.length < 2) {

      printUsage();
      System.exit(1);

    } else {

      dbRegexp = args[0];
      for (int i = 1; i < args.length; i++) {
        suites.add(args[i]);
      }


    }

    logger.info("dbRegexp: " + dbRegexp);
    for (Iterator i = suites.iterator(); i.hasNext(); ) {
      String suite = (String)i.next();
      logger.info("Suite: " + suite + " ");
    }

  } // parseCommandLine

  private void printUsage() {

    System.out.println("\nUsage: RunCheck [database regexp] [test-suite] {test-suite} ...\n");

  } // printUsage

  private String[] getDatabaseList(String regexp) {

    Connection conn;

    String[] databaseNames = null;

    // open connection
    try {

      conn = DBUtils.openConnection(dbProps.getProperty("driver",      "org.gjt.mm.mysql.Driver"),
                                    dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk"),
                                    dbProps.getProperty("user",        "anonymous"),
                                    dbProps.getProperty("password",    ""));

      System.out.println("Opened connection to " + dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk") + " as " + dbProps.getProperty("user", "anonymous"));

      System.out.println("Listing databases matching " + regexp + " :\n");

      databaseNames = DBUtils.listDatabases(conn, regexp);

      if (databaseNames.length == 0) {
        logger.warning("No database names matched");
      } else {
        for (int i = 0; i < databaseNames.length; i++) {
          System.out.println(databaseNames[i]);
        }
      }

      conn.close();

      //System.out.println("\nConnection closed");

    } catch (Exception e) {

      e.printStackTrace();
      System.exit(1);

    }

    return databaseNames;

  } // getDatabaseList

  private void init() {

    String propsFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "database.properties";
    logger.info("Reading database properties from " + propsFile);
    dbProps = Utils.readPropertiesFile(propsFile);

    dbNames = getDatabaseList(dbRegexp);

  } // init

  private void iterateOverDatabases() {

    System.out.println("\nRunning tests\n");
    // foreach database, open a connection, run the tests, and close the connection
    for (int i = 0; i < dbNames.length; i++) {
      String databaseName = dbNames[i];
      runTests(databaseName);
    }

  } // iterateOverDatabases

  private void runTests(String databaseName) {

    Connection conn;

    System.out.println("\tRunning tests on " + databaseName);

    // open DB connection
    try {

      String fullDBName = dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk") + databaseName;
      conn = DBUtils.openConnection(dbProps.getProperty("driver",      "org.gjt.mm.mysql.Driver"),
                                    fullDBName,
                                    dbProps.getProperty("user",        "anonymous"),
                                    dbProps.getProperty("password",    ""));

      conn.close();

    } catch (Exception e) {

      e.printStackTrace();

    }

  } // runTests

  private void finish() {



  } // finish


} // RunCheck