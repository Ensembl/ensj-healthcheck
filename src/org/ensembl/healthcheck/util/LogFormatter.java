package org.ensembl.healthcheck.util;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

import java.util.logging.*;
import java.util.*;

// This custom formatter formats parts of a log record to a single line

public class LogFormatter extends Formatter {

  // This method is called for every log record
  public String format(LogRecord rec) {
    StringBuffer buf = new StringBuffer();
    buf.append(rec.getLevel() + ": ");
    buf.append(formatMessage(rec));
    buf.append('\n');
    return buf.toString();
  }


} // LogFormatter
