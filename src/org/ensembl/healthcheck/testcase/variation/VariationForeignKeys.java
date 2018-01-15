/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key realtionships.
 */

public class VariationForeignKeys extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public VariationForeignKeys() {

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
			 * This is allowed allele can have null population_id 
			 * result &= checkForOrphans(con, "allele", "population_id", "population", "population_id",true);
			 */
			result &= checkForOrphans(con, "allele", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "compressed_genotype_region", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "compressed_genotype_region", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "compressed_genotype_var", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "failed_allele", "failed_description_id", "failed_description", "failed_description_id", true);
			result &= checkForOrphans(con, "failed_allele", "allele_id", "allele", "allele_id", true);
			result &= checkForOrphans(con, "failed_variation", "failed_description_id", "failed_description", "failed_description_id", true);
			result &= checkForOrphans(con, "failed_variation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "failed_structural_variation", "failed_description_id", "failed_description", "failed_description_id", true);
			result &= checkForOrphans(con, "failed_structural_variation", "structural_variation_id", "structural_variation", "structural_variation_id", true);
			result &= checkForOrphans(con, "sample_genotype_multiple_bp", "sample_id", "sample_population", "sample_id", true);
			result &= checkForOrphans(con, "sample_genotype_multiple_bp", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "sample_population", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "sample_population", "population_id", "population", "population_id", true);
			result &= checkForOrphans(con, "sample_synonym", "sample_id", "sample", "sample_id", true);
			result &= checkForOrphans(con, "sample_synonym", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "individual_synonym", "individual_id", "individual", "individual_id", true);
			result &= checkForOrphans(con, "phenotype", "phenotype_id", "phenotype_feature", "phenotype_id", true);
			result &= checkForOrphans(con, "phenotype_feature", "phenotype_id", "phenotype", "phenotype_id", true);
			result &= checkForOrphans(con, "phenotype_feature", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "phenotype_feature", "source_id", "source", "source_id", true);
			//result &= checkForOrphans(con, "phenotype_feature", "study_id", "study", "study_id", true);
			result &= checkForOrphans(con, "phenotype_feature_attrib", "phenotype_feature_id", "phenotype_feature", "phenotype_feature_id", true);
			result &= checkForOrphans(con, "phenotype_feature_attrib", "attrib_type_id", "attrib_type", "attrib_type_id", true);
			result &= checkForOrphans(con, "phenotype_ontology_accession", "phenotype_id", "phenotype", "phenotype_id", true);
			result &= checkForOrphans(con, "population_genotype", "population_id", "population", "population_id", true);
			result &= checkForOrphans(con, "population_genotype", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "population_synonym", "population_id", "population", "population_id", true);
			result &= checkForOrphans(con, "read_coverage", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "read_coverage", "sample_id", "sample", "sample_id", true);

			//  result &= checkForOrphans(con, "tmp_sample_genotype_single_bp", "variation_id", "variation", "variation_id", true);

			result &= checkForOrphans(con, "tmp_sample_genotype_single_bp", "sample_id", "sample", "sample_id",true);
			result &= checkForOrphans(con, "transcript_variation", "variation_feature_id", "variation_feature", "variation_feature_id", true);
			result &= checkForOrphans(con, "variation", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "variation", "class_attrib_id", "attrib", "attrib_id", true);
			result &= checkForOrphans(con, "variation_citation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "variation_citation", "publication_id", "publication", "publication_id", true);
			result &= checkForOrphans(con, "variation_feature", "source_id", "source", "source_id", true);
			//result &= checkForOrphans(con, "variation_feature", "variation_id", "allele", "variation_id", true);
			result &= checkForOrphans(con, "variation_feature", "class_attrib_id", "attrib", "attrib_id", true);
			result &= checkForOrphans(con, "variation_feature", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "variation_set_structure", "variation_set_sub", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_set_structure", "variation_set_super", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_set_variation", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "variation_set_variation", "variation_set_id", "variation_set", "variation_set_id", true);
			result &= checkForOrphans(con, "variation_synonym", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "variation_synonym", "variation_id", "variation", "variation_id", true);
			result &= checkForOrphans(con, "structural_variation_feature", "structural_variation_id", "structural_variation", "structural_variation_id", true);
			result &= checkForOrphans(con, "structural_variation_feature", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "structural_variation_feature", "study_id", "study", "study_id", true);
			result &= checkForOrphans(con, "structural_variation_feature", "class_attrib_id", "attrib", "attrib_id", true);
			result &= checkForOrphans(con, "structural_variation_feature", "seq_region_id", "seq_region", "seq_region_id", true);
			result &= checkForOrphans(con, "structural_variation", "source_id", "source", "source_id", true);
			result &= checkForOrphans(con, "structural_variation", "study_id", "study", "study_id", true);
			result &= checkForOrphans(con, "structural_variation", "class_attrib_id", "attrib", "attrib_id", true);
			result &= checkForOrphans(con, "structural_variation_sample", "structural_variation_id", "structural_variation", "structural_variation_id", true);
			result &= checkForOrphans(con, "structural_variation_association", "structural_variation_id", "structural_variation", "structural_variation_id", true);

			
			// alleles and genotypes
			result &= checkForOrphans(con, "allele", "allele_code_id", "allele_code", "allele_code_id", true);
			result &= checkForOrphans(con, "population_genotype", "genotype_code_id", "genotype_code", "genotype_code_id", true);
			result &= checkForOrphans(con, "genotype_code", "allele_code_id", "allele_code", "allele_code_id", true);
            
      // check phenotype_feature (special case since it can contain links to multiple tables)
      rows = countOrphansWithConstraint(con,"phenotype_feature","object_id","variation","name","type = 'Variation'");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in phenotype_feature table without entries in variation");
				result = false;
			}
            
      rows = countOrphansWithConstraint(con,"phenotype_feature","object_id","structural_variation","variation_name","type IN ('StructuralVariation','SupportingStructuralVariation')");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in phenotype_feature table without entries in structural_variation");
				result = false;
			}

			rows = countOrphansWithConstraint(con,"compressed_genotype_region","seq_region_id","variation_feature","seq_region_id","seq_region_start = variation_feature.seq_region_start");
			if (rows > 0) {
				ReportManager.problem(this, con, rows + "entries in Compressed genotype table without variation features");
				result = false;
			}
	
			// Hmmm.. this is not really a foreign key check.. [pontus]
			if (DBUtils.getRowCount(con, "SHOW TABLES like 'tmp_sample%'") > 0) {
				rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM tmp_sample_genotype_single_bp where length(allele_1) >1 or length(allele_2) > 1");
				if (rows > 0) {
					ReportManager.problem(this, con, rows + "entries in The tmp_sample_genotype_single_bp table contains alleles with more than 1 bp");
					result = false;
				}
			}
			
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
