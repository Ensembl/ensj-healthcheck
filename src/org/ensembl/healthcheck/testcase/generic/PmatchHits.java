/*
 Copyright (C) 2004 Wellcome Trust Sanger Centre
 
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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Checks the number of hits produced by pmatch and warns if too many.
 */
public class PmatchHits extends SingleDatabaseTestCase {

    /**
     * Create a new testcase.
     */
    public PmatchHits() {

        addToGroup("post_pmatch");
        setDescription("Checks the number of hits produced by pmatch and warns if too many.");

    }
    
    /**
     * Maximum of pmatch hits before a warning is produced.
     * Current value: 9
     */
    public static int max_allowed = 9;

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
		    
	int internal_id = 0;
	String protein_id = "";
	int currcount = 0;
	
	try {	
	    
	    String sql = "SELECT p.protein_internal_id, p.protein_id, count(a.protein_internal_id) "
		+ "FROM protein p, pmatch_feature a WHERE p.protein_internal_id=a.protein_internal_id "
		+ "GROUP BY p.protein_internal_id ORDER BY 3 DESC;";

            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                // load the vars
		internal_id = rs.getInt(1);
		protein_id = rs.getString(2);
		currcount = rs.getInt(3);

		ReportManager.info(this, con, " " + protein_id + " (" + internal_id
				   + ") has " + currcount + " hits.");

		//quit when the low counts start
		if(currcount <= max_allowed){
		    return result;
		}
		else{
		    result = false;
		    ReportManager.problem(this, con, protein_id + " (" + internal_id
					  + ") has " + currcount + " hits.");
		}
	    
	    } // while rs
	
	    rs.close();
	    stmt.close();

	} catch (Exception e) {
            result = false;
            e.printStackTrace();
	}
	return result;
	
    } // run

} // PmatchHits
