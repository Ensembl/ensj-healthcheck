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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that all xrefs for a particular external_db map to one
 * and only one ensembl object type.
 */

public class XrefTypes extends SingleDatabaseTestCase {

    /**
     * Create a new XrefTypes testcase.
     */
    public XrefTypes() {

        addToGroup("post_genebuild");
        addToGroup("release");
	addToGroup("core_xrefs");
        setDescription("Check that all xrefs only map to one ensembl object type.");

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

	try {

	    Statement stmt = con.createStatement();

	    // Query returns all external_db_id-object type relations
	    // execute it and loop over each row checking for > 1 consecutive row with same ID
	    
	    ResultSet rs = stmt.executeQuery("SELECT x.external_db_id, ox.ensembl_object_type, COUNT(*) FROM xref x, object_xref ox WHERE x.xref_id = ox.xref_id GROUP BY x.external_db_id, ox.ensembl_object_type");
	    
	    long previousID = -1;
	    String previousType = "";

	    while (rs != null && rs.next()) {
		
		long externalDBID = rs.getLong(1);
		String objectType = rs.getString(2);
		int count = rs.getInt(3);
		
		if (externalDBID == previousID) {
		    
		    ReportManager.problem(this, con, "External DB ID " + externalDBID + " is associated with " + objectType + " as well as " + previousType);
		    result = false;
		    
		}
		
		previousType = objectType;
		previousID = externalDBID;

	    } // while rs
	 

	    stmt.close();

	} catch (SQLException e) {
            e.printStackTrace();
	}

	if (result) {

	    ReportManager.correct(this, con, "All external dbs are only associated with one Ensembl object type");
	    
	}

	return result;

    } // run

    // ----------------------------------------------------------------------

} // XrefTypes
