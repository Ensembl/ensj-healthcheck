/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*

 $Log$
 Revision 1.1  2003/11/18 17:01:12  dkeefe
 Compare the _meta_table_info for old and new mart. So far just looks
 for missing columns in new


*/


package org.ensembl.healthcheck.testcase;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Compares the contents of two marts and reports significant changes
 */
public class MartCompareOldNewTestCase extends EnsTestCase  {

    /**
     * Creates a new instance of MartCompareOldNewTestCase
     */
    public MartCompareOldNewTestCase() {

	    addToGroup("post_ensmartbuild");
	    databaseRegexp = ".*";// any 
	    setDescription("Compares the _meta_table_info for two marts and reports big differences");

    }


    /**
     * Compare the _meta_table_info for two marts and report big differences
     */
    public TestResult run() {

	boolean result = true;

	DatabaseConnectionIterator it = getDatabaseConnectionIterator();
	int i;
	Connection con;
        
        // first check we got acceptable input from user
        i=0;
        while (it.hasNext()) {
            con = (Connection)it.next();
	    System.out.print(it.getCurrentDatabaseName()+" ");
	    i++;
	}
	if(i != 2){	// check we have 2 and only 2 databases
	    result = false;
            System.out.print("incorrect number of marts included by regex");
            return new TestResult(getShortTestName(), result);
	}


	       
        it = getDatabaseConnectionIterator();
        String query = "select table_column, column_non_null_value_count,column_distinct_non_null_value_count from _meta_table_info "; 


	try{
	    con = (Connection)it.next();
	    String mart1 = it.getCurrentDatabaseName();
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(query);        
	   
            // put results for new mart in a hash
	    con = (Connection)it.next();
	    String mart2 = it.getCurrentDatabaseName();
	    Statement st2 = con.createStatement();
	    ResultSet rs2 = st2.executeQuery(query);
	    Hashtable counts = new Hashtable();
            while(rs2.next()){
		String key =  rs2.getString(1); 
                Integer nonNull = new Integer(rs2.getInt(2));  
		Integer distinct = new Integer(rs2.getInt(3));  
		counts.put(key,distinct);
	        //System.out.print(key+" "+distinct+"\n");
	    }

            while(rs1.next()){
                String key =  rs1.getString(1); 
                Integer nonNull = new Integer(rs1.getInt(2));  
		Integer distinct = new Integer(rs1.getInt(3)); 
		if(counts.containsKey(key)){ // does the new mart contain this column
		    // if so compare the values
                    Integer newCount = (Integer)counts.get(key);
                    if(newCount.intValue() > distinct.intValue() * 2){
			ReportManager.info(this, con, "SUDDEN INCREASE: "+key+" "
                                                      +mart1+" "+distinct+" "+mart2+" "+newCount);
		    }

                    if(newCount.intValue() <  distinct.intValue() / 2){
			ReportManager.info(this, con, "SUDDEN DECREASE: "+key+" "
                                                      +mart1+" "+distinct+" "+mart2+" "+newCount);
		    }

		    // and remove the entry from the hash table
                    counts.remove(key);
		}else{
		    // report the missing column as a problem
		    ReportManager.problem(this, con, "MISSING COLUMN: "+key+" not in "+mart2);
		    result = false;
		}

		// look at what is left in the hash table - ie new stuff in new mart

 
	    }



	} catch(SQLException e) {
	    e.printStackTrace();
	}



	
	return new TestResult(getShortTestName(), result);

    } // run

} // MartCompareOldNewTestCase
