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
 * <p>Title: CompareSQLTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 17, 2003, 2:48 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version
 */


package org.ensembl.healthcheck;

public class CompareSQLTestCase extends EnsTestCase {
  
  /** Creates a new instance of CompareSQLTestCase */
  public CompareSQLTestCase() {
    databaseRegexp = "homo_sapiens_core_12_31|mus_musculus_core_12_3";
  }
  
  TestResult run() {
 
    String sql = "SELECT * FROM gene";
      
    boolean result = checkSameSQLResult(sql, databaseRegexp);
 
    return new TestResult(getShortTestName(), result, "");

  } // run
  
} // compareSQLTestCase

