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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestResult;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;

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
    
			 while (it.hasNext()) {
			 	
					Connection con = (Connection)it.next();
					
					boolean exonResult = checkStableIDs( con, "exon");
					boolean translationResult = checkStableIDs( con, "translation");
					boolean transcriptResult = checkStableIDs( con, "transcript");
					boolean geneResult = checkStableIDs( con, "gene");
		
					result = result && exonResult && translationResult 
											&& transcriptResult && geneResult;
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



	

}
