package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

public class NoRepeatFeatures extends AbstractEgCoreTestCase {

	@Override
	protected String getEgDescription() {
		return "Check that some kind of repeat masker has been run on the species.";
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		List<Integer> numRepeatFeaturesList = getTemplate(dbre).queryForDefaultObjectList(
				"select count(*) from repeat_feature;", Integer.class
		);
		
		Integer numRepeatFeatures = numRepeatFeaturesList.get(0);
		
		if (numRepeatFeatures==0) {
			
			ReportManager.problem(
				this, 
				dbre.getConnection(), 
				"No repeat features found!"
			);
			
			return false;
		}
		if (numRepeatFeatures<100) {
			
			ReportManager.problem(
				this, 
				dbre.getConnection(), 
				"Number of repeat features ("+numRepeatFeatures+") is suspiciously low!"
			);
			
			return false;
		}
		
		return true;
	}

}
