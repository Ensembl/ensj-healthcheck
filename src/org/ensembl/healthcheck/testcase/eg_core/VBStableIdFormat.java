/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check to see if stable_ids have the correct format
 * 
 * @author jallen
 * 
 */
public class VBStableIdFormat extends AbstractEgCoreTestCase {

	private final static String GENE_QUERY =
    "select count(stable_id) from gene where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}$'";

	private final static String TRANSCRIPT_QUERY =
    "select count(stable_id) from transcript where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}[[.-.]]R[[:alpha:]]$'";

	private final static String TRANSLATION_QUERY =
    "select count(stable_id) from translation where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}[[.-.]]P[[:alpha:]]$'";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate stmp = getSqlTemplate(dbre);
		boolean passes = true;

    Integer genes = stmp.queryForDefaultObject(GENE_QUERY, Integer.class);
    if (genes > 0) {
			ReportManager.problem(this, dbre.getConnection(), genes+" genes have invalid stable IDs");
			ReportManager.problem(this, dbre.getConnection(), "USEFUL SQL: select stable_id from gene where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}$';");
			passes = false;
		}

    Integer transcripts = stmp.queryForDefaultObject(TRANSCRIPT_QUERY, Integer.class);
    if (transcripts > 0) {
			ReportManager.problem(this, dbre.getConnection(), transcripts+" transcripts have invalid stable IDs");
			ReportManager.problem(this, dbre.getConnection(), "USEFUL SQL: select stable_id from transcript where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}[[.-.]]R[[:alpha:]]$';");
			passes = false;
		}

    Integer translations = stmp.queryForDefaultObject(TRANSLATION_QUERY, Integer.class);
    if (translations > 0) {
			ReportManager.problem(this, dbre.getConnection(), translations+" translations have invalid stable IDs");
			ReportManager.problem(this, dbre.getConnection(), "USEFUL SQL: select stable_id from translation where stable_id NOT REGEXP '^[[:alpha:]]{4}[[:digit:]]{6}[[.-.]]P[[:alpha:]]$';");
			passes = false;
		}

		return passes;
	}

	@Override
	protected String getEgDescription() {
		return "Check to see if stable_ids have the correct format";
	}
}
