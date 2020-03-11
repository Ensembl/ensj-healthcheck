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
import java.util.logging.LogRecord;

/**
 * Custom formatter that formats parts of a log record to a single line.
 */

public class LogFormatter extends Formatter {

  /** Implementation of the Formatter interface. This method is called for every log record.
   * @param rec The log record to format.
   * @return The formatted log record.
   */
  public String format(LogRecord rec) {
    StringBuffer buf = new StringBuffer();
    //buf.append(rec.getLevel() + ": ");
    buf.append(formatMessage(rec));
    buf.append('\n');
    return buf.toString();
  }

} // LogFormatter
