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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key realtionships.
 */

public class VariationForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public VariationForeignKeys() {

		// addToGroup("release"); removed to speed up cron job
		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Check for broken foreign-key relationships.");
		setTeamResponsible(Team.VARIATION);

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

		try {
			
			/*
			 * This is allowed allele can have null sample_id 
			 * result &= checkForOrphans(con, "allele", "sample_id", "sample", "sample_id",true);
			 */
			result &= checkForOrphans(con, "allele", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "allele_group", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "allele_group_allele", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "compressed_genotype_region", "sample_id", "individual", "sample_id", true);
			result &= checkForOrphans(con, "failed_allele", "failed_description_id", "failed_description", "failed_description_id", true);
			result &= checkForOrphans(con, "failed_variation", "failed_description_id", "failed_description", "failed_description_id", true);
			result &= checkForOrphans(con, "flanking_sequence", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "individual", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "individual_genotype_multiple_bp", "sample_id", "individual_population", "individual_sample_id", true);
			result &= checkForOrphans(con, "individual_genotype_multiple_bp", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "individual_population", "individual_sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "population", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "population_genotype", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "population_genotype", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "compressed_genotype_var", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "read_coverage", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "read_coverage", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "sample_synonym", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "tagged_variation_feature", "sample_id", "sample", "sample_id", true);
			/*
			 * instead check compressed_genotype_single_bp with individual table
			 * result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "variation_id", "variation", "variation_id", true);
			 * this is true only for ensembl snps
			 * result &= checkForOrphans(con, "tmp_individual_genotype_single_bp", "variation_id", "variation_feature", "variation_id",true);
			 */
			result &= checkForOrphans(con, "transcript_variation", "variation_feature_id", "variation_feature", "variation_feature_id", true);
			result &= checkForOrphans(con, "variation", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "variation_annotation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "variation_feature", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "variation_feature", "variation_id", "flanking_sequence", "variation_id", true);
			result &= checkForOrphans(con, "variation_feature", "variation_id", "allele", "variation_id", true);
			result &= checkForOrphans(con, "variation_group_variation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "variation_set_structure", "variation_set_sub", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_set_structure", "variation_set_super", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_set_variation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "variation_set_variation", "variation_set_id", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_synonym", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "variation_synonym", "variation_id", "variation", "variation_id", true);
			
			// alleles and genotypes
			result &= checkForOrphans(con, "allele", "allele_code_id", "allele_code", "allele_code_id", true);
			result &= checkForOrphans(con, "population_genotype", "genotype_code_id", "genotype_code", "genotype_code_id", true);
			result &= checkForOrphans(con, "genotype_code", "allele_code_id", "allele_code", "allele_code_id", true);

			rows = countOrphansWithConstraint(con,"compressed_genotype_region","seq_region_id","variation_feature","seq_region_id","seq_region_start = variation_feature.seq_region_start");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in Compressed genotype table without variation features");
				result = false;
			}
	
			// Hmmm.. this is not really a foreign key check.. [pontus]
			if (getRowCount(con, "SHOW TABLES like 'tmp_individual%'") > 0) {
				rows = getRowCount(con, "SELECT COUNT(*) FROM tmp_individual_genotype_single_bp where length(allele_1) >1 or length(allele_2) > 1");
				if (rows > 0) {
					ReportManager.problem(this, con, rows + "entries in The tmp_individual_genotype_single_bp table contains alleles with more than 1 bp");
					result = false;
				}
			}
			
			/* haplotype seq_region_id in chimp 506737 and in human 27795,27796,27797,27798,27799,27800,27801,27802,27803 hard coded here */
			/*
			 * This situation must be allowed though.. basically, this is just a compression of the flanking sequence and any sequence
			 * possible to compress against should be ok. In any case, just checking the seq_region_id and cases where we only
			 *  have a single mapping is very inconsistent and with poor resolution  
			 * for this kind of test.. I'll comment out this test [Pontus]
			 *
			rows = getRowCount(
					con,
					"SELECT COUNT(*) " +
					"FROM variation_feature vf, flanking_sequence f " +
					"where vf.variation_id=f.variation_id and " +
					"vf.seq_region_id != f.seq_region_id and " +
					"vf.map_weight=1 and " +
					"vf.seq_region_id not in (506737,27795,27796,27797,27798,27799,27800,27801,27802,27803) and " +
					"f.seq_region_id not in (506737,27795,27796,27797,27798,27799,27800,27801,27802,27803)");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in flanking_sequence have same variation_id, but different seq_region_id compare with table variation features");
				result = false;
			}
			 */
			/*
			 * Similar argument as above, I'll comment out [Pontus]
			 *
			rows = getRowCount(
					con,
					"SELECT COUNT(*) " +
					"FROM variation_feature vf, " +
					"flanking_sequence f " +
					"where " +
					"vf.variation_id=f.variation_id and " +
					"vf.seq_region_strand != f.seq_region_strand and " +
					"vf.map_weight=1 and " +
					"vf.seq_region_id not in (506737,27795,27796,27797,27798,27799,27800,27801,27802,27803) and " +
					"f.seq_region_id not in (506737,27795,27796,27797,27798,27799,27800,27801,27802,27803)");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in flanking_sequence have same variation_id, but different seq_region_strand compare with table variation features");
				result = false;
			}
			*/
			
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
			result = false;
		}
		if (result) {
			// if there were no problems, just inform for the interface to pick the HC
			ReportManager.correct(this, con, "VariationForeignKeys test passed without any problem");
		}
		return result;

	}

} // VariationForeignKeys
