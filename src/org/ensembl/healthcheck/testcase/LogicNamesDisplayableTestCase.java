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

package org.ensembl.healthcheck.testcase;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check that the logic names in the analysis table are displayable.
 * Currently reads the list of displayable logc names from a text file.
 * Current set of logic names is stored at 
 *  http://www.ensembl.org/Docs/wiki/html/EnsemblDocs/LogicNames.html
 */
public class LogicNamesDisplayableTestCase extends EnsTestCase {
  
  private static final String LOGIC_NAMES_FILE = "logicnames.txt";
  private static final boolean CASE_SENSITIVE = false;
  
  /**
   * Creates a new instance of LogicNamesDisplayableTestCase
   */
  public LogicNamesDisplayableTestCase() {
    String[] cols = { "logic_name" };
    addCondition(new HasTableColumnsCondition("analysis", cols));
    addToGroup("db_constraints");
    setDescription("Checks that all logic names in analysis are displayable");
  }
  
  /**
   * Check each row in the logic_names column of the analysis table against the 
   * list of logic names that are displayed by the web code; this list is currently at
   * http://www.ensembl.org/Docs/wiki/html/EnsemblDocs/LogicNames.html
   * Note that this test case actually uses the names from the file logicnames.txt
   * which currently has to be manually created from the above URL.
   */
  public TestResult run() {
    
    boolean result = true;
    
    String message = CASE_SENSITIVE ? "Logic name comparison is case sensitive" : "Logic name comparison is NOT case sensitive";
    logger.info(message);
    
    // read the file containing the allowed logic names
    String[] allowedNames = Utils.readTextFile(LOGIC_NAMES_FILE);
    logger.fine("Read " + allowedNames.length + " logic names from " + LOGIC_NAMES_FILE);
    
    DatabaseConnectionIterator it = getMatchingSchemaIterator();
    
    while (it.hasNext()) {
      
      Connection con = (Connection)it.next();
      
      String[] dbLogicNames = getColumnValues(con, "SELECT logic_name FROM analysis");
      
      for (int i = 0; i < dbLogicNames.length; i++) {
        
        if (!Utils.stringInArray(dbLogicNames[i], allowedNames, CASE_SENSITIVE)) {
          ReportManager.problem(this, con, "Logic name " + dbLogicNames[i] + " in analysis table will not be drawn by the web code");
        } 
        
      }
      
    } // while connection
    
    return new TestResult(getShortTestName(), result);
    
  } // run
  
} // LogicNamesDisplayableTestCase
