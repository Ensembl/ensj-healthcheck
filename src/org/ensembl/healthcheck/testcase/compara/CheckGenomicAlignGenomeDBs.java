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
 * An EnsEMBL Healthcheck test case that checks that all the genome_dbs for a
 * method_link_species_set are present in the genomic_aligns
 */

public class CheckGenomicAlignGenomeDBs extends SingleDatabaseTestCase {

    /**
     * Create an CheckGenomicAlignGenomeDBs that applies to a specific set of databases.
     */
    public CheckGenomicAlignGenomeDBs() {

        addToGroup("compara_db_constraints");
        setDescription("Check the genome_dbs for a method_link_species_set are present in the genomic_aligns");
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
	 * Check have entries in the genomic_align table
	 */
	if (!tableHasRows(con, "genomic_align")) {
	    ReportManager.problem(this, con, "No entries in the genomic_align table"); 
	    return result;
	} 
	if (!tableHasRows(con, "genomic_align_block")) {
	    ReportManager.problem(this, con, "No entries in the genomic_align_block table"); 
	    return result;
	} 
	if (!tableHasRows(con, "method_link_species_set")) {
	    ReportManager.problem(this, con, "No entries in the method_link_species_set table"); 
	    return result;
	}
	/**
	 * Get all method_link_species_set_ids for genomic_align_blocks
	 */
	String[] method_link_species_set_ids = getColumnValues(con, "SELECT distinct(method_link_species_set_id) FROM genomic_align_block");

        if (method_link_species_set_ids.length > 0) {

	    for (int i = 0; i < method_link_species_set_ids.length; i++) {
		/**
		 * Expected number of genome_db_ids
		 */
		
		
		String gdb_sql = new String("SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = " + method_link_species_set_ids[i]);
		String[] num_genome_db_ids = getColumnValues(con, gdb_sql);
		

		/** 
		 * Find genome_db_ids in genomic_aligns. For speed, only look
		 * at the first 100 genomic_align_blocks. If the test fails,
		 * it could be by chance that not all the genome_db_ids are
		 * found. Expect the number of distinct genome_db_ids 
		 * to be the same as the number of genome_db_ids in the 
		 * species set except when I have an ancestor when the number
		 * from the genomic_aligns will be one larger. Don't 
		 * specifically test for this, just check if it's equal to or
		 * larger - more worried if it's smaller ie missed some
		 * expected genome_db_ids.
		 */
		String useful_sql;
		useful_sql = new String("SELECT COUNT(DISTINCT genome_db_id) FROM (SELECT * FROM genomic_align_block WHERE method_link_species_set_id = " + method_link_species_set_ids[i] + " limit 100) t1 LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN dnafrag USING (dnafrag_id) HAVING COUNT(DISTINCT genome_db_id) >= (SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = " + method_link_species_set_ids[i] + " );");
		String[] success = getColumnValues(con, useful_sql); 

		if (success.length > 0) {
		    /**
		    System.out.println("MLSS " + method_link_species_set_ids[i] + " real " + success[0] + " expected " + num_genome_db_ids[0]);
		    */
		    ReportManager.correct(this, con, "All genome_dbs are present in the genomic_aligns for method_link_species_set_id " + method_link_species_set_ids[i]);
		} else {
		    ReportManager.problem(this, con, "WARNING not all the genome_dbs are present in the first 100 genomic_align_block_ids. Could indicate a problem with alignment with method_link_species_set_id " + method_link_species_set_ids[i]);
		    ReportManager.problem(this, con, "USEFUL SQL: " + useful_sql);
		    result = false;
		}
	    }
	}

        return result;

    }

} // CheckGenomicAlignGenomeDBs
