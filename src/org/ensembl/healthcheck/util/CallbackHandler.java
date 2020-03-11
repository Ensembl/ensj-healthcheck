/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.ensembl.healthcheck.CallbackTarget;

/**
 * Logging handler that takes makes a callback to a particular method when a
 * log record is created.
 */
public class CallbackHandler extends Handler {

    private CallbackTarget callbackTarget;

    // ------------------------------------------------------------------------
    /**
     * Creates a new instance of CallbackHandler
     * 
     * @param ct
     *          The object on which to call the callback method.
     * @param formatter
     *          The formatter object to use.
     */
    public CallbackHandler(CallbackTarget ct, Formatter formatter) {
        callbackTarget = ct;
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
     * Flush. Currently a no-op.
     */
    public void flush() {
    }

    // -------------------------------------------------------------------------
    /**
     * Implementation of the Handler interface publish() method. Called each
     * time a log record is produced. In this Handler, the <code>callback</code>
     * method of the CallbackTarget it called with the log message.
     * 
     * @param logRecord
     *          The logRecord to deal with.
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
