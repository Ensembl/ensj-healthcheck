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
 * Checks database entries do not contain unsupported characters.
 */
public class CheckChar extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckChar
	 */
	public CheckChar() {

		addToGroup("variation-release");
		setDescription("Check that imported names/descriptions contains only supported characters");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Check only for Phenotype table currently
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
			 * Will extract a list of phenotype.descriptions and check for unsupported char & short names
			 */
		    boolean char_ok   = true;


		    List<String> data = getSqlTemplate(con).queryForDefaultObjectList(
			"select description from phenotype", String.class);
		    for (int i = 0; i < data.size(); i++) {

			    String input = data.get(i);

                            // check for unusually short descriptions
			    if(input.length() < 4){
				result = false;
				ReportManager.problem(this, con, "phenotype: " + input + " is suspiciously short");
			    }
 
                            // check for phenotype descriptions suggesting no phenotype
                            boolean name_ok = checkNonPhenotypes(input);
			    if(name_ok == false){
				result = false;
				ReportManager.problem(this, con, "phenotype: " + input + " is not useful");
			    }

                            // check for unsupported individual character
			    char_ok = checkUnsupportedChar(input);
			    if(char_ok == false){
				result = false;
				ReportManager.problem(this, con, "phenotype: " + input + " has suspect characters");
			    }

			}		
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
			result = false;
		}
		if (result) {
			// if there were no problems, just inform for the interface to pick the HC
			ReportManager.correct(this, con, "CheckChar healthcheck passed without any problem");
		}
		return result;
	} 

	// --------------------------------------------------------------

	private boolean checkUnsupportedChar( String input) {

		boolean is_ok = true;
		int len = input.length();
			    
		for (int i =0; i< len; i++){
		    char test_value= input.charAt(i);
				
		    //get ascii code				
		    int ascii_val = (int) test_value;	   
		    
		    // check code in supported range
		    if(ascii_val < 32 || ascii_val  > 126 || ascii_val == 60 || ascii_val == 62 ){
			is_ok = false;
		    }
		}
		return is_ok;
	}


	private boolean checkNonPhenotypes( String input) {

		boolean is_ok = true;
		String[] junk = {"None", "Not provided", "not specified", "Not in OMIM", "Variant of unknown significance" };

		int len = junk.length;
			    
		for (int i =0; i< len; i++){

		    if( input.equalsIgnoreCase( junk[i] ) ){
			is_ok  = false;
		    }
		}
		
		return is_ok;
	}

	// --------------------------------------
}
