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
import java.util.*;

import org.ensembl.healthcheck.util.*;

public abstract class EnsTestCase {
    
    protected TestRunner testRunner;
    protected String databaseRegexp = "";
    protected ArrayList groups;
    
    // -------------------------------------------------------------------------
    /** 
     * Creates a new instance of EnsTestCase
     */
    public EnsTestCase() {
    
      groups = new ArrayList();
      addToGroup("all");             // everything is in all, by default
      
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
    
    public ArrayList getGroups() {

      return groups;
      
    }   

    public String getCommaSeparatedGroups() {

      StringBuffer gString = new StringBuffer();
      
      Iterator it = groups.iterator();
      while (it.hasNext()) {
        gString.append((String)it.next());
	if (it.hasNext()) {
	  gString.append(",");
	}
      }
      return gString.toString();
    }   
    
    public void setGroups(ArrayList s) {
      
	groups = s;
    
    }
    
    public void setGroups(String[] s) {
      for (int i = 0; i < s.length; i++) {
	groups.add(s[i]);
      }
    }
    
    public void addToGroup(String newGroupName) {
      
      if (!groups.contains(newGroupName)) {
	groups.add(newGroupName);
      } else {
	System.err.println("Warning: " + getTestName() + " is already a memeber of " + newGroupName + " not added again.");
      }
      
    }
    
    public void removeFromGroup(String groupName) {
     
      if (groups.contains(groupName)) {
	groups.remove(groupName);
      } else {
	System.err.println("Warning: " + getTestName() + " was not a memeber of " + groupName);
      }
      
    }
    
    public boolean inGroup(String group) {
     
      return groups.contains(group);
      
    }
    
    public boolean inGroups(ArrayList checkGroups) {
     
      boolean result = false;
      
      Iterator it = checkGroups.iterator();
      while (it.hasNext()) {
	if (inGroup((String)it.next())) {
	  result = true;
	}
      }
      return result;
      
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
    public int countRowsInTable(Connection con, String table) {
            
      return getRowCount(con, "SELECT COUNT(*) FROM " + table);
            
    } // countRowsInTable
    
    // -------------------------------------------------------------------------
   
    public int getRowCount(Connection con, String sql) {
      
      int result = -1;
      
      try {
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(sql);
	if (rs != null) {
	  rs.next();
	  result = rs.getInt(1);	
	}
	rs.close();
	stmt.close();
      } catch (Exception e) {
	e.printStackTrace();
      }
      
      return result;
      
    }
    
    // -------------------------------------------------------------------------
    
    public int countOrphans(Connection con, String table1, String col1, String table2, String col2, boolean oneWayOnly) {
      
      int resultLeft, resultRight;
      
      String sql = "SELECT COUNT(*) FROM " + table1 + 
                   " LEFT JOIN " + table2 + " ON " + table1 + "." + col1 + " = " + table2 + "." + col2 + 
		   " WHERE " + table2 + "." + col2 + " iS NULL";

      resultLeft = getRowCount(con, sql);
      
      if (!oneWayOnly) {
	// and the other way ... (a right join?)
	sql = "SELECT COUNT(*) FROM " + table2 + 
	      " LEFT JOIN " + table1 + " ON " + table2 + "." + col2 + " = " + table1 + "." + col1 + 
	      " WHERE " + table1 + "." + col1 + " IS NULL";
     
	resultRight = getRowCount(con, sql);
      } else {
	 resultRight = 0;
      }
      
      System.out.println("Left: " + resultLeft + " Right: " + resultRight);
      
      return resultLeft + resultRight;
      
      
    } // countOrphans
    
    // -------------------------------------------------------------------------
    
    public TestResult checkSameSQLResult(String sql, String dbRegexp) {
      
      TestResult tr = new TestResult();
      ArrayList resultSetGroup = new ArrayList();
      
      DatabaseConnectionIterator it = testRunner.getDatabaseConnectionIterator(getAffectedDatabases(dbRegexp));
      
      while (it.hasNext()) {
	
	Connection con = (Connection)it.next();
	
	try {
	  Statement stmt = con.createStatement();
	  ResultSet rs = stmt.executeQuery(sql);
	  if (rs != null) {
	    resultSetGroup.add(rs);
	  }
	  rs.close();
	  stmt.close();
	  con.close();
	} catch (Exception e) {
	  e.printStackTrace();
	}
      }
      
      boolean same = DBUtils.compareResultSetGroup(resultSetGroup);
      
      return tr;
    
    } // checkSameSQLResult
    
    // -------------------------------------------------------------------------
    
} // EnsTestCase
