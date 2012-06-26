
package org.ensembl.healthcheck.test;

import java.io.File;

import org.ensembl.healthcheck.util.ClassFileFilenameFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ClassFileFilenameFilterTest {
  
  private ClassFileFilenameFilter cnff;
  private File file;
  
  @BeforeTest
  public void setUp() {
    cnff = new ClassFileFilenameFilter();
    file = new File("");
  }
  
  /** Test of accept method, of class org.ensembl.healthcheck.util.ClassFileFilenameFilter. */
  @Test
  public void testAccept() {
    System.out.println("testAccept");
    Assert.assertTrue(cnff.accept(file, "TestFile.class"));
    Assert.assertTrue(cnff.accept(file, "/work/ensjhealthcheck/org/ensembl/TestFile.class"));
    Assert.assertTrue(!cnff.accept(file, "TestFile.java"));
    Assert.assertTrue(!cnff.accept(file, "classes/file.xml"));

  }  
}
