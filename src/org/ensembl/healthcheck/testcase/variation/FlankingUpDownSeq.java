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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
//import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that if the up_seq or down_seq of flanking_sequence is null, that
 * up_seq_region_start or down_seq_region_start should not be null. 
 */
public class FlankingUpDownSeq extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of CheckFlankingUpDownSeq
     */
    public FlankingUpDownSeq() {
        addToGroup("variation");
	//  addToGroup("release");
        setDescription("Check that if the up_seq or down_seq of flanking_sequence is null, that up_seq_region_start or down_seq_region_start should not be null.");
    }

    /**
     * Find any matching databases that have both no up_seq and up_seq_region_start.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

	// check up_seq and up_seq_region_start 

        Connection con = dbre.getConnection();
        int rows = getRowCount(con, "SELECT COUNT(*) FROM flanking_sequence WHERE up_seq is null AND up_seq_region_start is null");
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " have no up_seq and no up_seq_region_start");
        } else {
            ReportManager.correct(this, con, "No flanking_sequence have no up_seq and no up_seq_region_start");
        }

	// check down_seq and down_seq_region_start
	rows = getRowCount(con, "SELECT COUNT(*) FROM flanking_sequence WHERE down_seq is null AND down_seq_region_start is null");
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " have no down_seq and no down_seq_region_start");
        } else {
            ReportManager.correct(this, con, "No flanking_sequence have no down_seq and no down_seq_region_start");
        }

        // check how many variation don't have flanking sequence
        rows = getRowCount(con, "SELECT COUNT(*) FROM variation v LEFT JOIN flanking_sequence f ON v.variation_id=f.variation_id WHERE f.variation_id is NULL");
        if (rows > 0) {
            result = false;
            ReportManager.problem(this, con, rows + " variations have no flanking sequence");
        } else {
                    ReportManager.correct(this, con, "All variations have flanking sequence");
        }            
        return result;

    } // run

} // FlankingUpDownSeq
