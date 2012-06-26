

package org.ensembl.healthcheck.test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ensembl.healthcheck.util.LogFormatter;
import org.ensembl.healthcheck.util.MyStreamHandler;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @version $Revision$
 * @author glenn
 */
public class MyStreamHandlerTest {
  
  private MyStreamHandler msh;
  private LogRecord lr;

  @BeforeTest
  public void setUp() {
    msh = new MyStreamHandler(System.out, new LogFormatter());
    lr = new LogRecord(Level.INFO, "message");
  }
  
  /** Test of close method, of class org.ensembl.healthcheck.util.MyStreamHandler. */
//  @Test
//  public void testClose() {
////    System.out.println("testClose");
//    
//    msh.close(); // ?? how to test this ??
//  }
  
  /** Test of flush method, of class org.ensembl.healthcheck.util.MyStreamHandler. */
  @Test
  public void testFlush() {
//    System.out.println("testFlush");

    msh.flush();  // ?? how to test this ??
    
  }
  
  /** Test of publish method, of class org.ensembl.healthcheck.util.MyStreamHandler. */
  @Test
  public void testPublish() {
//    System.out.println("testPublish");
    
    msh.publish(lr);  // ?? how to test this ??
    
  }
}
