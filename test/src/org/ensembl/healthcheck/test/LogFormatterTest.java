package org.ensembl.healthcheck.test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ensembl.healthcheck.util.LogFormatter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LogFormatterTest {

  /** Test of format method, of class org.ensembl.healthcheck.util.LogFormatter. */
  @Test
  public void testFormat() {
    LogFormatter lf = new LogFormatter();
    LogRecord lr = new LogRecord(Level.INFO, "a test message");
    String msg = lf.format(lr);
    Assert.assertEquals(msg, "a test message\n", "Checking formatting");
  }
}
