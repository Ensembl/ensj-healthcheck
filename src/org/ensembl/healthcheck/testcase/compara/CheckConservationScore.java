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

/**
 * An EnsEMBL Healthcheck test case that checks the conservation_score table
 */

public class CheckConservationScore extends SingleDatabaseTestCase {

    /**
     * Create an CheckConservationScore that applies to a specific set of databases.
     */
    public CheckConservationScore() {

        addToGroup("compara_genomic");
        setDescription("Check the conservation_score table in ensembl_compara databases.");
        setTeamResponsible("compara");

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

	/**
	 * Get all method_link_species_set_ids for method_link type of 
	 * GERP_CONSERVATION_SCORE
	 */
	String[] method_link_species_set_ids = getColumnValues(con, "SELECT method_link_species_set_id FROM method_link_species_set LEFT JOIN method_link USING (method_link_id) WHERE type=\"GERP_CONSERVATION_SCORE\" OR class LIKE \"ConservationScore%\"");

	/**
	 * Get Ancestral sequences genome_db_id
	 */
	String ancestral_seq_id = getRowColumnValue(con, "SELECT genome_db_id FROM genome_db WHERE name = \"Ancestral sequences\"");

        if (method_link_species_set_ids.length > 0) {

	    /** 
	     * Check have entries in conservation_score table
	     */
	    if (!tableHasRows(con, "conservation_score")) {
		ReportManager.problem(this, con, "FAILED: Database contains entry in the method_link_species_set table but the conservation_score table is empty"); 
		return result;
	    }

	    for (int i = 0; i < method_link_species_set_ids.length; i++) {

		//Get the mlss_id for the associated multiple alignment
		String multi_align_mlss_id = getRowColumnValue(con, "SELECT meta_value FROM meta WHERE meta_key=\"gerp_" + method_link_species_set_ids[i] + "\"");
		if (multi_align_mlss_id == "") {
		    ReportManager.problem(this, con, "There is no gerp_" + method_link_species_set_ids[i] + " entry in the meta table\n");
		} else {
		    /** Find the multiple alignments gabs which have more than 3
		     * species but don't have any conservation scores
		     * Need to exclude gabs containing ancestral sequences
		     */
		    String useful_sql;
		    if (ancestral_seq_id == "") {
			useful_sql = new String("SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN conservation_score USING (genomic_align_block_id) WHERE genomic_align_block.method_link_species_set_id = " +  multi_align_mlss_id + " AND conservation_score.genomic_align_block_id IS NULL GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) > 3");
		    } else {
			useful_sql = new String("SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN conservation_score USING (genomic_align_block_id) LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN dnafrag USING (dnafrag_id) WHERE genomic_align_block.method_link_species_set_id = " + multi_align_mlss_id + " AND conservation_score.genomic_align_block_id IS NULL AND genome_db_id <> " + ancestral_seq_id + " GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) > 3");
		    }

		    String[] failures = getColumnValues(con, useful_sql); 
		    if (failures.length > 0) {
			/** 
			 * Warning if there are blocks with 4 genomes because
			 * it is possible to have (human, chimp, rhesus) and 
			 * one of cow or dog and still not get above the
			 * min_rej_sub score (default=0.5)
			 */
			String useful_sql4 = new String("SELECT genomic_align_block.genomic_align_block_id FROM genomic_align_block LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN conservation_score USING (genomic_align_block_id) WHERE genomic_align_block.method_link_species_set_id = " +  multi_align_mlss_id + " AND conservation_score.genomic_align_block_id IS NULL GROUP BY genomic_align_block.genomic_align_block_id HAVING count(*) = 4");
			String[] failures4 = getColumnValues(con, useful_sql4);
			if (failures.length == failures4.length) {
			    ReportManager.problem(this, con, "WARNING conservation_score -> multiple alignments which have more than 3 species but don't have any conservation scores");
			    ReportManager.problem(this, con, "WARNING DETAILS: There are " + failures.length + " blocks (mlss= " + multi_align_mlss_id + ") with 4 seqs and no conservation score! Must check that the sum of the branch lengths of these 4 species is less than 0.5 (min_neu_evol). If it is greater than 0.5, there is a problem that needs fixing!");
			    ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql4);
			    result = true;

			} else {
			    ReportManager.problem(this, con, "FAILED conservation_score -> multiple alignments which have more than 3 species but don't have any conservation scores");
			    ReportManager.problem(this, con, "FAILURE DETAILS: There are " + failures.length + " blocks (mlss= " + multi_align_mlss_id + ") with more than 4 seqs and no conservation score!");
			    ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
			    result = false;
			}
		    }
		}
	    }
	    
        } else if (tableHasRows(con, "conservation_score")) {
            ReportManager.problem(this, con, "FAILED: Database contains data in the conservation_score table but no corresponding entry in the method_link_species_set table.");
        } else {
            ReportManager.correct(this, con, "NO conservation scores in this database");
        }

        return result;

    }

} // CheckConservationScore
