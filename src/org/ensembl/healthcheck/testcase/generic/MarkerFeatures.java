/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that:
 *  - marker features exist if markers exist
 *  - that map_wieghts are set to non-zero values
 *  - all marker priorities are > 50
 *  - each chromosome has some marker features
 *
 * Currently only checks for human, mouse, rat and zebrafish.
 */

public class MarkerFeatures extends SingleDatabaseTestCase {

    // marker priority to warn if under
    private static final int MARKER_PRIORITY_THRESHOLD = 50;

    // max number of top-level seq regions to check
    private static final int MAX_TOP_LEVEL = 100;

    /**
     * Creates a new instance of MarkerFeatures.
     */
    public MarkerFeatures() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Checks that marker_features exist and that they have non-zero map_weights, that marker priorities are sensible and that all chromosomes have some marker features");

    }

    /**
     * This test only applies to core databases.
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
	removeAppliesToType(DatabaseType.ESTGENE);
	removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Test various things about marker features.
     * 
     * @param dbre The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
	
	// only check for human, mouse, rat and zebrafish
        Species s = dbre.getSpecies();
	if (s.equals(Species.HOMO_SAPIENS) || s.equals(Species.MUS_MUSCULUS) || s.equals(Species.RATTUS_NORVEGICUS) || s.equals(Species.DANIO_RERIO)) {

	    result &= checkFeaturesAndMapWeights(con);
	
	    result &= checkMarkerPriorities(con);
	
	    result &= checkAllChromosomesHaveMarkers(con);

	}
	
        return result;

    } // run

    // ----------------------------------------------------------------------
    /*
     * Verify marker features exist if markers exist, and that map weights are non-zero.
     */
    
    private boolean checkFeaturesAndMapWeights(Connection con) {

	boolean result = true;
	
        int rowCount = getRowCount(con, "SELECT COUNT(*) FROM marker_feature");

        if (rowCount == 0) {
            ReportManager.problem(this, con, "No marker features in database even though markers are present");
            result = false;
        }

        int badWeightCount = getRowCount(con, "SELECT marker_id, COUNT(*) AS correct, map_weight FROM marker_feature GROUP BY marker_id HAVING map_weight != correct");

        if (badWeightCount > 0) {
            ReportManager.problem(this, con, badWeightCount + " marker features have not been assigned correct map weights");
            result = false;
        }

        if (result) {
            ReportManager.correct(this, con, "Marker features appear to be ok");
        }

	return result;

    } // checkFeaturesAndMapWeights


    // ----------------------------------------------------------------------
    /**
     * Check that all priorities are greater than a certain threshold.
     */
    private boolean checkMarkerPriorities(Connection con) {
	
	boolean result = true;

	int count = getRowCount(con, "SELECT COUNT(*) FROM marker WHERE priority > " + MARKER_PRIORITY_THRESHOLD);
				
	if (count == 0) {
	    
	    ReportManager.problem(this, con, " No marker features have priorities greater than the threshold (" + MARKER_PRIORITY_THRESHOLD + ")");
	    result = false;

	} else {

	    ReportManager.correct(this, con, "Some marker features have priorities greater than " + MARKER_PRIORITY_THRESHOLD);

	}

	return result;
    }

    // ----------------------------------------------------------------------
    /**
     * Check that all chromomes have > 0 markers.
     */
    private boolean checkAllChromosomesHaveMarkers(Connection con) {

	boolean result = true;

	// find all the chromosomes, and for each one check that it has some markers
	// note a "chromosome" is assumed to be a seq_region that is:
	//  - on the top-level co-ordinate system and
	//  - doesn't have and _ or . in the name and
	//  - has a seq_region name of less than 3 characters 
        //  - doesn't have a name starting with "Un"

	// get top level co-ordinate system ID
	String sql = "SELECT coord_system_id FROM coord_system WHERE rank=1 LIMIT 1";
		
	String s = getRowColumnValue(con, sql);
	
	if (s.length() == 0) {
	    System.err.println("Error: can't get top-level co-ordinate system for " + DBUtils.getShortDatabaseName(con));
	    return false;
	} 
	
	int topLevelCSID = Integer.parseInt(s);
	    
	try {
		
	    // check each top-level seq_region (up to a limit) to see how many marker features there are
	    Statement stmt = con.createStatement();
		
	    ResultSet rs = stmt.executeQuery("SELECT * FROM seq_region WHERE coord_system_id=" + topLevelCSID + " AND name NOT LIKE '%\\_%' AND name NOT LIKE '%.%' AND name NOT LIKE 'Un%' AND LENGTH(name) < 3 ORDER BY name" );
		
	    int numTopLevel = 0;
		
	    while (rs.next() && numTopLevel++ < MAX_TOP_LEVEL) {

		long seqRegionID = rs.getLong("seq_region_id");
		String seqRegionName = rs.getString("name");
		logger.fine("Counting marker features on chromosome " + seqRegionName);
		    
		sql = "SELECT COUNT(*) FROM marker_map_location WHERE chromosome_name='" + seqRegionName + "'";
		int rows = getRowCount(con, sql);
		if (rows == 0) {
			
		    ReportManager.problem(this, con, "Chromosome " + seqRegionName + " (seq_region_id " + seqRegionID + ") has no markers");
		    result = false;
			
		} else {
			
		    ReportManager.correct(this, con, "Chromosome " + seqRegionName + " has " + rows + " markers");
		}

	    }

	    rs.close();
	    stmt.close();
	    
	    if (numTopLevel == MAX_TOP_LEVEL) {
		logger.warning("Only checked first " + numTopLevel + " seq_regions");
	    }
	    
	    
	} catch (SQLException se) {
	    se.printStackTrace();
	}

	return result;

    }

    // ----------------------------------------------------------------------
    
} // MarkerFeatures
