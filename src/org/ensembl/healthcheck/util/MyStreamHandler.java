/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.util;

import java.util.logging.*;
import java.util.*;
import java.io.*;

/**
 * Custom stream handler for logging. Just prints to an OutputStream.
 */

public class MyStreamHandler extends Handler {
  
  OutputStream outputStream;
  
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
  /** Close the Handler. Currently a no-op.
   * @throws SecurityException N/A.
   */
  public void close() throws java.lang.SecurityException {
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
