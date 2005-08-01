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
\Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
* Check that there are certain types in repeat_types.
*/

public class RepeatConsensus extends SingleDatabaseTestCase {
	
   /**
    * Create a new RepeatConsensus testcase.
    */
   public RepeatConsensus() {

       addToGroup("post_genebuild");
       addToGroup("release");
       setDescription("Check there certain types in repeat_consensus.repeat_type.");

   }

   /**
    * Run the test.
    * 
    * @param dbre
    *          The database to use.
    * @return true if the test pased.
    *  
    */
   public boolean run(DatabaseRegistryEntry dbre) {

       boolean result = true;
       
       Connection con = dbre.getConnection();
      
       int rows = getRowCount(con, "SELECT COUNT(*) FROM repeat_consensus WHERE repeat_type in ('simple','')");
       
       if (rows == 0) {

           ReportManager.problem(this, con, "repeat_consensus table has no rows of repeat_type 'simple' or empty");
           result = false;

       } else {

           ReportManager.correct(this, con, "repeat_consensus appears to have valid repeat_types");
       }

       return result;

   } // run

} // RepeatConsensus
