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
import org.ensembl.healthcheck.Species;

/**
 * Checks populations are entered as expected
 */
public class Population extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Population
	 */
	public Population() {

		addToGroup("variation-release");
		setDescription("Check the Populations are entered as expected");
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
			// Check for populations without sizes
			String size_stmt = "select count(*) from population pop, individual_population ip where pop.population_id = ip.population_id and pop.size is null;";
			
                        int size_rows = DBUtils.getRowCount(con,size_stmt);
                        if (size_rows > 0) {
			    result = false;
			    ReportManager.problem(this, con, String.valueOf(size_rows) + " populations have no stored size");
                        }

			// Check for populations with freqs_from_gts set for mouse and human
			 if (dbre.getSpecies() == Species.MUS_MUSCULUS || dbre.getSpecies() == Species.HOMO_SAPIENS) {

			     // Check for populations without sizes
			     String freq_stmt = "select count(*) from population pop where pop.freqs_from_gts = 1;";
			
			     int freq_rows = DBUtils.getRowCount(con,freq_stmt);
			     if (freq_rows == 0) {
				 result = false;
				 ReportManager.problem(this, con, "No populations have freqs_from_gts set ");
			     }
			 }
			 
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
			result = false;
		}
		

		return result;
	 } 
    
}
