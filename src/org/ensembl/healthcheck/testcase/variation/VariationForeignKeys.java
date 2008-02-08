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
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true Ff all foreign key relationships are valid.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;
		int rows = 0;

		Connection con = dbre.getConnection();

		// ----------------------------

        // ----------------------------
 	      	
	/*This is allowed allele can have null sample_id
	  result &= checkForOrphans(con, "allele", "sample_id", "sample", "sample_id", true);*/

	/*This is a sql to the above command
	SELECT COUNT(*) FROM allele LEFT JOIN sample ON allele.sample_id = sample.sample_id WHERE sample.sample_id is NULL;
	*/
	result &= checkForOrphans(con, "variation", "source_id", "source", "source_id", true);

	result &= checkForOrphans(con, "variation_synonym", "source_id", "source", "source_id", true);

        result &= checkForOrphans(con, "variation_synonym", "variation_id", "variation", "variation_id", true);
        
        result &= checkForOrphans(con, "variation_feature", "source_id", "source", "source_id", true);

	result &= checkForOrphans(con, "allele_group", "sample_id", "sample", "sample_id", true);
	
	result &= checkForOrphans(con, "individual", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "population", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "population_genotype", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "population_genotype", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "sample_synonym", "sample_id", "sample", "sample_id", true);
		
	result &= checkForOrphans(con, "individual_genotype_multiple_bp", "sample_id", "sample", "sample_id", true);
	
	/*result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "variation_id", "variation", "variation_id", true); instead check compressed_genotype_single_bp with individual table*/

        /*result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "variation_id", "variation_feature", "variation_id", true); this is true only for ensembl snps*/
        
	result &= checkForOrphans(con, "compressed_genotype_single_bp", "sample_id", "individual", "sample_id", true);

	result &= checkForOrphans(con, "individual_population", "individual_sample_id", "sample", "sample_id", true);
	
	result &= checkForOrphans(con, "individual_genotype_multiple_bp", "sample_id", "individual_population", "individual_sample_id", true);
        	
	result &= checkForOrphans(con, "read_coverage", "sample_id", "sample", "sample_id", true);
	
	result &= checkForOrphans(con, "tagged_variation_feature", "sample_id", "sample", "sample_id", true);

	result &= checkForOrphans(con, "allele", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "allele_group_allele", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "variation_synonym", "variation_id", "variation", "variation_id", true);
	
	result &= checkForOrphans(con, "flanking_sequence", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "variation_feature", "variation_id", "flanking_sequence", "variation_id",true);
	
	result &= checkForOrphans(con, "variation_feature", "variation_id", "allele", "variation_id",true);
	
	result &= checkForOrphans(con, "variation_group_variation", "variation_id", "variation", "variation_id", true);

	result &= checkForOrphans(con, "transcript_variation", "variation_feature_id", "variation_feature", "variation_feature_id", true);
	
	rows = getRowCount(con,"SELECT COUNT(*) FROM variation_feature vf, flanking_sequence f where vf.variation_id=f.variation_id and vf.seq_region_id != f.seq_region_id and vf.map_weight=1 and vf.seq_region_id not in (226063,226064,226065,226066,226031,226054)");
	if (rows > 0){
	    ReportManager.problem(this, con, "flanking_sequence contains entries have same variation_id, but different seq_region_id compare with table variation features");
	    result =  false;
	}
	rows = getRowCount(con,"SELECT COUNT(*) FROM compressed_genotype_single_bp c where c.seq_region_start not in (select vf.seq_region_start from variation_feature vf where c.seq_region_id = vf.seq_region_id)");
	if (rows > 0){
	    ReportManager.problem(this, con, "Compressed genotype table corrupted: contains entries without variation features");
	    result =  false;
	}
		
	if (getRowCount(con,"SHOW TABLES like 'tmp_individual%'") > 0){
	    rows = getRowCount(con,"SELECT COUNT(*) FROM tmp_individual_genotype_single_bp where length(allele_1) >1 or length(allele_2) > 1");
	    if (rows > 0){
		ReportManager.problem(this,con,"The tmp_individual_genotype_single_bp table contains alleles with more than 1 bp");
		result = false;
	    }
	}
	if (! result ){
	    //if there were no problems, just inform for the interface to pick the HC
	    ReportManager.info(this,con,"VariationForeignKeys test passed without any problem");
	}
	return result;
	
	}
    
} // VariationForeignKeys
