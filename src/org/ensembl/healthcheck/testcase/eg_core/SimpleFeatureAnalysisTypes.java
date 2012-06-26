/**
 * SimpleFeatureAnalysisTypes
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to check simple features for permitted types
 * 
 * @author dstaines
 * 
 */
public class SimpleFeatureAnalysisTypes extends AbstractEgCoreTestCase {

	private final static String LOGIC_NAME_SQL = "select distinct logic_name from simple_feature "
			+ "join analysis using (analysis_id) "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "where species_id=?";

	private final static Set<String> BLACKLISTED_ANALYSES = new HashSet<String>(
			Arrays.asList(new String[] { "gene", "mrna", "cds" }));

	public SimpleFeatureAnalysisTypes() {
		super();
		setDescription("Test to check simple features are not in the blacklist: "
				+ BLACKLISTED_ANALYSES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#runTest(org
	 * .ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate temp = getSqlTemplate(dbre);
		// count number genes
		for (int speciesId : dbre.getSpeciesIds()) {
			for (String analysis : temp.queryForDefaultObjectList(
					LOGIC_NAME_SQL, String.class, speciesId)) {
				if (BLACKLISTED_ANALYSES.contains(analysis)) {
					ReportManager.problem(this, dbre.getConnection(),
							"Blacklisted simple_feature of analysis type "
									+ analysis + " found for species "
									+ speciesId);
					result = false;
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to check simple features for permitted types";
	}
}
