/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
