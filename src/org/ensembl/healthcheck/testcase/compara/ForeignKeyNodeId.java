/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
