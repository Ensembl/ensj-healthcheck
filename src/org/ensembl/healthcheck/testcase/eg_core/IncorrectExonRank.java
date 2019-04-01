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

/**
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to see if we have transcripts with no rank 1 exon
 * @author dstaines
 *
 */
public class IncorrectExonRank extends AbstractEgCoreTestCase {

	private final static String NO_RANK_ONE = "select count(*) from transcript where transcript_id not in "
			+ "(select transcript_id from exon_transcript where rank=1)";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int n =  getTemplate(dbre).queryForDefaultObject(NO_RANK_ONE, Integer.class);
		if(n>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), n+" transcripts found with no rank 1: "+NO_RANK_ONE);
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if we have transcripts with no rank 1 exon";
	}

}
