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
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
//import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that allele frequencies add up to 1
 */
public class AlleleFrequencies extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of Check Allele Frequencies
     */
    public AlleleFrequencies() {
        addToGroup("variation");
        setDescription("Check that the allele frequencies add up to 1");
    }

    /**
     * Check that all allele/genotype frequencies add up to 1 for the same variation/subsnp and sample.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
	String[] tables = new String[] {
	    "population_genotype",
	    "allele"
	};
	// Tolerance for the deviation from 1.0
	float tol = 0.005f;
	// Get the results in batches (determined by the variation_id)
	int chunk = 1000000;
	
	try {
		
	    Statement stmt = con.createStatement();
	    
	    // Get variations with allele/genotype frequencies that don't add up to 1 for the same variation_id, subsnp_id and sample_id
	    for (int i=0; i<tables.length; i++) {
		// The query to get the data
		String sql = "SELECT s.variation_id, s.subsnp_id, s.sample_id, s.frequency FROM " + tables[i] + " s USE INDEX (variation_idx,subsnp_idx) ORDER BY s.variation_id, s.subsnp_id, s.sample_id LIMIT ";
		int offset = 0;
		int leftover = 1;
		boolean noFail = true;
		// Loop until we've reached the maximum variation_id or hit a fail condition
		while (leftover > 0 && noFail) {
		    ResultSet rs = stmt.executeQuery(sql + new String(String.valueOf(offset) + "," + String.valueOf(chunk)));
		    offset += chunk;
		    
		    int lastVid = 0;
		    int lastSSid = 0;
		    int lastSid = 0;
		    int curVid;
		    int curSSid;
		    int curSid;
		    float freq;
		    float sum = 1.f;
		    leftover = 0;
		    
		    while (rs.next()) {
			curVid = rs.getInt(1);
			curSSid = rs.getInt(2);
			curSid = rs.getInt(3);
			freq = rs.getFloat(4);
			
			// If any of the values was NULL, skip processing the row
			if (curVid != 0 && curSSid != 0 && curSid != 0 && !rs.wasNull()) {
			    // If any of the ids is different from the last one, stop summing and check the sum of the latest variation
			    if (curVid != lastVid || curSSid != lastSSid || curSid != lastSid) {
				if (Math.abs(1.f - sum) > tol) {
				    ReportManager.problem(this, con, "There are variations in " + tables[i] + " where the frequencies don't add up to 1 (e.g. variation_id = " + String.valueOf(lastVid) + ", subsnp_id = " + String.valueOf(lastSSid) + ", sample_id = " + String.valueOf(lastSid) + ", sum is " + String.valueOf(sum));
				    noFail = false;
				    result = false;
				    break;
				}
				// Set the last ids to this one and reset the sum
				lastVid = curVid;
				lastSSid = curSSid;
				lastSid = curSid;
				sum = 0.f;
				// The previous variation is completely processed so reset the leftover counter
				leftover = 0;
			    }
			    // Add the frequency to the sum
			    sum += freq;
			}
			leftover++;
		    }
		    
		    // Roll back the offset with the leftover count so that we don't skip any variations (will also take care of the very last variation)
		    offset -= leftover;
		    
		    rs.close();
		}
		if (noFail) {
		    ReportManager.correct(this,con,"Frequencies in " + tables[i] + " all add up to 1");
		}
	    }
	    stmt.close();
	} catch (Exception e) {
	    result = false;
	    e.printStackTrace();
	}
	if ( result ){
	    ReportManager.correct(this,con,"Allele/Genotype frequency healthcheck passed without any problem");
	}
        return result;

    } // run

} // AlleleFrequencies
