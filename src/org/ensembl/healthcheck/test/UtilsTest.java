/*
 * UtilsTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 9:48 AM
 */

package org.ensembl.healthcheck.test;

import org.ensembl.healthcheck.util.*;

import junit.framework.*;

/**
 * @version $Revision$
 * @author glenn
 */
public class UtilsTest extends TestCase {
  
  public UtilsTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(UtilsTest.class);
    
    return suite;
  }
  
  public void setUp() {
  }
  
  /** Test of readPropertiesFile method, of class org.ensembl.healthcheck.util.Utils. */
  public void testReadPropertiesFile() {
    System.out.println("testReadPropertiesFile");
    
    assertNotNull(Utils.readPropertiesFile("database.properties"));
  }
  
  public void tearDown() {
    
  }
  
  
  
}
