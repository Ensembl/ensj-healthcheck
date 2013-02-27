/**
 * File: SampleMetaTestCase.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to make sure that seq_regions have appropriate names
 * 
 * @author dstaines
 * 
 */
public class SeqRegionNaming extends AbstractEgCoreTestCase {

	private final static String COUNT_SEQ = "select count(*) from seq_region s "
			+ "join coord_system c using (coord_system_id) where species_id = ? and s.name LIKE ?";
	private final static String CS_CLAUSE_INC = " and c.name = ?";
	private final static String CS_CLAUSE_EXC = " and c.name <> ?";

	public SeqRegionNaming() {
		super();
	}

	@Override
	protected String getEgDescription() {
		return "Test to make sure that seq_regions have appropriate names";
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate template = getSqlTemplate(dbre);
		boolean success = true;
		for (int speciesId : dbre.getSpeciesIds()) {
			// 1. Do we have any non-chromosome seq_regions with names like
			// chromosome?
			int cnt = template.queryForDefaultObject(COUNT_SEQ + CS_CLAUSE_EXC,
					Integer.class, speciesId, "%chromosome%", "chromosome");
			if (cnt > 0) {
				ReportManager.problem(this, dbre.getConnection(), "Found "
						+ cnt + " non-chromosome seq_regions for species_id "
						+ speciesId + " with names containing \"chromosome\"");
				success = false;
			}
			// 2. Do we have any chromosome seq_regions with names like
			// supercont?
			cnt = template.queryForDefaultObject(COUNT_SEQ + CS_CLAUSE_INC,
					Integer.class, speciesId, "%supercont%", "chromosome");
			if (cnt > 0) {
				ReportManager.problem(this, dbre.getConnection(), "Found "
						+ cnt + " chromosome seq_regions for species_id "
						+ speciesId + " with names containing \"supercont\"");
				success = false;
			}
		}
		return success;
	}

}
