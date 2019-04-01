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


package org.ensembl.healthcheck.util;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Custom stream handler for logging. Just prints to an OutputStream.
 */

public class MyStreamHandler extends Handler {
  
  private OutputStream outputStream;
  
  // ------------------------------------------------------------------------
  /**
   * Creates a new instance of MyStreamHandler
   * @param os The output stream to use, e.g. System.out
   * @param formatter The formatter object to use.
   */
  public MyStreamHandler(OutputStream os, Formatter formatter) {
    this.outputStream = os;
    setFormatter(formatter);
  }
  
  // -------------------------------------------------------------------------
  /** 
   * Close the Handler. Currently a no-op.
   */
  public void close() {
  }
  
  // -------------------------------------------------------------------------
  /**
   * Flush the output stream buffer.
   */
  public void flush() {
    try {
      outputStream.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  // -------------------------------------------------------------------------
  /**
   * Implementation of the Handler interface publish() method. Called each time a log record is produced.
   * @param logRecord The logRecord to deal with.
   */
  public void publish(LogRecord logRecord) {
    try {
      outputStream.write((getFormatter().format(logRecord)).getBytes());
      flush();
     } catch (Exception e) {
      e.printStackTrace();
    }
  } // publish 
  
  // -------------------------------------------------------------------------
  
} // MyStreamHandler
