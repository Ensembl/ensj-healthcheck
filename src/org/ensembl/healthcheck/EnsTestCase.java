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

/**
 * <p>Title: EnsTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 11, 2003, 1:12 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version 1.0
 */

package org.ensembl.healthcheck;

import java.sql.*;

public abstract class EnsTestCase {
    
    protected TestRunner testRunner;
    protected String databaseRegexp = "";
    
    // -------------------------------------------------------------------------
    /** 
     * Creates a new instance of EnsTestCase
     */
    public EnsTestCase() {
      
    } // EnsTestCase
    
    // -------------------------------------------------------------------------
    abstract TestResult run();   
    
    // -------------------------------------------------------------------------
    
    public TestRunner getTestRunner() {
      
	return testRunner;
	
    } // getTestRunner
    
    // -------------------------------------------------------------------------
    
    public void init(TestRunner tr) {

      this.testRunner = tr;
    
    } // init

    // -------------------------------------------------------------------------
    
    public String getTestName() {
      
      return this.getClass().getName();
      
    }
    
     // -------------------------------------------------------------------------
    
    public String getShortTestName() {
      
      String longName = getTestName();

      return longName.substring(longName.lastIndexOf('.')+1);
      
    }
    
    // -------------------------------------------------------------------------
    
    public String[] getAffectedDatabases(String databaseRegexp) {
    
      return testRunner.getListOfDatabaseNames(databaseRegexp);
      
    } // getAffectedDatabases
    
    // -------------------------------------------------------------------------
    
    public void printAffectedDatabases(String databaseRegexp) {
    
      System.out.println("Databases matching " + databaseRegexp + ":");
      String[] databaseList = getAffectedDatabases(databaseRegexp);
      for (int i = 0; i < databaseList.length; i++) {
	System.out.println("\t\t" + databaseList[i]);
      }
      
    } // printAffectedDatabases
    // -------------------------------------------------------------------------
    
} // EnsTestCase
