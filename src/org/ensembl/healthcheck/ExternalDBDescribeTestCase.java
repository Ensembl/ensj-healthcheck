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

package org.ensembl.healthcheck;

/**
 * <p>Title: ExternalDBDescribeTestCase.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: EMBL</p>
 * <p>Created on March 18, 2003, 4:08 PM</p>
 * @author Glenn Proctor <glenn@ebi.ac.uk>
 * @version $Revision$
 */

public class ExternalDBDescribeTestCase extends EnsTestCase {
  
  /** Creates a new instance of ExternalDBDescriptionTestCase */
  public ExternalDBDescribeTestCase() {
    databaseRegexp = ".*_((core)|(estgene))_.*";
    addToGroup("current");
  }
  
  TestResult run() {
    
    boolean result = super.checkSameSQLResult("DESCRIBE external_db", databaseRegexp, preFilterRegexp);
    
    return new TestResult(getShortTestName(), result, "DESCRIBE external_db");
  }
  
}
