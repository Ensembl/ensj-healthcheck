/*
 * DBUtilsTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 9:52 AM
 */

package org.ensembl.healthcheck.util;

import junit.framework.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
 *
 * @author glenn
 */
public class DBUtilsTest extends TestCase {
  
  
  public DBUtilsTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(DBUtilsTest.class);
    
    return suite;
  }
  
  /** Test of openConnection method, of class org.ensembl.healthcheck.util.DBUtils. */
  public void testOpenConnection() {
    System.out.println("testOpenConnection");
    
    Connection con = DBUtils.openConnection("org.gjt.mm.mysql.Driver",
                                            "jdbc:mysql://kaka.sanger.ac.uk/",
					    "anonymous", "");
    assertNotNull(con);
    try {
      con.close();
    } catch (Exception e) {
      fail("Failure closing connection:\n\t" + e.getMessage());
    }
  }
  
  /** Test of listDatabases method, of class org.ensembl.healthcheck.util.DBUtils. */
  public void testListDatabases() {
    
    // ----------
    System.out.println("testListDatabases - all");
    Connection con = DBUtils.openConnection("org.gjt.mm.mysql.Driver",
                                            "jdbc:mysql://kaka.sanger.ac.uk/",
					    "anonymous", "");
    assertNotNull(con);
    
    String [] dbs = DBUtils.listDatabases(con);
    assertNotNull(dbs);
    
    // ----------
    System.out.println("testListDatabases - regexp ^homo.*");

    dbs = DBUtils.listDatabases(con, "^*homo.*");
    assertNotNull(dbs);
    
    // ----------

    try {
      con.close();
    } catch (Exception e) {
      fail("Failure closing connection:\n\t" + e.getMessage());
    }
    
  }
  
  // Add test methods here, they have to start with 'test' name.
  // for example:
  // public void testHello() {}
  
  
  
}
