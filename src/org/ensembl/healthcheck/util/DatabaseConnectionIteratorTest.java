/*
 * DatabaseConnectionIteratorTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 10:01 AM
 */

package org.ensembl.healthcheck.util;

import junit.framework.*;
import java.util.*;
import java.util.logging.*;
import java.sql.*;

/**
 * @version $Revision$
 * @author glenn
 */
public class DatabaseConnectionIteratorTest extends TestCase {
  
  private DatabaseConnectionIterator dbci;
  
  public DatabaseConnectionIteratorTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(DatabaseConnectionIteratorTest.class);
    
    return suite;
  }
  
  public void setUp() {
    String[] dbNames = {"homo_sapiens_core_10_30", "home_sapiens_core_11_31"};
    
    dbci = new DatabaseConnectionIterator("org.gjt.mm.mysql.Driver",
                                          "jdbc:mysql://kaka.sanger.ac.uk/",
					  "anonymous", "",
					  dbNames);
  }
  
  /** Test of hasNext method, of class org.ensembl.healthcheck.util.DatabaseConnectionIterator. */
  public void testHasNext() {
    
    assertTrue(dbci.hasNext());
    
  }
  
  /** Test of next method, of class org.ensembl.healthcheck.util.DatabaseConnectionIterator. */
  public void testNext() {
    System.out.println("testNext");
    
    try {
      dbci.next();
    } catch (Exception e) {
      fail("next() failed:\n\t" + e.getMessage());
    }
  }
  
  /** Test of getCurrentDatabaseName method, of class org.ensembl.healthcheck.util.DatabaseConnectionIterator. */
  public void testGetCurrentDatabaseName() {
    System.out.println("testGetCurrentDatabaseName");
    
    dbci.next();
    assertEquals(dbci.getCurrentDatabaseName(), "homo_sapiens_core_10_30");
  }
  
  /** Test of remove method, of class org.ensembl.healthcheck.util.DatabaseConnectionIterator. */
  public void testRemove() {
    // remove is not implemented
  }
  
}
