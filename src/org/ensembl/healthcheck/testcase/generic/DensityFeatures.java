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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all top-level seq regions have some SNP/gene/knownGene density features, and 
 * that the values agree between the density_feature and seq_region attrib tables.
 * Only checks top-level seq regions that do NOT have an _ in their names.
 */

public class DensityFeatures extends SingleDatabaseTestCase {

    // max number of top-level seq regions to check
    private static final int MAX_TOP_LEVEL = 100;

    // map between analysis.logic_name and seq_region attrib_type.code
    private Map logicNameToAttribCode = new HashMap();
   

    /**
     * Create a new DensityFeatures testcase.
     */
    public DensityFeatures() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that all top-level seq regions have some SNP/gene/knownGene density features, and that the values agree between the density_feature and seq_region attrib tables.");

	logicNameToAttribCode.put("snpDensity", "SNPCount");
	logicNameToAttribCode.put("geneDensity", "GeneCount");
	logicNameToAttribCode.put("knownGeneDensity", "knownGeneCount");

    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

	// get top level co-ordinate system ID
	String sql = "SELECT coord_system_id FROM coord_system WHERE rank=1 LIMIT 1";
		
	String s = getRowColumnValue(con, sql);
	
	if (s.length() == 0) {
	    System.err.println("Error: can't get top-level co-ordinate system for " + DBUtils.getShortDatabaseName(con));
	    return false;
	} 
	
	int topLevelCSID = Integer.parseInt(s);
	
	    
	try {
		
	    // check each top-level seq_region (up to a limit) to see how many density features there are
	    Statement stmt = con.createStatement();
		
	    ResultSet rs = stmt.executeQuery("SELECT * FROM seq_region WHERE coord_system_id=" + topLevelCSID + " AND name NOT LIKE '%\\_%'");
		
	    int numTopLevel = 0;
		
	    while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {
		    
		long seqRegionID = rs.getLong("seq_region_id");
		String seqRegionName = rs.getString("name");
		logger.fine("Counting density features on seq_region " + seqRegionName);
		    
		sql = "SELECT COUNT(*) FROM density_feature WHERE seq_region_id=" + seqRegionID;
		int dfRows = getRowCount(con, sql);
		if (dfRows == 0) {
			
		    ReportManager.problem(this, con, "Top-level seq region " + seqRegionName + " (ID " + seqRegionID + ") has no density features");
		    result = false;
			
		} else {
			
		    ReportManager.correct(this, con, seqRegionName + " has " + dfRows + " density features");
		}

		// check each analysis type
		Iterator it = logicNameToAttribCode.keySet().iterator();
		while (it.hasNext()) {
	    
		    String logicName = (String)it.next();
		    String attribCode = (String)logicNameToAttribCode.get(logicName);

		    
		    // check if this species has appropriate density features
		    int analRows = getRowCount(con, "SELECT COUNT(*) FROM analysis WHERE logic_name='" + logicName + "'");
		    if (analRows == 0) {
			logger.info(DBUtils.getShortDatabaseName(con) + " has no " + logicName + " analysis type, skipping checks for these features");
		    } else {
			
			// check that the sum of the density_feature.density_value matches what
			// is in the seq_region_attrib table
			
			logger.fine("Comparing density_feature.density_value with seq_region_attrib for " + logicName + " features on " + seqRegionName);
			
			sql = "SELECT SUM(df.density_value) FROM density_type dt, density_feature df, analysis a WHERE dt.density_type_id=df.density_type_id AND dt.analysis_id=a.analysis_id AND a.logic_name='" + logicName + "' AND seq_region_id=" + seqRegionID;
			
			String sumDF = getRowColumnValue(con, sql);
			if (sumDF != null && sumDF.length() > 0) {
			    
			    long sumFromDensityFeature = Long.parseLong(sumDF);
			    
			    sql = "SELECT value FROM seq_region_attrib sra, attrib_type at WHERE sra.attrib_type_id=at.attrib_type_id AND at.code='" + attribCode + "' AND seq_region_id=" + seqRegionID;
			    
			    String sumSRA = getRowColumnValue(con, sql);
			    long valueFromSeqRegionAttrib = Long.parseLong(sumSRA);
			    
			    if (sumSRA != null && sumSRA.length() > 0) {
				
				if (sumFromDensityFeature != valueFromSeqRegionAttrib) {
				    
				    ReportManager.problem(this, con, "Sum of values for " + logicName + " from density_feature (" + sumFromDensityFeature + ") doesn't agree with value from seq_region_attrib (" + valueFromSeqRegionAttrib + ") for " + seqRegionName);
				    result = false;
					
				} else {
				    
				    ReportManager.correct(this, con, "density_feature and seq_region_attrib values agree for " + logicName + " on seq region " + seqRegionName);
				    
				}
				
			    } // if sumSRA
			    
			} // if sumDF
			
		    } // if rows 
		    
		} // while it

	    } // while rs.next
	    
	    rs.close();
	    stmt.close();
	    
	    if (numTopLevel == MAX_TOP_LEVEL) {
		logger.warning("Only checked first " + numTopLevel + " seq_regions");
	    }
	    
	    
	} catch (SQLException se) {
	    se.printStackTrace();
	}
	
    
	return result;
	
    } // run
    
} // DensityFeatures
