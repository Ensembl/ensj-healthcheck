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
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=1 and t.seq_region_start<>e.seq_region_start and t.seq_region_strand=1",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=1 and t.seq_region_end<>e.seq_region_end and t.seq_region_strand=-1",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=(select max(tt.rank) from exon_transcript tt where tt.transcript_id=t.transcript_id) and t.seq_region_end<>e.seq_region_end and t.seq_region_strand=1",
			"select t.transcript_id, t.seq_region_start, t.seq_region_end, e.seq_region_start from transcript t join exon_transcript et using (transcript_id) join exon e using (exon_id) where et.rank=(select max(tt.rank) from exon_transcript tt where tt.transcript_id=t.transcript_id) and t.seq_region_start<>e.seq_region_start and t.seq_region_strand=-1" };

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
