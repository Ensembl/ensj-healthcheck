/*
  Copyright (C) 2004 EBI, GRL
 
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

import org.ensembl.healthcheck.*;

/**
 * Logging handler that takes makes a callback to a particular method when a log record is created.
 */
public class CallbackHandler extends Handler {
  
  CallbackTarget callbackTarget;
  
  // ------------------------------------------------------------------------
  /** Creates a new instance of CallbackHandler
   * @param ct The object on which to call the callback method.
   * @param formatter The formatter object to use.
   */
  public CallbackHandler(CallbackTarget ct, Formatter formatter) {
    callbackTarget = ct;
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
   * Flush. Currently a no-op.
   */
  public void flush() {
  }
  
  // -------------------------------------------------------------------------
  /**
   * Implementation of the Handler interface publish() method. Called each time a log record is produced.
   * In this Handler, the <code>callback</code> method of the CallbackTarget it called with the log message.
   * @param logRecord The logRecord to deal with.
   */
  public void publish(LogRecord logRecord) {
    try {
      callbackTarget.callback(logRecord);
    } catch (Exception e) {
      e.printStackTrace();
    }
  } // publish 
  
  // -------------------------------------------------------------------------
  
} // CallbackHandler
