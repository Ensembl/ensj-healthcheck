package org.ensembl.healthcheck.test;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.ensembl.healthcheck.util.*;

public class TestDBConnection {

  public TestDBConnection() {

    Connection conn;

    Properties dbProps = Utils.readPropertiesFile("/homes/glenn/database.properties");

    // open connection
    try {

      conn = DBUtils.openConnection(dbProps.getProperty("driver",      "org.gjt.mm.mysql.Driver"),
                                    dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk"),
                                    dbProps.getProperty("user",        "anonymous"),
                                    dbProps.getProperty("password",    ""));

      System.out.println("Opened connection to " + dbProps.getProperty("databaseURL", "kaka.sanger.ac.uk") + " as " + dbProps.getProperty("user", "anonymous"));

      // List DBs
      System.out.println("Listing databases matching ^homo.* :\n");

      String[] databaseNames = DBUtils.listDatabases(conn, "^homo.*");

      if (databaseNames.length == 0) {
        System.err.println("No database names matched");
      } else {
        for (int i = 0; i < databaseNames.length; i++) {
          System.out.println(databaseNames[i]);
        }
      }

      conn.close();

      System.out.println("\nConnection closed");

    } catch (Exception e) {

      e.printStackTrace();
      System.exit(1);

    }

  } // TestConnection

  public static void main(String[] args) {

    TestDBConnection testDBConnection1 = new TestDBConnection();

  } // main


} // TestDBConnection