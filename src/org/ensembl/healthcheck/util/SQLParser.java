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

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import java.util.logging.*;

/**
 * Utilities for parsing a SQL file.
 */
public class SQLParser {
    
    /** Creates a new instance of SQLParser */
    public SQLParser() {
    }
    
    // -------------------------------------------------------------------------
    /** 
     * Parse a file containing SQL. 
     * @param fileName The name of the file to parse.
     * @return A list of SQL commands read from the file.
     */
    public List parse(String fileName) {
        
        List lines = new ArrayList();
        
      // TBC
        
        return lines;
        
    }
    
    // -------------------------------------------------------------------------

    
}
