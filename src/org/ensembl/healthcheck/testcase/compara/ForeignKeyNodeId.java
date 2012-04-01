/*
 Copyright (C) 2004 EBI, GRL
 
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

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.Team;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyNodeId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomicAlignBlockId that applies to a specific set of databases.
     */
    public ForeignKeyNodeId() {

        addToGroup("compara_genomic");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible(Team.COMPARA);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

       Connection con = dbre.getConnection();

       if (tableHasRows(con, "genomic_align_tree")) {
	   
	   if (tableHasRows(con, "genomic_align")) {
	       /* Can't use this because genomic_align.node_id can be NULL eg pairwise alignments */ 
	       /* result &= checkForOrphans(con, "genomic_align", "node_id", "genomic_align_tree", "node_id"); */
	       int num_orphans = DBUtils.getRowCount(con, "SELECT genomic_align.node_id FROM genomic_align LEFT JOIN genomic_align_tree ON genomic_align.node_id = genomic_align_tree.node_id WHERE genomic_align.node_id is not NULL AND genomic_align_tree.node_id iS NULL");
	       if (num_orphans > 0) {
		   ReportManager.problem(this, con, num_orphans + " genomic_align entries are not linked to genomic_align_tree");
		   ReportManager.problem(this, con, " USEFUL SQL: SELECT genomic_align.node_id FROM genomic_align LEFT JOIN genomic_align_tree ON genomic_align.node_id = genomic_align_tree.node_id WHERE genomic_align.node_id is not NULL AND genomic_align_tree.node_id iS NULL");
		   result = false;
	       }

	   } else {
	       ReportManager.correct(this, con, "NO ENTRIES in genomic_align table, so nothing to test IGNORED");
	   }
	   
	   // Check the left_node_id values are set (and assume right_node_ids have also been set)
	   int left_node_ids = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM genomic_align_tree WHERE left_node_id != 0");
	   if (left_node_ids == 0) {
	       ReportManager.problem(this, con, "There are no left_node_ids set.");
	   } else {
	       ReportManager.correct(this, con, "left_node_ids have been set");
	   }

	   /* Looking at distance_to_parent > 1 is true for LOW_COVERAGE but not epo */
	    //Check distance_to_parent > 1
	   /*
	   int dist_to_parent = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM genomic_align_tree WHERE distance_to_parent > 1");
	   if (dist_to_parent > 0) {
	       ReportManager.problem(this, con, dist_to_parent + " entries in genomic_align_tree table have distance_to_parent values greater than 1");
	       result = false;
	   } else {
	       ReportManager.correct(this, con, "All entries in genomic_align_tree table have distance_to_parent values of less than 1");
	   }
	   */
       } else {
	   ReportManager.correct(this, con, "NO ENTRIES in genomic_align_tree tables, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomicAlignId
