/*
 * TestResultTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 11:41 AM
 */

package org.ensembl.healthcheck.test;

import junit.framework.*;

import org.ensembl.healthcheck.*;

/**
 *
 * @author glenn
 */
public class TestResultTest extends TestCase {
  
  private org.ensembl.healthcheck.TestResult tr1, tr2;
  
  public TestResultTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(TestResultTest.class);
    
    return suite;
  }
  
  //
  public void setUp() {
    
    tr1 = new org.ensembl.healthcheck.TestResult("True no message",  true);
    tr2 = new org.ensembl.healthcheck.TestResult("False no message", false);
    
  }
  
  /** Test of setResult method, of class org.ensembl.healthcheck.TestResult. */
  public void testSetResult() {
    System.out.println("testSetResult");
    
    tr1.setResult(false);
    assertTrue(!tr1.getResult());
    
  }
  
  /** Test of getResult method, of class org.ensembl.healthcheck.TestResult. */
  public void testGetResult() {
    System.out.println("testGetResult");
    
    assertTrue(!tr2.getResult());
    
  }
  
  /** Test of getName method, of class org.ensembl.healthcheck.TestResult. */
  public void testGetName() {
    System.out.println("testGetName");
    
    assertEquals(tr1.getName(), "True no message");
    
  }
  
  /** Test of setName method, of class org.ensembl.healthcheck.TestResult. */
  public void testSetName() {
    
    tr2.setName("new name");
    assertEquals(tr2.getName(), "new name");
    
  }
  
}
