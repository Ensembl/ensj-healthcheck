/*
 * UtilsTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 9:48 AM
 */

package org.ensembl.healthcheck.util.test;

import org.ensembl.healthcheck.util.*;

import junit.framework.*;
import java.io.*;
import java.util.*;

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
    // @todo - create a properties file here, read it in test, then remove in tearDown?
  }
  
  /** Test of readPropertiesFile method, of class org.ensembl.healthcheck.util.Utils. */
  public void testReadPropertiesFile() {
    System.out.println("testReadPropertiesFile");
    
    assertNotNull(Utils.readPropertiesFile("database.properties"));
  }
  
  public void tearDown() {
    
  }
  
  
  
}
