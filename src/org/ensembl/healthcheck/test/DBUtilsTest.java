
package org.ensembl.healthcheck.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.healthcheck.util.DBUtils;

/**
 * @version $Revision$
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
  
  
  public void testGenerateTempDatabaseName() {
    
    String dbName = DBUtils.generateTempDatabaseName();
    assertNotNull(dbName);
    
      
  }
  
  
  
}
