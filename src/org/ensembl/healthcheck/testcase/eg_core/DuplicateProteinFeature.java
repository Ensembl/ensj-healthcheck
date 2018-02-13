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

/**
 * File: DuplicateProteinFeatures.java
 * Created by: dwilson
 * Created on: Mar 4, 2013
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to find where protein_features have been added twice
 * @author dwilson
 *
 */
public class DuplicateProteinFeature extends AbstractEgCoreTestCase {

	private final static String DUPLICATE_XREF = "select count(*) from (select count(*) from xref x group by x.dbprimary_acc,x.external_db_id,x.info_type,x.info_text having count(*)>1) cc";
	private final static String DUPLICATE_OBJ_XREF = "select count(*) from (select count(*) from xref x join object_xref ox using (xref_id) group by ox.ensembl_id, ox.ensembl_object_type,x.dbprimary_acc,x.external_db_id,x.info_type,x.info_text having count(*)>1) cc";

	private final static String DUPLICATE_PF  = "SELECT COUNT(*) FROM (SELECT COUNT(*) FROM protein_feature GROUP BY translation_id, seq_start, seq_end, hit_start, hit_end, hit_name, analysis_id, score, evalue, perc_ident HAVING COUNT(*)>1) cc;";
	private final static String ALTERNATE_SQL = "SELECT COUNT(*) FROM protein_feature pf1, protein_feature pf2 "
                                              + "WHERE pf1.protein_feature_id != pf2.protein_feature_id AND "
                                              + "pf1.translation_id = pf2.translation_id AND "
                                              + "pf1.seq_start = pf2.seq_start AND "
                                              + "pf1.seq_end = pf2.seq_end AND "
                                              + "pf1.hit_start = pf2.hit_start AND "
                                              + "pf1.hit_end = pf2.hit_end AND "
                                              + "pf1.hit_name = pf2.hit_name AND "
                                              + "pf1.analysis_id = pf2.analysis_id AND "
                                              + "pf1.score = pf2.score AND "
                                              + "pf1.evalue = pf2.evalue AND "
                                              + "pf1.perc_ident = pf2.perc_ident;";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nDupPF =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_PF, Integer.class);
		if(nDupPF>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupPF+" duplicates found in protein_feature: "+DUPLICATE_PF);
			ReportManager.problem(this, dbre.getConnection(), "Alternative useful SQL: "+ALTERNATE_SQL);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find where protein_features have been added twice";
	}

}
