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
	addToGroup("xrefs");
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

	String[] dbNames = getColumnValues(con, "SELECT DISTINCT(db_name) FROM external_db WHERE db_name NOT LIKE 'AFFY%'");
											    
	for (int i = 0; i < dbNames.length; i++) {

	    String dbName = dbNames[i];
	    logger.fine("Checking object types for " + dbName);
	    String sql = "SELECT DISTINCT(ox.ensembl_object_type) FROM external_db e, xref x, object_xref ox WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND e.db_name='" + dbName + "'";
	    String[] objectTypes = getColumnValues(con, sql);

	    // note that since the same external_db table is used across all databases
	    // it is allowable for some external_db entries to have no associated xrefs
	    if (objectTypes.length == 1) {

		ReportManager.correct(this, con, "Exactly one object type associated with " + dbName + " xrefs");
		
	    } else if (objectTypes.length > 1) {

		ReportManager.problem(this, con, dbName + " xrefs are associated with " + objectTypes.length + " Ensembl object types (" + Utils.arrayToString(objectTypes, ",") + ") - should only be one");
		result = false;

	    }

	}

        return result;

    } // run

    // ----------------------------------------------------------------------

} // XrefTypes
