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
        addToGroup("variation-long");
	setHintLongRunning(true);
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
	float tol = 0.025f;
	// Get the results in batches (determined by the variation_id)
	int chunk = 250000;
	
	// long mainStart = System.currentTimeMillis();
	try {
		
	    Statement stmt = con.createStatement();
	    
	    // Get variations with allele/genotype frequencies that don't add up to 1 for the same variation_id, subsnp_id and sample_id
	    for (int i=0; i<tables.length; i++) {
		// long subStart = System.currentTimeMillis();
		
		// Get the maximum variation id
		String sql = "SELECT MAX(s.variation_id) FROM " + tables[i] + " s";
		sql = getRowColumnValue(con,sql);
		if (sql.length() == 0) {
		    sql = new String("0");
		}
		int maxId = Integer.parseInt(sql);
		
		// The query to get the data
		sql = "SELECT s.variation_id, s.subsnp_id, s.sample_id, s.frequency FROM " + tables[i] + " s USE INDEX (variation_idx,subsnp_idx) WHERE s.variation_id BETWEEN VIDLOWER AND VIDUPPER ORDER BY s.variation_id, s.subsnp_id, s.sample_id";
		int offset = 1;
		
		// Count the number of failed
		int failed = 0;
		// Keep an example
		String example = new String();
		
		// Loop until we've reached the maximum variation_id
		while (offset <= maxId) {
		    
		    // long s = System.currentTimeMillis();
		    
		    // Replace the offsets in the SQL query
		    ResultSet rs = stmt.executeQuery(sql.replaceFirst("VIDLOWER",String.valueOf(offset)).replaceFirst("VIDUPPER",String.valueOf(offset+chunk)));
		    
		    // long e = System.currentTimeMillis();
		    // System.out.println("Got " + String.valueOf(chunk) + " variations in " + String.valueOf(((e-s)/1000)) + " seconds. Offset is " + String.valueOf(offset));
		    
		    // Increase the offset with the chunk size and add 1
		    offset += chunk+1;
		    
		    int lastVid = 0;
		    int lastSSid = 0;
		    int lastSid = 0;
		    int curVid;
		    int curSSid;
		    int curSid;
		    float freq;
		    float sum = 1.f;
		    int count = 0;
		    
		    while (rs.next()) {
			
			// Get the variation_id, subsnp_id, sample_id and frequency. If any of these are NULL, they will be returned as 0 
			curVid = rs.getInt(1);
			curSSid = rs.getInt(2);
			curSid = rs.getInt(3);
			freq = rs.getFloat(4);
			
			// If any of the values was NULL, skip processing the row. For the frequency, we have to use the wasNull() function to check this. The ids it is sufficient to check if they were 0 
			if (curVid != 0 && curSSid != 0 && curSid != 0 && !rs.wasNull()) {
			    
			    // If any of the ids is different from the last one, stop summing and check the sum of the latest variation
			    if (curVid != lastVid || curSSid != lastSSid || curSid != lastSid) {
				// See if the sum of the frequencies deviates from 1 more than what we tolerate. In that case, count it as a failed
				if (Math.abs(1.f - sum) > tol) {
				    if (failed == 0) {
					// Keep an example
					example = "variation_id = " + String.valueOf(lastVid) + ", subsnp_id = " + String.valueOf(lastSSid) + ", sample_id = " + String.valueOf(lastSid) + ", sum is " + String.valueOf(sum);
				    }
				    failed++;
				}
				
				// Set the last ids to this one and reset the sum
				lastVid = curVid;
				lastSSid = curSSid;
				lastSid = curSid;
				sum = 0.f;
			    }
			    // Add the frequency to the sum
			    sum += freq;
			}
			count++;
		    }
		    
		    rs.close();
		    
		    // s = System.currentTimeMillis();
		    // System.out.println("Processed " + String.valueOf(count) + " rows in " + String.valueOf(((s-e)/1000)) + " seconds");
		}
		if (failed == 0) {
		    // Report that the current table is ok
		    ReportManager.correct(this,con,"Frequencies in " + tables[i] + " all add up to 1");
		}
		else {
		    ReportManager.problem(this, con, "There are " + String.valueOf(failed) + " variations in " + tables[i] + " where the frequencies don't add up to 1 +/- " + String.valueOf(tol) + " (e.g. " + example + ")");
		    result = false;
		}
		// long subEnd = System.currentTimeMillis();
		// System.out.println("Time for healthcheck on " + tables[i] + " (~" + String.valueOf(maxId) + " variations) was " + String.valueOf(((subEnd-subStart)/1000)) + " seconds");
	    }
	    stmt.close();
	} catch (Exception e) {
	    result = false;
	    e.printStackTrace();
	}
	
	// long mainEnd = System.currentTimeMillis();
	// System.out.println("Total time for healthcheck was " + String.valueOf(((mainEnd-mainStart)/1000)) + " seconds");
	
	if ( result ){
	    ReportManager.correct(this,con,"Allele/Genotype frequency healthcheck passed without any problem");
	}
        return result;

    } // run

} // AlleleFrequencies
