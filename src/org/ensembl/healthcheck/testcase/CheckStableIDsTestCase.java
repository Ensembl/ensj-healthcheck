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

import java.sql.Connection;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestResult;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;

import java.util.Vector;
import java.util.Collections;

import java.lang.Comparable;

/**
 * Checks the *_stable_id tables to ensure they are populated, have no orphan references,
 * and have valid versions. Also prints some examples from the table for checking by eye.
 * 
 * <p>Group is <b>check_stable_ids</b></p>
 * 
 * <p>To be run after the stable ids have been assigned.</p>
 */
public class CheckStableIDsTestCase extends EnsTestCase {



	public CheckStableIDsTestCase() {
      addToGroup("check_stable_ids");
      setDescription("Checks *_stable_id tables are valid.");
  }

		
	public TestResult run() {

		boolean result = true;
    
		DatabaseConnectionIterator it = getDatabaseConnectionIterator();
    int numMinMaxIds = 0;
    Vector minMaxList = new Vector();

    while (it.hasNext()) {
        Connection con = (Connection)it.next();
					
        ReportManager.info(this, con, "Checking stable ids and versions for validity.");

        boolean exonResult = checkStableIDs( con, "exon");
        boolean translationResult = checkStableIDs( con, "translation");
        boolean transcriptResult = checkStableIDs( con, "transcript");
        boolean geneResult = checkStableIDs( con, "gene");
        
        minMaxList.add(getMinMaxStableIDs(con,"exon"));
        minMaxList.add(getMinMaxStableIDs(con,"translation"));
        minMaxList.add(getMinMaxStableIDs(con,"transcript"));
        minMaxList.add(getMinMaxStableIDs(con,"gene"));
        
        result = result && exonResult && translationResult 
            && transcriptResult && geneResult;
    }

    ReportManager.info(this, "All core DBs" , "Looking for possible duplicate stable identifiers.");
       
    /* Now make sure that there are no duplicate stable identifiers anywhere */    

    /* Use an inefficient O(N^2) algorithm to find potentially overlapping
     * stable ids. This could be made more efficient, but difficult to implement
     * this is still way, way faster than doing many SQL queries against
     * all of the databases.
     */
    int len = minMaxList.size();
    for(int i=0; i< len; i++) {
         Pair pair1 = (Pair)minMaxList.elementAt(i);

         for(int j=i+1; j< len; j++) {
           Pair pair2 = (Pair)minMaxList.elementAt(j);

           if(pair1.overlaps(pair2)) {
             /* do a *slow* SQL query because there are possible duplicates
              * to see if any identifiers are the same 
              */
             String dupString = getRowColumnValue( pair1.con, "select count(*) from "+ 
                                                   pair1.dbName + "." + pair1.tableName + " t1, " +
                                                   pair2.dbName + "." + pair2.tableName + " t2  " +
                                                   "where t1.stable_id = t2.stable_id");
             int dupCount = Integer.parseInt(dupString);

             if(dupCount > 0) {
               /* Duplicates were found! */
               if(pair1.dbName.equals(pair2.dbName)) {
                   /* it is probably ok to have duplicate stable ids in the same database
                    * but different tables.  Warn anyway.
                    */
                   ReportManager.warning(this, pair1.dbName, dupString + " duplicate stable identifiers" +
                                         " found between " + pair1.tableName + 
                                         " and " + pair2.tableName + ".");
               } else {
                 /* it is definately not ok to have duplicate stable ids in
                  * different databases, this is a problem.
                  */
                 result = false;
                 String dbName = pair1.dbName + " AND " + pair2.dbName;
                 String tab1   = pair1.dbName + "." + pair1.tableName;
                 String tab2   = pair2.dbName + "." + pair2.tableName;
                           
                 ReportManager.problem(this, dbName, dupString + " duplicate stable identifiers "+
                                       "found between " + tab1 + " and " + tab2 + ".");
               }
             }
           }       
         }
    }                
			 
		return new TestResult(getShortTestName(), result);
	}

	
	

	/**
	 * Checks that the typeName_stable_id table is valid. The table is valid if it has >0 rows,
	 * and there are no orphan references between typeName table and typeName_stable_id. Also
	 * prints some example data from the typeName_stable_id table via ReportManager.info().
	 * @param con connection to run quries on.
	 * @param typeName name of the type to check, e.g. "exon"
	 * @return true if the table and references are valid, otherwise false.
	 */
	public boolean checkStableIDs(Connection con, String typeName) {
		
		boolean result = true;
		
				String nStableIDs = getRowColumnValue( con, "select count(*) from "+  typeName +"_stable_id;");
				ReportManager.info(this, con, "Num " +  typeName +"s stable ids = " + nStableIDs);
		
				if ( Integer.parseInt(	nStableIDs )<1 ) {
					ReportManager.problem(this, con,  typeName +"_stable_id table is empty.");
					result = false;
				}

				// TODO - add Craig's DBUtils.printRows method to head
				// print a few rows so we can check by eye that the table looks ok
				DBUtils.printRows(this, con, "select * from " +  typeName +"_stable_id limit 10;");
		
		
				// look for orphans between type and type_stable_id tables
				int orphans = countOrphans(con, typeName, typeName +"_id", 
																		typeName +"_stable_id", typeName +"_id", false);
				if ( orphans>0 ) {
					ReportManager.problem(this, con, "Orphan references between "+  typeName +" and " 
					+  typeName +"_stable_id tables.");
					result = false;
				}

				String nInvalidVersionsStr = getRowColumnValue( con,
							"select count(*) as "+  typeName +"_with_invalid_version"
								+ " from "+  typeName +"_stable_id where version<1;");
				int nInvalidVersions = Integer.parseInt(	nInvalidVersionsStr ); 
				if (  nInvalidVersions>0 ) {
					ReportManager.problem(this, con, "Invalid "+  typeName + "versions in "+  typeName +"_stable_id.");
					DBUtils.printRows(this, con, "select distinct(version) from "+  typeName +"_stable_id;" );
					result = false;
				}

				return result;
	}



  private Pair getMinMaxStableIDs(Connection con, String typeName) {
      Pair p = new Pair();
      String tableName = typeName + "_stable_id";
      
      p.min  = getRowColumnValue(con,"SELECT MIN(stable_id) FROM " + tableName);
      p.max  = getRowColumnValue(con,"SELECT MAX(stable_id) FROM " + tableName);
      p.con  = con;

      p.tableName = tableName;
      p.dbName    = DBUtils.getShortDatabaseName(con);
      
      return p;
  }
	
    private class Pair implements Comparable {
        String min;
        String max;
        String dbName;
        String tableName;
        Connection con;
        
        public int compareTo(Object o) {
            Pair p = (Pair)o;

            if(p.min == null || this.min == null) {
                return 0;
            }

            return this.min.compareTo(p.min);
        }

        public boolean overlaps(Pair p) {
            if(this.min == null || this.max == null || p.min == null || p.max == null) {
                return false;
            }

            return (p.max.compareTo(this.min) >= 0) && (p.min.compareTo(this.max) <= 0);
        }

    }


}
