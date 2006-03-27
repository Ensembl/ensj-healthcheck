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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * realtionships.
 */

public class VariationForeignKeys extends SingleDatabaseTestCase {

    /**
     * Create an OrphanTestCase that applies to a specific set of databases.
     */
    public VariationForeignKeys() {

        addToGroup("release");
        addToGroup("variation");
        setDescription("Check for broken foreign-key relationships.");
    }

    /**
     * Look for broken foreign key realtionships.
     * @param dbre
     *          The database to use.
     * @return true Ff all foreign key relationships are valid.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
	int rows = 0;

        Connection con = dbre.getConnection();

        // ----------------------------

       	result &= checkForOrphans(con, "allele", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "allele_group", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "individual", "sample_id", "sample", "sample_id", true);
  
	result &= checkForOrphans(con, "population", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "population_genotype", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "sample_synonym", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "individual_genotype_multiple_bp", "sample_id", "sample", "sample_id", true);

	//        result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "read_coverage", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "tagged_variation_feature", "sample_id", "sample", "sample_id", true);

	//result &= checkForOrphans(con, "pairwise_ld", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "allele", "variation_id", "variation", "variation_id", true);

	//	result &= checkForOrphans(con, "individual_genotype_multiple_bp", "variation_id", "variation", "variation_id", true);
	
	//	result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "population_genotype", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "allele_group_allele", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "variation_synonym", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "flanking_sequence", "variation_id", "variation", "variation_id", false);

	result &= checkForOrphans(con, "variation_feature", "variation_id", "flanking_sequence", "variation_id",true);
	
	result &= checkForOrphans(con, "variation_group_variation", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "variation_feature", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "transcript_variation", "variation_feature_id", "variation_feature", "variation_feature_id", true);

	rows = getRowCount(con,"SELECT COUNT(*) FROM compressed_genotype_single_bp c where c.seq_region_start not in (select vf.seq_region_start from variation_feature vf where c.seq_region_id = vf.seq_region_id)");
	if (rows > 0){
	    ReportManager.problem(this, con, "Compressed genotype table corrupted: contains entries without variation features");
	    result =  false;
	}
	
	return result;
    }

} // VariationForeignKeys
