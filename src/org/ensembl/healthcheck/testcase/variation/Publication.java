/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Checks database does not contain duplicated Publication entries
 */
public class Publication extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Publication
	 */
	public Publication() {

		addToGroup("variation-release");
		setDescription("Check the database does not contain duplicated Publication entries");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return True if the test passed
	 */
         public boolean run(final DatabaseRegistryEntry dbre) {
	     boolean result = true;

		Connection con = dbre.getConnection();

		try {
			/*
			 * Attempt to extract a list of publications with the same unique id
			 */


		    // fields to check on: 
		    String[] check_id = {"pmid", "pmcid", "doi" }; 


		    for (int i =0; i< 3; i++){

			String stmt = "select count(*) from publication p1, publication p2 where p1." + check_id[i] + " = p2." + check_id[i] + " and p1.publication_id < p2.publication_id;";
			
                        int rows = DBUtils.getRowCount(con,stmt);
                        if (rows > 0) {
			    result = false;
			    ReportManager.problem(this, con, String.valueOf(rows) + " rows are duplicated in publication on " + check_id[i]);
                        }
			

		    }		
		} catch (Exception e) {
		    ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
		    result = false;
		}
		
		if (result) {
		    // if there were no problems, just inform for the interface to pick the HC
		    ReportManager.correct(this, con, "Publication healthcheck passed without any problem");
		}
		return result;
	 } 
    
}
