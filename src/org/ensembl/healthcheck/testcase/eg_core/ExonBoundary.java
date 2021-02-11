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
 * File: ExonBoundaryTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to detect if exon boundaries are at the expected locations
 * @author dstaines
 *
 */
public class ExonBoundary extends AbstractEgCoreTestCase {

	public final static String[] Qs = {
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=1 and t.seq_region_start<>e.seq_region_start and t.seq_region_strand=1 and t.transcript_id not in (select transcript_id from transcript_attrib inner join attrib_type using (attrib_type_id) where code='trans_spliced')",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=1 and t.seq_region_end<>e.seq_region_end and t.seq_region_strand=-1 and t.transcript_id not in (select transcript_id from transcript_attrib inner join attrib_type using (attrib_type_id) where code='trans_spliced')",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=(select max(tt.rank) from exon_transcript tt where tt.transcript_id=t.transcript_id) and t.seq_region_end<>e.seq_region_end and t.seq_region_strand=1 and t.transcript_id not in (select transcript_id from transcript_attrib inner join attrib_type using (attrib_type_id) where code='trans_spliced')",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=(select max(tt.rank) from exon_transcript tt where tt.transcript_id=t.transcript_id) and t.seq_region_start<>e.seq_region_start and t.seq_region_strand=-1 and t.transcript_id not in (select transcript_id from transcript_attrib inner join attrib_type using (attrib_type_id) where code='trans_spliced')" };

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean success = true;
		for (String q : Qs) {
			for (Integer i : getTemplate(dbre).queryForDefaultObjectList(q,
					Integer.class)) {
				success = false;
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"Transcript "
										+ i
										+ " has exons that do not start/end at the expected locations");
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to detect if exon boundaries are at the expected locations";
	}

}
