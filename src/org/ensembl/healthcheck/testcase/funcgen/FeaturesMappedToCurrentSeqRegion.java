package org.ensembl.healthcheck.testcase.funcgen;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author mnuhn
 * 
 * Check, if the features are on a seq region that is current.
 *
 */
public class FeaturesMappedToCurrentSeqRegion extends SingleDatabaseTestCase {

	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		
		final String[] featureTables = new String[] {
			"probe_feature",
			"annotated_feature",
			"external_feature",
			"regulatory_feature",
			"result_feature",
			"segmentation_feature"
		};
		
		boolean passes = true;
		
		for (int i=0; i<featureTables.length; i++) {
			
			String currentTable = featureTables[i];
			boolean currentPasses = checkCountIsZero(
					dbre.getConnection(), 
					currentTable + " join seq_region using (seq_region_id) join coord_system using (coord_system_id)", 
					"coord_system.is_current!=true"
			);
			if (!currentPasses) {
				String msg = currentTable + " has features mapped to a seq region that is not current.";
				logger.severe(msg);
				ReportManager.problem(this, dbre.getConnection(), msg);
			}
			passes &= currentPasses;
		}
		return passes;
	}
}
