package org.ensembl.healthcheck.test;

import org.ensembl.healthcheck.util.DBUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DBUtilsTest {

  @Test
  public void testGenerateTempDatabaseName() {
    String dbName = DBUtils.generateTempDatabaseName();
    Assert.assertNotNull(dbName);
  }

}
