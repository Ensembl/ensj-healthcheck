package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test to make sure that db column is populated for analysis types attached to
 * protein features. If this is missing the domain column will not be populated.
 * 
 * @author dstaines
 * 
 */
public class ProteinFeatureAnalysisDb extends AbstractEgCoreTestCase {

	private static final String SQL = "select logic_name from analysis a where (db='' or db is null) and analysis_id in (select distinct analysis_id from protein_feature pf)";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passed = true;
		List<String> logicNames = getTemplate(dbre).queryForDefaultObjectList(
				SQL, String.class);
		if (logicNames.size() > 0) {
			passed = false;
			ReportManager
					.problem(
							this,
							dbre.getConnection(),
							"The following analysis types attached to protein features do not have db set and will not display properly: "
									+ logicNames);
		}
		return passed;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to make sure that db column is populated for analysis types attached to"
		 +" protein features. If this is missing the domain column will not be populated.";
	}

}
