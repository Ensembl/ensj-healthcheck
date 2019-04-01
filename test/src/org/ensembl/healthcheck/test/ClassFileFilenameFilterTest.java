/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
