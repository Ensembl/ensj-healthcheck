/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

/**
 * File: GenomeStatistics.java
 * Created by: James Allen
 * Created on: Apr 22, 2014
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Arrays;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Check a) that genome statistics exist, and b) that the links to
 * attrib_type are present and correct.
 * @author jallen
 *
 */
public class GenomeStatistics extends AbstractEgCoreTestCase {
  // It's not great to hard-code these, but they're hard-coded in the
  // modules that generate the stats, so there's not really another option.
  List<String> statistics = Arrays.asList(new String[] {
    "coding_cnt", "pseudogene_cnt", "noncoding_cnt",
    "noncoding_cnt_s", "noncoding_cnt_l", "noncoding_cnt_m",
    "transcript", "ref_length", "total_length" });
  
  // Should return 1 for each statistic
  private final static String STATISTIC_EXISTS =
    "SELECT COUNT(*) FROM genome_statistics WHERE statistic = ?;";

  // Should return 0.
  private final static String STATISTICS_CONSISTENT =
    "SELECT COUNT(*) FROM "
      + "genome_statistics LEFT OUTER JOIN "
      + "attrib_type USING (attrib_type_id) "
      + "WHERE statistic <> code AND statistic NOT IN "
      + "('transcript', 'alt_transcript', 'PredictionTranscript', 'StructuralVariation');";

  private final static String STATISTICS_CONSISTENT_2 =
    "SELECT attrib_type_id, statistic, code FROM "
      + "genome_statistics LEFT OUTER JOIN "
      + "attrib_type USING (attrib_type_id) "
      + "WHERE statistic <> code AND statistic NOT IN "
      + "('transcript', 'alt_transcript', 'PredictionTranscript', 'StructuralVariation');";
  
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
    
		final int speciesCnt = dbre.getSpeciesIds().size();

    for (final String statistic : statistics) {
      final int statCount = getTemplate(dbre).queryForDefaultObject(STATISTIC_EXISTS, Integer.class, statistic);
      
			if (statCount != speciesCnt) {
        passes = false;
        ReportManager.problem(this, dbre.getConnection(), statCount+" '"+statistic+"' entries found in genome_statistics table - expected "+speciesCnt);
      }
    }
    
    final int statsConsistent = getTemplate(dbre).queryForDefaultObject(STATISTICS_CONSISTENT, Integer.class);
    if (statsConsistent > 0) {
      passes = false;
      ReportManager.problem(this, dbre.getConnection(), "Inconsistent statistic names: "+STATISTICS_CONSISTENT);
      ReportManager.problem(this, dbre.getConnection(), "USEFUL SQL: "+STATISTICS_CONSISTENT_2);
    }
    
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Check a) that genome statistics exist, and b) that the links to attrib_type are present and correct.";
	}

}
