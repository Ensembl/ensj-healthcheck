/*
 * LogFormatterTest.java
 * NetBeans JUnit based test
 *
 * Created on March 13, 2003, 9:15 AM
 */

package org.ensembl.healthcheck.util;

import junit.framework.*;
import java.util.logging.*;
import java.util.*;

/**
 *
 * @author glenn
 */
public class LogFormatterTest extends TestCase {
  
  public LogFormatterTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(LogFormatterTest.class);
    
    return suite;
  }
  
  /** Test of format method, of class org.ensembl.healthcheck.util.LogFormatter. */
  public void testFormat() {
    
    LogFormatter lf = new LogFormatter();
    LogRecord lr = new LogRecord(Level.INFO, "a test message");
    assertEquals(lf.format(lr), "INFO: a test message\n");
  
  }
  
  // Add test methods here, they have to start with 'test' name.
  // for example:
  // public void testHello() {}
  
  
  
}
